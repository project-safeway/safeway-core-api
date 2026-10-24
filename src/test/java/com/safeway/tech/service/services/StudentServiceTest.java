package com.safeway.tech.service.services;

import com.safeway.tech.domain.models.Guardian;
import com.safeway.tech.domain.models.GuardianStudent;
import com.safeway.tech.domain.models.Student;
import com.safeway.tech.domain.models.Transport;
import com.safeway.tech.infra.exception.EntityNotFoundException;
import com.safeway.tech.repository.GuardianRepository;
import com.safeway.tech.repository.GuardianStudentRepository;
import com.safeway.tech.repository.StudentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
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
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class StudentServiceTest {

    @Mock
    private StudentRepository studentRepository;
    @Mock
    private GuardianStudentRepository guardianStudentRepository;
    @Mock
    private GuardianRepository guardianRepository;
    @Mock
    private CurrentUserService currentUserService;
    @Mock
    private GuardianService guardianService;
    @InjectMocks
    private StudentService studentService;

    private final UUID studentId = UUID.randomUUID();
    private final UUID guardianId = UUID.randomUUID();
    private final UUID transportId = UUID.randomUUID();
    private Student student;

    @BeforeEach
    void setUp() {
        Transport transport = new Transport();
        transport.setId(transportId);
        student = new Student();
        student.setId(studentId);
        student.setTransport(transport);
        when(currentUserService.getCurrentTransportId()).thenReturn(transportId);
    }

    @Test
    void linksExistingGuardianWithoutCreatingAnotherRegistration() {
        allowStudent();
        Guardian guardian = allowGuardian();

        studentService.addGuardian(studentId, guardianId);

        ArgumentCaptor<GuardianStudent> captor = ArgumentCaptor.forClass(GuardianStudent.class);
        verify(guardianStudentRepository).save(captor.capture());
        assertSame(student, captor.getValue().getStudent());
        assertSame(guardian, captor.getValue().getGuardian());
        assertTrue(captor.getValue().isActive());
    }

    @Test
    void reusesExistingLinkAndReactivatesIt() {
        allowStudent();
        allowGuardian();
        GuardianStudent link = new GuardianStudent();
        link.setId(UUID.randomUUID());
        link.setActive(false);
        link.setDeletedAt(Instant.now());
        when(guardianStudentRepository.findByGuardianIdAndStudentId(guardianId, studentId))
                .thenReturn(Optional.of(link));

        studentService.addGuardian(studentId, guardianId);
        studentService.addGuardian(studentId, guardianId);

        assertTrue(link.isActive());
        assertNull(link.getDeletedAt());
        verify(guardianStudentRepository, times(2)).save(link);
    }

    @Test
    void rejectsStudentOutsideCurrentTransportBeforeLookingUpGuardian() {
        assertThrows(EntityNotFoundException.class,
                () -> studentService.addGuardian(studentId, guardianId));

        verifyNoInteractions(guardianService, guardianStudentRepository);
    }

    @Test
    void rejectsInaccessibleGuardianWithoutSavingLink() {
        allowStudent();
        when(guardianService.findById(guardianId)).thenThrow(new EntityNotFoundException("Guardian not found"));

        assertThrows(EntityNotFoundException.class,
                () -> studentService.addGuardian(studentId, guardianId));

        verifyNoInteractions(guardianStudentRepository);
    }

    @Test
    void listsGuardiansOnlyAfterCheckingStudentTransport() {
        allowStudent();
        Guardian guardian = new Guardian();
        when(guardianRepository.findByStudentIdAndTransportId(studentId, transportId))
                .thenReturn(List.of(guardian));

        assertEquals(List.of(guardian), studentService.findGuardians(studentId));
    }

    @Test
    void removesOnlyTheLink() {
        allowStudent();
        Guardian guardian = new Guardian();
        GuardianStudent link = new GuardianStudent();
        link.setGuardian(guardian);
        when(guardianStudentRepository.findByGuardianIdAndStudentId(guardianId, studentId))
                .thenReturn(Optional.of(link));

        studentService.removeGuardian(studentId, guardianId);

        assertFalse(link.isActive());
        assertNotNull(link.getDeletedAt());
        assertTrue(guardian.isActive());
        verify(guardianStudentRepository).save(link);
        verifyNoInteractions(guardianService);
    }

    @Test
    void rejectsRemovalOfMissingLink() {
        allowStudent();

        assertThrows(EntityNotFoundException.class,
                () -> studentService.removeGuardian(studentId, guardianId));

        verify(guardianStudentRepository, never()).save(any());
    }

    @Test
    void rejectsRemovalFromStudentOutsideCurrentTransport() {
        assertThrows(EntityNotFoundException.class,
                () -> studentService.removeGuardian(studentId, guardianId));

        verifyNoInteractions(guardianStudentRepository);
    }

    private void allowStudent() {
        when(studentRepository.findByIdAndTransportIdAndActiveTrue(studentId, transportId))
                .thenReturn(Optional.of(student));
    }

    private Guardian allowGuardian() {
        Guardian guardian = new Guardian();
        guardian.setId(guardianId);
        when(guardianService.findById(guardianId)).thenReturn(guardian);
        return guardian;
    }
}
