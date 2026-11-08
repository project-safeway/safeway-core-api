package com.safeway.tech.service.services;

import com.safeway.tech.api.dto.student.StudentFeignResponse;
import com.safeway.tech.domain.enums.UserRole;
import com.safeway.tech.domain.models.User;
import com.safeway.tech.infra.exception.EntityNotFoundException;
import com.safeway.tech.repository.StudentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class StudentFinancialService {

    private final StudentRepository studentRepository;
    private final UserService userService;
    private final CurrentUserService currentUserService;

    @Value("${auth.v2.service-token.client-id:safeway-core}")
    private String serviceClientId;

    public StudentFeignResponse findById(UUID studentId) {
        return studentRepository.findFinancialStudentsByIds(List.of(studentId), requireDriverTransport())
                .stream().findFirst()
                .orElseThrow(() -> new EntityNotFoundException("Student was not found"));
    }

    public List<StudentFeignResponse> findAllActive() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated()
                && authentication.getPrincipal() instanceof Jwt jwt
                && "service".equals(jwt.getClaimAsString("tokenType"))
                && serviceClientId.equals(jwt.getSubject())) {
            return studentRepository.findFinancialStudents(null);
        }
        return studentRepository.findFinancialStudents(requireDriverTransport());
    }

    public List<StudentFeignResponse> batchFindById(List<UUID> ids) {
        UUID transportId = requireDriverTransport();
        if (ids.isEmpty()) {
            return List.of();
        }
        return studentRepository.findFinancialStudentsByIds(ids, transportId);
    }

    private UUID requireDriverTransport() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()
                || !(authentication.getPrincipal() instanceof Jwt jwt)
                || "service".equals(jwt.getClaimAsString("tokenType"))) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Driver authentication is required");
        }
        User user = userService.findCurrentUser();
        if (!user.isActive() || user.getRole() != UserRole.DRIVER || user.getTransport() == null
                || !user.getTransport().isActive()
                || !user.getTransport().getId().toString().equals(jwt.getClaimAsString("transport"))) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Driver transport is not available");
        }
        return currentUserService.getCurrentTransportId();
    }
}
