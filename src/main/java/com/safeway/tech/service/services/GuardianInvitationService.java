package com.safeway.tech.service.services;

import com.safeway.tech.api.dto.guardian.GuardianCandidateResponse;
import com.safeway.tech.api.dto.guardian.GuardianInvitationAcceptRequest;
import com.safeway.tech.api.dto.guardian.GuardianInvitationApprovalRequest;
import com.safeway.tech.api.dto.guardian.GuardianInvitationCreateRequest;
import com.safeway.tech.api.dto.guardian.GuardianInvitationCreatedResponse;
import com.safeway.tech.api.dto.guardian.GuardianInvitationRegistrationResponse;
import com.safeway.tech.api.dto.guardian.GuardianInvitationResponse;
import com.safeway.tech.api.dto.guardian.PendingGuardianInvitationResponse;
import com.safeway.tech.auth.core.port.PasswordHasherPort;
import com.safeway.tech.auth.core.port.PasswordVerifierPort;
import com.safeway.tech.domain.enums.InvitationStatusEnum;
import com.safeway.tech.domain.enums.InvitationTypeEnum;
import com.safeway.tech.domain.enums.UserRole;
import com.safeway.tech.domain.models.Address;
import com.safeway.tech.domain.models.Guardian;
import com.safeway.tech.domain.models.GuardianInvitation;
import com.safeway.tech.domain.models.GuardianStudent;
import com.safeway.tech.domain.models.Student;
import com.safeway.tech.domain.models.Transport;
import com.safeway.tech.domain.models.User;
import com.safeway.tech.infra.exception.EntityNotFoundException;
import com.safeway.tech.infra.exception.InvitationExpiredException;
import com.safeway.tech.repository.GuardianInvitationRepository;
import com.safeway.tech.repository.GuardianRepository;
import com.safeway.tech.repository.GuardianStudentRepository;
import com.safeway.tech.repository.UserRepository;
import com.safeway.tech.service.mappers.GuardianMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.security.SecureRandom;
import java.time.Duration;
import java.time.Instant;
import java.util.Base64;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class GuardianInvitationService {

    private static final Duration INVITATION_LIFETIME = Duration.ofDays(7);
    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    private final GuardianInvitationRepository invitationRepository;
    private final GuardianRepository guardianRepository;
    private final GuardianStudentRepository guardianStudentRepository;
    private final UserRepository userRepository;
    private final StudentService studentService;
    private final AddressService addressService;
    private final CurrentUserService currentUserService;
    private final PasswordHasherPort passwordHasher;
    private final PasswordVerifierPort passwordVerifier;

    @Transactional
    public GuardianInvitationCreatedResponse create(GuardianInvitationCreateRequest request) {
        User driver = requireCurrentDriver();
        Transport transport = driver.getTransport();
        GuardianInvitation invitation = new GuardianInvitation();
        invitation.setToken(generateToken());
        invitation.setType(request.type());
        invitation.setStatus(InvitationStatusEnum.PENDING);
        invitation.setExpiresAt(Instant.now().plus(INVITATION_LIFETIME));
        invitation.setTransport(transport);
        invitation.setCreatedBy(driver);

        if (request.type() == InvitationTypeEnum.STUDENT_SPECIFIC) {
            if (request.studentId() == null || request.guardianId() == null) {
                throw new ResponseStatusException(
                        HttpStatus.BAD_REQUEST, "Student-specific invitations require studentId and guardianId");
            }
            Student student = studentService.findByIdForCurrentTransport(request.studentId());
            Guardian guardian = findGuardianForStudent(request.guardianId(), student.getId(), transport.getId());
            ensureContactMatches(request.targetEmail(), guardian.getEmail(), "email");
            ensureContactMatches(request.targetPhoneNumber(), guardian.getPrimaryPhoneNumber(), "phone");

            invitation.setStudent(student);
            invitation.setGuardian(guardian);
            invitation.setTargetEmail(guardian.getEmail());
            invitation.setTargetPhoneNumber(guardian.getPrimaryPhoneNumber());
        } else {
            if (request.studentId() != null || request.guardianId() != null) {
                throw new ResponseStatusException(
                        HttpStatus.BAD_REQUEST, "Generic invitations cannot specify a student or guardian");
            }
            invitation.setTargetEmail(normalizeEmail(request.targetEmail()));
            invitation.setTargetPhoneNumber(normalizePhone(request.targetPhoneNumber()));
        }

        invitation = invitationRepository.save(invitation);
        return new GuardianInvitationCreatedResponse(
                invitation.getId(),
                invitation.getType(),
                invitation.getToken(),
                invitation.getExpiresAt());
    }

    @Transactional
    public GuardianInvitationResponse getPublicInvitation(String token) {
        GuardianInvitation invitation = invitationRepository.findByTokenAndActiveTrue(token)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Invitation not found"));
        expireIfNeeded(invitation);
        return toPublicResponse(invitation);
    }

    @SuppressWarnings("MethodLength")
    @Transactional(noRollbackFor = InvitationExpiredException.class)
    public GuardianInvitationRegistrationResponse accept(String token, GuardianInvitationAcceptRequest request) {
        GuardianInvitation invitation = invitationRepository.findActiveByTokenForUpdate(token)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Invitation not found"));
        expireIfNeeded(invitation);
        if (invitation.getStatus() == InvitationStatusEnum.EXPIRED) {
            throw new InvitationExpiredException();
        }
        if (invitation.getStatus() != InvitationStatusEnum.PENDING) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Invitation is no longer available");
        }

        String email = normalizeEmail(request.email());
        User existingUser = userRepository.findByEmail(email).orElse(null);
        if (existingUser != null) {
            return acceptForExistingGuardian(invitation, request, email, existingUser);
        }
        validateTargetContacts(invitation, email, request.primaryPhoneNumber());

        Guardian guardian;
        User user;
        if (invitation.getType() == InvitationTypeEnum.STUDENT_SPECIFIC) {
            guardian = invitation.getGuardian();
            if (guardian == null || !guardian.isActive()) {
                throw new ResponseStatusException(HttpStatus.CONFLICT, "The invited pre-registration is unavailable");
            }
            validateSpecificInvitation(invitation, guardian);
            if (guardian.getUser() != null && guardian.getUser().getRole() != UserRole.DRIVER) {
                throw new ResponseStatusException(HttpStatus.CONFLICT, "This guardian already has an account");
            }
            user = createGuardianUser(guardian.getName(), email,
                    normalizePhone(guardian.getPrimaryPhoneNumber()), request.password());
            guardian.setEmail(email);
            guardian.setUser(user);
            guardianRepository.save(guardian);
            invitation.setStatus(InvitationStatusEnum.COMPLETED);
        } else {
            if (request.name() == null || request.name().isBlank()
                    || request.primaryPhoneNumber() == null || request.primaryPhoneNumber().isBlank()
                    || request.address() == null) {
                throw new ResponseStatusException(
                        HttpStatus.BAD_REQUEST, "Generic registration requires name, phone number, and address");
            }
            validatePhoneNumber(request.primaryPhoneNumber());
            if (request.secondaryPhoneNumber() != null && !request.secondaryPhoneNumber().isBlank()) {
                validatePhoneNumber(request.secondaryPhoneNumber());
            }
            Address address = addressService.create(request.address());
            user = createGuardianUser(request.name().trim(), email, normalizePhone(request.primaryPhoneNumber()), request.password());
            guardian = new Guardian();
            guardian.setName(request.name().trim());
            guardian.setPrimaryPhoneNumber(normalizePhone(request.primaryPhoneNumber()));
            guardian.setSecondaryPhoneNumber(normalizePhone(request.secondaryPhoneNumber()));
            guardian.setEmail(email);
            guardian.setAddress(address);
            guardian.setUser(user);
            guardian = guardianRepository.save(guardian);
            invitation.setGuardian(guardian);
            invitation.setTargetEmail(email);
            invitation.setTargetPhoneNumber(normalizePhone(request.primaryPhoneNumber()));
            invitation.setExpiresAt(null);
            invitation.setStatus(InvitationStatusEnum.AWAITING_APPROVAL);
        }

        invitationRepository.save(invitation);
        return new GuardianInvitationRegistrationResponse(guardian.getId(), invitation.getStatus(),
                invitation.getStatus() == InvitationStatusEnum.AWAITING_APPROVAL);
    }

    private GuardianInvitationRegistrationResponse acceptForExistingGuardian(
            GuardianInvitation invitation,
            GuardianInvitationAcceptRequest request,
            String email,
            User existingUser) {
        if (!existingUser.isActive()
                || existingUser.getRole() != UserRole.GUARDIAN
                || !passwordVerifier.matches(request.password(), existingUser.getPasswordHash())) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Account credentials are invalid");
        }
        Guardian existingGuardian = guardianRepository.findByUserIdAndActiveTrue(existingUser.getId())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.CONFLICT, "Guardian account is not associated with a guardian profile"));

        if (invitation.getType() == InvitationTypeEnum.STUDENT_SPECIFIC) {
            Guardian preRegistration = invitation.getGuardian();
            Student student = invitation.getStudent();
            if (preRegistration == null || student == null || !preRegistration.isActive()) {
                throw new ResponseStatusException(HttpStatus.CONFLICT, "The invited pre-registration is unavailable");
            }
            validateSpecificInvitation(invitation, preRegistration);
            ensureContactMatches(invitation.getTargetEmail(), email, "email");
            linkGuardianToStudent(existingGuardian, student);
            if (!preRegistration.getId().equals(existingGuardian.getId())) {
                preRegistration.setActive(false);
                guardianRepository.save(preRegistration);
            }
            invitation.setGuardian(existingGuardian);
            invitation.setStatus(InvitationStatusEnum.COMPLETED);
        } else {
            validateTargetContacts(invitation, email, existingGuardian.getPrimaryPhoneNumber());
            invitation.setGuardian(existingGuardian);
            invitation.setTargetEmail(email);
            invitation.setTargetPhoneNumber(normalizePhone(existingGuardian.getPrimaryPhoneNumber()));
            invitation.setExpiresAt(null);
            invitation.setStatus(InvitationStatusEnum.AWAITING_APPROVAL);
        }

        invitationRepository.save(invitation);
        return new GuardianInvitationRegistrationResponse(
                existingGuardian.getId(),
                invitation.getStatus(),
                invitation.getStatus() == InvitationStatusEnum.AWAITING_APPROVAL);
    }

    @Transactional
    public List<PendingGuardianInvitationResponse> listPending() {
        User driver = requireCurrentDriver();
        List<GuardianInvitation> invitations = invitationRepository.findActiveByTransportAndStatus(
                driver.getTransport().getId(),
                InvitationStatusEnum.AWAITING_APPROVAL);

        invitations.forEach(this::expireIfNeeded);
        return invitations.stream()
                .filter(invitation -> invitation.getStatus() == InvitationStatusEnum.AWAITING_APPROVAL)
                .map(invitation -> toPendingResponse(invitation, driver))
                .toList();
    }

    @Transactional
    public void approve(UUID invitationId, GuardianInvitationApprovalRequest request) {
        User driver = requireCurrentDriver();
        GuardianInvitation invitation = findInvitationForUpdate(invitationId, driver.getTransport().getId());
        if (invitation.getStatus() != InvitationStatusEnum.AWAITING_APPROVAL
                || (invitation.getExpiresAt() != null
                        && !invitation.getExpiresAt().isAfter(Instant.now()))) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Guardian registration is not awaiting approval");
        }

        Guardian pendingGuardian = invitation.getGuardian();
        if (pendingGuardian == null || pendingGuardian.getUser() == null) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Guardian registration is incomplete");
        }

        Guardian approvedGuardian = request.preRegistrationGuardianId() == null
                ? pendingGuardian
                : transferAccountToPreRegistration(invitation, pendingGuardian, request.preRegistrationGuardianId(), driver);

        Set<UUID> studentIds = new HashSet<>(request.studentIds());
        if (studentIds.size() != request.studentIds().size()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Student IDs must be unique");
        }
        for (UUID studentId : studentIds) {
            Student student = studentService.findByIdForCurrentTransport(studentId);
            linkGuardianToStudent(approvedGuardian, student);
        }

        invitation.setGuardian(approvedGuardian);
        invitation.setStatus(InvitationStatusEnum.COMPLETED);
        invitationRepository.save(invitation);
    }

    @Transactional
    public void cancel(UUID invitationId) {
        User driver = requireCurrentDriver();
        GuardianInvitation invitation = findInvitationForUpdate(invitationId, driver.getTransport().getId());
        if (invitation.getStatus() != InvitationStatusEnum.PENDING) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT, "Only invitations that have not been accepted can be cancelled");
        }
        invitation.setStatus(InvitationStatusEnum.CANCELLED);
        invitationRepository.save(invitation);
    }

    private Guardian transferAccountToPreRegistration(
            GuardianInvitation invitation,
            Guardian pendingGuardian,
            UUID candidateId,
            User driver) {
        List<GuardianCandidateResponse> candidates = findCandidates(invitation, driver).stream()
                .map(candidate -> new GuardianCandidateResponse(
                        candidate.getId(),
                        candidate.getName(),
                        candidate.getPrimaryPhoneNumber(),
                        candidate.getEmail()))
                .toList();
        boolean isCandidate = candidates.stream().anyMatch(candidate -> candidate.guardianId().equals(candidateId));
        if (!isCandidate) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, "Selected pre-registration is not a contact match for this request");
        }

        Guardian candidate = guardianRepository.findById(candidateId)
                .filter(Guardian::isActive)
                .orElseThrow(() -> new EntityNotFoundException("Pre-registered guardian was not found"));
        if (candidate.getUser() != null && candidate.getUser().getRole() != UserRole.DRIVER) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Pre-registered guardian already has an account");
        }

        User account = pendingGuardian.getUser();
        pendingGuardian.setUser(null);
        pendingGuardian.setActive(false);
        guardianRepository.saveAndFlush(pendingGuardian);
        candidate.setUser(account);
        candidate.setName(pendingGuardian.getName());
        candidate.setEmail(account.getEmail());
        candidate.setPrimaryPhoneNumber(pendingGuardian.getPrimaryPhoneNumber());
        candidate.setSecondaryPhoneNumber(pendingGuardian.getSecondaryPhoneNumber());
        candidate.setAddress(pendingGuardian.getAddress());
        return guardianRepository.save(candidate);
    }

    private PendingGuardianInvitationResponse toPendingResponse(GuardianInvitation invitation, User driver) {
        Guardian guardian = invitation.getGuardian();
        return new PendingGuardianInvitationResponse(
                invitation.getId(),
                GuardianMapper.toResponse(guardian),
                invitation.getCreatedAt(),
                findCandidates(invitation, driver).stream()
                        .map(candidate -> new GuardianCandidateResponse(
                                candidate.getId(),
                                candidate.getName(),
                                candidate.getPrimaryPhoneNumber(),
                                candidate.getEmail()))
                        .toList());
    }

    private List<Guardian> findCandidates(GuardianInvitation invitation, User driver) {
        if (invitation.getTargetEmail() == null || invitation.getTargetPhoneNumber() == null) {
            return List.of();
        }
        String targetEmail = normalizeEmail(invitation.getTargetEmail());
        String targetPhone = normalizePhone(invitation.getTargetPhoneNumber());
        return guardianRepository.findByEmailAndOwnerOrTransport(
                        targetEmail,
                        driver.getId(),
                        driver.getTransport().getId())
                .stream()
                .filter(candidate -> !candidate.getId().equals(
                        invitation.getGuardian() == null ? null : invitation.getGuardian().getId()))
                .filter(candidate -> normalizePhone(candidate.getPrimaryPhoneNumber()).equals(targetPhone))
                .toList();
    }

    private GuardianInvitationResponse toPublicResponse(GuardianInvitation invitation) {
        return new GuardianInvitationResponse(
                invitation.getId(),
                invitation.getType(),
                invitation.getStatus(),
                invitation.getExpiresAt(),
                invitation.getStudent() == null ? null : invitation.getStudent().getName());
    }

    private void expireIfNeeded(GuardianInvitation invitation) {
        if ((invitation.getStatus() == InvitationStatusEnum.PENDING
                || invitation.getStatus() == InvitationStatusEnum.AWAITING_APPROVAL)
                && invitation.getExpiresAt() != null
                && !invitation.getExpiresAt().isAfter(Instant.now())) {
            invitation.setStatus(InvitationStatusEnum.EXPIRED);
            invitationRepository.save(invitation);
        }
    }

    private Guardian findGuardianForStudent(UUID guardianId, UUID studentId, UUID transportId) {
        return guardianRepository.findByStudentIdAndTransportId(studentId, transportId).stream()
                .filter(guardian -> guardian.getId().equals(guardianId))
                .findFirst()
                .orElseThrow(() -> new EntityNotFoundException(
                        "Guardian is not linked to the selected student"));
    }

    private void validateSpecificInvitation(GuardianInvitation invitation, Guardian guardian) {
        Student student = invitation.getStudent();
        if (student == null
                || !student.isActive()
                || !student.getTransport().getId().equals(invitation.getTransport().getId())
                || !guardianStudentRepository.existsActiveLink(guardian.getId(), student.getId())) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT, "The invited student or guardian link is no longer active");
        }
    }

    private void linkGuardianToStudent(Guardian guardian, Student student) {
        GuardianStudent link = guardianStudentRepository
                .findByGuardianIdAndStudentId(guardian.getId(), student.getId())
                .orElseGet(() -> {
                    GuardianStudent newLink = new GuardianStudent();
                    newLink.setGuardian(guardian);
                    newLink.setStudent(student);
                    return newLink;
                });
        link.setActive(true);
        link.setDeletedAt(null);
        guardianStudentRepository.save(link);
    }

    private GuardianInvitation findInvitationForUpdate(UUID id, UUID transportId) {
        return invitationRepository.findActiveByIdAndTransportForUpdate(id, transportId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Invitation not found"));
    }

    private User requireCurrentDriver() {
        User user = userRepository.findById(currentUserService.getCurrentUserId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User not found"));
        if (!user.isActive()
                || user.getRole() != UserRole.DRIVER
                || user.getTransport() == null) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Only the owning driver can perform this operation");
        }
        UUID tokenTransportId = currentUserService.getCurrentTransportId();
        if (!user.getTransport().getId().equals(tokenTransportId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Only the owning driver can perform this operation");
        }
        return user;
    }

    private User createGuardianUser(String name, String email, String phoneNumber, String password) {
        User user = new User();
        user.setName(name);
        user.setEmail(email);
        user.setPasswordHash(passwordHasher.hash(password));
        user.setRole(UserRole.GUARDIAN);
        user.setPrimaryPhoneNumber(phoneNumber);
        return userRepository.save(user);
    }

    private void validateTargetContacts(
            GuardianInvitation invitation,
            String email,
            String requestedPhoneNumber) {
        if (invitation.getTargetEmail() != null
                && !normalizeEmail(invitation.getTargetEmail()).equals(email)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Email does not match the invitation");
        }
        if (invitation.getTargetPhoneNumber() != null
                && requestedPhoneNumber != null
                && !normalizePhone(invitation.getTargetPhoneNumber())
                        .equals(normalizePhone(requestedPhoneNumber))) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Phone number does not match the invitation");
        }
    }

    private void ensureContactMatches(String requested, String registered, String contactName) {
        if (requested == null || requested.isBlank()) {
            return;
        }
        String normalizedRequest = contactName.equals("email")
                ? normalizeEmail(requested)
                : normalizePhone(requested);
        String normalizedRegistered = contactName.equals("email")
                ? normalizeEmail(registered)
                : normalizePhone(registered);
        if (!normalizedRequest.equals(normalizedRegistered)) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, "Invitation " + contactName + " must match the pre-registration");
        }
    }

    private String normalizeEmail(String email) {
        return email == null || email.isBlank() ? null : email.trim().toLowerCase(Locale.ROOT);
    }

    private String normalizePhone(String phoneNumber) {
        if (phoneNumber == null || phoneNumber.isBlank()) {
            return null;
        }
        return phoneNumber.replaceAll("\\D", "");
    }

    private void validatePhoneNumber(String phoneNumber) {
        int digitCount = normalizePhone(phoneNumber).length();
        if (digitCount < 8 || digitCount > 15) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Phone number must contain 8 to 15 digits");
        }
    }

    private String generateToken() {
        byte[] bytes = new byte[32];
        SECURE_RANDOM.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }
}
