package com.safeway.tech.service.services;

import com.safeway.tech.api.dto.address.AddressRequest;
import com.safeway.tech.api.dto.guardian.GuardianInvitationAcceptRequest;
import com.safeway.tech.api.dto.guardian.GuardianInvitationApprovalRequest;
import com.safeway.tech.api.dto.guardian.GuardianInvitationRegistrationResponse;
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
import com.safeway.tech.repository.GuardianInvitationRepository;
import com.safeway.tech.repository.GuardianRepository;
import com.safeway.tech.repository.GuardianStudentRepository;
import com.safeway.tech.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GuardianInvitationServiceTest {

    @Mock
    private GuardianInvitationRepository invitationRepository;
    @Mock
    private GuardianRepository guardianRepository;
    @Mock
    private GuardianStudentRepository guardianStudentRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private StudentService studentService;
    @Mock
    private AddressService addressService;
    @Mock
    private CurrentUserService currentUserService;
    @Mock
    private PasswordHasherPort passwordHasher;
    @Mock
    private PasswordVerifierPort passwordVerifier;

    @InjectMocks
    private GuardianInvitationService invitationService;

    @Test
    void genericAcceptanceCreatesGuardianAccountAndAwaitsDriverApproval() {
        GuardianInvitation invitation = new GuardianInvitation();
        invitation.setToken("token");
        invitation.setType(InvitationTypeEnum.GENERIC);
        invitation.setStatus(InvitationStatusEnum.PENDING);
        invitation.setExpiresAt(Instant.now().plusSeconds(3600));

        when(invitationRepository.findActiveByTokenForUpdate("token")).thenReturn(Optional.of(invitation));
        when(userRepository.findByEmail("guardian@example.com")).thenReturn(Optional.empty());
        when(passwordHasher.hash("secure-password")).thenReturn("hashed-password");
        when(userRepository.save(any(User.class))).thenAnswer(call -> call.getArgument(0));
        when(addressService.create(any(AddressRequest.class))).thenReturn(new Address());
        when(guardianRepository.save(any(Guardian.class))).thenAnswer(call -> call.getArgument(0));
        when(invitationRepository.save(any(GuardianInvitation.class))).thenAnswer(call -> call.getArgument(0));

        GuardianInvitationRegistrationResponse response = invitationService.accept(
                "token",
                new GuardianInvitationAcceptRequest(
                        "guardian@example.com",
                        "secure-password",
                        "Guardian Name",
                        "5511999999999",
                        null,
                        new AddressRequest("Street", "10", null, "District", "City", "SP", "00000000", "HOME", true)));

        assertEquals(InvitationStatusEnum.AWAITING_APPROVAL, response.status());
        assertTrue(response.requiresApproval());
        Guardian guardian = invitation.getGuardian();
        assertNotNull(guardian);
        assertEquals("guardian@example.com", guardian.getUser().getEmail());
        assertEquals(UserRole.GUARDIAN, guardian.getUser().getRole());
        assertNull(guardian.getUser().getTransport());
        assertEquals(InvitationStatusEnum.AWAITING_APPROVAL, invitation.getStatus());
        assertNull(invitation.getExpiresAt());
    }

    @Test
    void studentSpecificAcceptanceUsesPreRegistrationAndCompletesImmediately() {
        GuardianInvitation invitation = new GuardianInvitation();
        invitation.setToken("student-token");
        invitation.setType(InvitationTypeEnum.STUDENT_SPECIFIC);
        invitation.setStatus(InvitationStatusEnum.PENDING);
        invitation.setExpiresAt(Instant.now().plusSeconds(3600));
        Student student = new Student();
        student.setActive(true);
        Transport transport = new Transport();
        transport.setId(UUID.randomUUID());
        student.setTransport(transport);
        invitation.setTransport(transport);
        invitation.setStudent(student);

        Guardian guardian = new Guardian();
        guardian.setName("Pre-registered Guardian");
        guardian.setPrimaryPhoneNumber("5511999999999");
        guardian.setEmail("guardian@example.com");
        User driver = new User();
        driver.setRole(UserRole.DRIVER);
        guardian.setUser(driver);
        invitation.setGuardian(guardian);

        when(invitationRepository.findActiveByTokenForUpdate("student-token")).thenReturn(Optional.of(invitation));
        when(userRepository.findByEmail("guardian@example.com")).thenReturn(Optional.empty());
        when(guardianStudentRepository.existsActiveLink(null, null)).thenReturn(true);
        when(passwordHasher.hash("secure-password")).thenReturn("hashed-password");
        when(userRepository.save(any(User.class))).thenAnswer(call -> call.getArgument(0));
        when(guardianRepository.save(any(Guardian.class))).thenAnswer(call -> call.getArgument(0));
        when(invitationRepository.save(any(GuardianInvitation.class))).thenAnswer(call -> call.getArgument(0));

        GuardianInvitationRegistrationResponse response = invitationService.accept(
                "student-token",
                new GuardianInvitationAcceptRequest(
                        "guardian@example.com",
                        "secure-password",
                        null,
                        null,
                        null,
                        null));

        assertEquals(InvitationStatusEnum.COMPLETED, response.status());
        assertFalse(response.requiresApproval());
        assertEquals(UserRole.GUARDIAN, guardian.getUser().getRole());
        assertEquals("guardian@example.com", guardian.getUser().getEmail());
        assertEquals(InvitationStatusEnum.COMPLETED, invitation.getStatus());
    }

    @Test
    void existingGuardianCanUseGenericInvitationForAdditionalStudentLinks() {
        GuardianInvitation invitation = new GuardianInvitation();
        invitation.setToken("existing-guardian-token");
        invitation.setType(InvitationTypeEnum.GENERIC);
        invitation.setStatus(InvitationStatusEnum.PENDING);
        invitation.setExpiresAt(Instant.now().plusSeconds(3600));

        User user = new User();
        user.setId(UUID.randomUUID());
        user.setEmail("guardian@example.com");
        user.setPasswordHash("stored-hash");
        user.setRole(UserRole.GUARDIAN);
        Guardian guardian = new Guardian();
        guardian.setId(UUID.randomUUID());
        guardian.setUser(user);
        guardian.setPrimaryPhoneNumber("5511999999999");
        guardian.setEmail(user.getEmail());

        when(invitationRepository.findActiveByTokenForUpdate("existing-guardian-token"))
                .thenReturn(Optional.of(invitation));
        when(userRepository.findByEmail("guardian@example.com")).thenReturn(Optional.of(user));
        when(passwordVerifier.matches("secure-password", "stored-hash")).thenReturn(true);
        when(guardianRepository.findByUserIdAndActiveTrue(user.getId())).thenReturn(Optional.of(guardian));
        when(invitationRepository.save(any(GuardianInvitation.class))).thenAnswer(call -> call.getArgument(0));

        GuardianInvitationRegistrationResponse response = invitationService.accept(
                "existing-guardian-token",
                new GuardianInvitationAcceptRequest(
                        "guardian@example.com", "secure-password", null, null, null, null));

        assertEquals(guardian.getId(), response.guardianId());
        assertEquals(InvitationStatusEnum.AWAITING_APPROVAL, response.status());
        assertTrue(response.requiresApproval());
        assertEquals(guardian, invitation.getGuardian());
        assertNull(invitation.getExpiresAt());
    }

    @Test
    void approvalCreatesOnlyDriverSelectedStudentLinks() {
        UUID driverId = UUID.randomUUID();
        UUID transportId = UUID.randomUUID();
        UUID invitationId = UUID.randomUUID();
        UUID studentId = UUID.randomUUID();
        UUID guardianId = UUID.randomUUID();

        User driver = new User();
        driver.setId(driverId);
        driver.setRole(UserRole.DRIVER);
        driver.setTransport(new Transport());
        driver.getTransport().setId(transportId);

        User guardianUser = new User();
        guardianUser.setRole(UserRole.GUARDIAN);
        Guardian guardian = new Guardian();
        guardian.setId(guardianId);
        guardian.setUser(guardianUser);

        GuardianInvitation invitation = new GuardianInvitation();
        invitation.setId(invitationId);
        invitation.setStatus(InvitationStatusEnum.AWAITING_APPROVAL);
        invitation.setGuardian(guardian);

        Student student = new Student();
        student.setId(studentId);

        when(currentUserService.getCurrentUserId()).thenReturn(driverId);
        when(currentUserService.getCurrentTransportId()).thenReturn(transportId);
        when(userRepository.findById(driverId)).thenReturn(Optional.of(driver));
        when(invitationRepository.findActiveByIdAndTransportForUpdate(invitationId, transportId))
                .thenReturn(Optional.of(invitation));
        when(studentService.findByIdForCurrentTransport(studentId)).thenReturn(student);
        when(guardianStudentRepository.findByGuardianIdAndStudentId(guardianId, studentId))
                .thenReturn(Optional.empty());
        when(guardianStudentRepository.save(any())).thenAnswer(call -> call.getArgument(0));
        when(invitationRepository.save(any(GuardianInvitation.class))).thenAnswer(call -> call.getArgument(0));

        invitationService.approve(
                invitationId,
                new GuardianInvitationApprovalRequest(List.of(studentId), null));

        assertEquals(InvitationStatusEnum.COMPLETED, invitation.getStatus());
        org.mockito.ArgumentCaptor<GuardianStudent> linkCaptor =
                org.mockito.ArgumentCaptor.forClass(GuardianStudent.class);
        org.mockito.Mockito.verify(guardianStudentRepository).save(linkCaptor.capture());
        assertEquals(guardian, linkCaptor.getValue().getGuardian());
        assertEquals(student, linkCaptor.getValue().getStudent());
        assertTrue(linkCaptor.getValue().isActive());
    }
}
