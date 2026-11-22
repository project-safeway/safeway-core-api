package com.safeway.tech.service.services;

import com.safeway.tech.domain.models.Student;
import com.safeway.tech.infra.messaging.publishers.EventPublisher;
import com.safeway.tech.repository.GuardianRepository;
import com.safeway.tech.repository.GuardianStudentRepository;
import com.safeway.tech.repository.StudentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.CacheManager;
import org.springframework.cache.concurrent.ConcurrentMapCacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = StudentServiceCacheTest.CacheTestConfiguration.class)
class StudentServiceCacheTest {

    @Autowired
    private StudentService studentService;
    @Autowired
    private StudentRepository studentRepository;
    @Autowired
    private CurrentUserService currentUserService;
    @Autowired
    private CacheManager cacheManager;

    @BeforeEach
    void clearCachesAndMocks() {
        cacheManager.getCacheNames().forEach(name -> cacheManager.getCache(name).clear());
        reset(studentRepository, currentUserService);
    }

    @Test
    void scopesStudentCacheByAuthenticatedUser() {
        UUID studentId = UUID.randomUUID();
        UUID firstUserId = UUID.randomUUID();
        UUID secondUserId = UUID.randomUUID();
        Student firstStudent = new Student();
        Student secondStudent = new Student();

        AtomicReference<UUID> currentUser = new AtomicReference<>(firstUserId);
        when(currentUserService.getCurrentUserId()).thenAnswer(invocation -> currentUser.get());
        when(studentRepository.findByIdAndUserId(studentId, firstUserId)).thenReturn(Optional.of(firstStudent));
        when(studentRepository.findByIdAndUserId(studentId, secondUserId)).thenReturn(Optional.of(secondStudent));

        assertSame(firstStudent, studentService.findById(studentId));
        assertSame(firstStudent, studentService.findById(studentId));
        currentUser.set(secondUserId);
        assertSame(secondStudent, studentService.findById(studentId));

        verify(studentRepository, times(1)).findByIdAndUserId(studentId, firstUserId);
        verify(studentRepository, times(1)).findByIdAndUserId(studentId, secondUserId);
    }

    @Test
    void scopesTransportListCacheByCurrentTransport() {
        UUID firstTransportId = UUID.randomUUID();
        UUID secondTransportId = UUID.randomUUID();
        List<Student> firstTransportStudents = List.of(new Student());
        List<Student> secondTransportStudents = List.of(new Student());

        AtomicReference<UUID> currentTransport = new AtomicReference<>(firstTransportId);
        when(currentUserService.getCurrentTransportId()).thenAnswer(invocation -> currentTransport.get());
        when(studentRepository.findAllByTransportIdAndActiveTrue(firstTransportId))
                .thenReturn(firstTransportStudents);
        when(studentRepository.findAllByTransportIdAndActiveTrue(secondTransportId))
                .thenReturn(secondTransportStudents);

        assertSame(firstTransportStudents, studentService.findAllForCurrentTransport());
        assertSame(firstTransportStudents, studentService.findAllForCurrentTransport());
        currentTransport.set(secondTransportId);
        assertSame(secondTransportStudents, studentService.findAllForCurrentTransport());
        assertSame(secondTransportStudents, studentService.findAllForCurrentTransport());

        verify(studentRepository, times(1)).findAllByTransportIdAndActiveTrue(firstTransportId);
        verify(studentRepository, times(1)).findAllByTransportIdAndActiveTrue(secondTransportId);
    }

    @Configuration(proxyBeanMethods = false)
    @EnableCaching
    static class CacheTestConfiguration {

        @Bean
        CacheManager cacheManager() {
            return new ConcurrentMapCacheManager(
                    "studentsByUser",
                    "studentsByTransport",
                    "studentsByTransportList",
                    "studentGuardians");
        }

        @Bean
        CurrentUserService currentUserService() {
            return mock(CurrentUserService.class);
        }

        @Bean
        StudentRepository studentRepository() {
            return mock(StudentRepository.class);
        }

        @Bean
        StudentService studentService(
                StudentRepository studentRepository,
                CurrentUserService currentUserService,
                SchoolService schoolService,
                UserService userService,
                TransportService transportService,
                EventPublisher eventPublisher,
                GuardianService guardianService,
                GuardianStudentRepository guardianStudentRepository,
                GuardianRepository guardianRepository) {
            return new StudentService(
                    studentRepository,
                    schoolService,
                    userService,
                    transportService,
                    eventPublisher,
                    currentUserService,
                    guardianService,
                    guardianStudentRepository,
                    guardianRepository);
        }

        @Bean
        SchoolService schoolService() {
            return mock(SchoolService.class);
        }

        @Bean
        UserService userService() {
            return mock(UserService.class);
        }

        @Bean
        TransportService transportService() {
            return mock(TransportService.class);
        }

        @Bean
        EventPublisher eventPublisher() {
            return mock(EventPublisher.class);
        }

        @Bean
        GuardianService guardianService() {
            return mock(GuardianService.class);
        }

        @Bean
        GuardianStudentRepository guardianStudentRepository() {
            return mock(GuardianStudentRepository.class);
        }

        @Bean
        GuardianRepository guardianRepository() {
            return mock(GuardianRepository.class);
        }
    }
}
