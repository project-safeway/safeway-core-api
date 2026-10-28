package com.safeway.tech.service.services;

import com.safeway.tech.api.dto.student.StudentFeignResponse;
import com.safeway.tech.domain.enums.UserRole;
import com.safeway.tech.domain.models.Transport;
import com.safeway.tech.domain.models.User;
import com.safeway.tech.infra.exception.EntityNotFoundException;
import com.safeway.tech.repository.StudentRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class StudentFinancialServiceTest {

    @Mock
    private StudentRepository studentRepository;
    @Mock
    private UserService userService;
    @Mock
    private CurrentUserService currentUserService;
    @InjectMocks
    private StudentFinancialService service;

    private final UUID transportId = UUID.randomUUID();
    private final UUID driverId = UUID.randomUUID();
    private final UUID studentId = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(service, "serviceClientId", "financial-client");
    }

    @AfterEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void findsStudentThroughDriverTransportWithoutGuardianLink() {
        authenticateDriver(transportId, UserRole.DRIVER);
        StudentFeignResponse response = response();
        when(currentUserService.getCurrentTransportId()).thenReturn(transportId);
        when(studentRepository.findFinancialStudentsByIds(List.of(studentId), transportId))
                .thenReturn(List.of(response));

        assertEquals(response, service.findById(studentId));
    }

    @Test
    void rejectsStudentOutsideDriverTransport() {
        authenticateDriver(transportId, UserRole.DRIVER);
        when(currentUserService.getCurrentTransportId()).thenReturn(transportId);
        when(studentRepository.findFinancialStudentsByIds(List.of(studentId), transportId)).thenReturn(List.of());

        assertThrows(EntityNotFoundException.class, () -> service.findById(studentId));
    }

    @Test
    void scopesBatchAndInteractiveListToDriverTransport() {
        authenticateDriver(transportId, UserRole.DRIVER);
        when(currentUserService.getCurrentTransportId()).thenReturn(transportId);
        when(studentRepository.findFinancialStudents(transportId)).thenReturn(List.of(response()));
        when(studentRepository.findFinancialStudentsByIds(List.of(studentId), transportId))
                .thenReturn(List.of(response()));

        assertEquals(List.of(response()), service.findAllActive());
        assertEquals(List.of(response()), service.batchFindById(List.of(studentId)));
    }

    @Test
    void allowsConfiguredServiceToListAllTransportsWithoutTransportClaim() {
        authenticate(Jwt.withTokenValue("token").header("alg", "RS256")
                .subject("financial-client").claim("tokenType", "service").build());
        when(studentRepository.findFinancialStudents(null)).thenReturn(List.of(response()));

        assertEquals(List.of(response()), service.findAllActive());
        verifyNoInteractions(userService, currentUserService);
    }

    @Test
    void rejectsServiceWithUnexpectedSubject() {
        authenticate(Jwt.withTokenValue("token").header("alg", "RS256")
                .subject("other-client").claim("tokenType", "service").build());

        assertThrows(ResponseStatusException.class, () -> service.findAllActive());
        verifyNoInteractions(studentRepository, userService);
    }

    @Test
    void rejectsGuardianEvenWithTransportClaim() {
        authenticateDriver(transportId, UserRole.GUARDIAN);

        assertThrows(ResponseStatusException.class, () -> service.findById(studentId));
        verifyNoInteractions(studentRepository);
    }

    @Test
    void rejectsStaleTransportClaim() {
        authenticateDriver(UUID.randomUUID(), UserRole.DRIVER);

        assertThrows(ResponseStatusException.class, () -> service.findAllActive());
        verifyNoInteractions(studentRepository);
    }

    @Test
    void rejectsUnauthenticatedRequests() {
        SecurityContextHolder.clearContext();

        assertThrows(ResponseStatusException.class, () -> service.findAllActive());
        verifyNoInteractions(studentRepository, userService);
    }

    private void authenticateDriver(UUID claimedTransport, UserRole role) {
        Transport transport = new Transport();
        transport.setId(transportId);
        User user = new User();
        user.setId(driverId);
        user.setRole(role);
        user.setTransport(transport);
        when(userService.findCurrentUser()).thenReturn(user);
        authenticate(Jwt.withTokenValue("token").header("alg", "RS256").subject(driverId.toString())
                .claim("transport", claimedTransport.toString()).claim("role", role.name()).build());
    }

    private void authenticate(Jwt jwt) {
        SecurityContextHolder.getContext().setAuthentication(new JwtAuthenticationToken(jwt, List.of()));
    }

    private StudentFeignResponse response() {
        return new StudentFeignResponse(studentId, "Student", 500.0, 15, true, transportId, driverId);
    }
}
