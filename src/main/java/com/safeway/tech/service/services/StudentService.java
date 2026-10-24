package com.safeway.tech.service.services;

import com.safeway.tech.api.dto.student.StudentRequest;
import com.safeway.tech.domain.models.Guardian;
import com.safeway.tech.domain.models.GuardianStudent;
import com.safeway.tech.domain.models.School;
import com.safeway.tech.domain.models.Student;
import com.safeway.tech.domain.models.Transport;
import com.safeway.tech.domain.models.User;
import com.safeway.tech.infra.exception.OperationNotAllowedException;
import com.safeway.tech.infra.exception.EntityNotFoundException;
import com.safeway.tech.infra.messaging.publishers.EventPublisher;
import com.safeway.tech.repository.StudentRepository;
import com.safeway.tech.repository.GuardianRepository;
import com.safeway.tech.repository.GuardianStudentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class StudentService {

    private final StudentRepository studentRepository;
    private final SchoolService schoolService;
    private final UserService userService;
    private final TransportService transportService;
    private final EventPublisher eventPublisher;
    private final CurrentUserService currentUserService;
    private final GuardianService guardianService;
    private final GuardianStudentRepository guardianStudentRepository;
    private final GuardianRepository guardianRepository;

    public Student findById(UUID studentId) {
        UUID userId = currentUserService.getCurrentUserId();
        return studentRepository.findByIdAndUserId(studentId, userId)
                .orElseThrow(() -> new EntityNotFoundException("Student was not found"));
    }

    @Transactional
    public Student create(StudentRequest request) {
        UUID userId = currentUserService.getCurrentUserId();
        User user = userService.findById(userId);

        if (!user.isActive()) {
            throw new OperationNotAllowedException("User is not allowed to perform this operation");
        }

        Student student = new Student();
        applyData(student, request);

        School school = schoolService.findById(request.schoolId());

        student.setSchool(school);

        Transport transport = transportService.getTransport();

        student.setTransport(transport);

        student = studentRepository.save(student);

        for (var guardianRequest : request.guardian()) {
            Guardian guardian = guardianService.createPreRegistrationGuardian(guardianRequest);
            linkGuardian(guardian, student);
        }

        eventPublisher.publishStudentCreated(student, userId);
        return student;
    }

    @Transactional
    public Student update(UUID studentId, StudentRequest request) {
        UUID userId = currentUserService.getCurrentUserId();
        User user = userService.findById(userId);

        if (!user.isActive()) {
            throw new OperationNotAllowedException("User is not allowed to perform this operation");
        }

        Student student = findById(studentId);
        applyData(student, request);

        School school = schoolService.findById(request.schoolId());

        student.setSchool(school);

        student = studentRepository.save(student);

        eventPublisher.publishStudentUpdated(student, userId);
        return student;
    }

    @Transactional
    public Student updateForCurrentTransport(UUID studentId, StudentRequest request) {
        UUID userId = currentUserService.getCurrentUserId();
        User user = userService.findById(userId);
        if (!user.isActive()) {
            throw new OperationNotAllowedException("User is not allowed to perform this operation");
        }

        Student student = findByIdForCurrentTransport(studentId);
        applyData(student, request);
        student.setSchool(schoolService.findById(request.schoolId()));
        student = studentRepository.save(student);
        eventPublisher.publishStudentUpdated(student, userId);
        return student;
    }

    @Transactional
    public void delete(UUID studentId) {
        Student student = findById(studentId);
        student.setActive(false);
        studentRepository.save(student);
        eventPublisher.publishStudentDeactivated(student, currentUserService.getCurrentUserId());
    }

    @Transactional
    public void deleteForCurrentTransport(UUID studentId) {
        Student student = findByIdForCurrentTransport(studentId);
        student.setActive(false);
        studentRepository.save(student);
        eventPublisher.publishStudentDeactivated(student, currentUserService.getCurrentUserId());
    }



    public List<Student> batchFindById(List<UUID> ids) {
        UUID userId = currentUserService.getCurrentUserId();
        return studentRepository.findByIdInAndUserId(ids, userId);
    }

    public List<Student> findAllActive() {
        UUID userId = currentUserService.getCurrentUserId();
        return studentRepository.findAllByActiveTrueAndUserId(userId);
    }

    public List<Student> findAllForCurrentTransport() {
        UUID transportId = currentUserService.getCurrentTransportId();
        return studentRepository.findAllByTransportIdAndActiveTrue(transportId);
    }

    public Student findByIdForCurrentTransport(UUID studentId) {
        UUID transportId = currentUserService.getCurrentTransportId();
        return studentRepository.findByIdAndTransportIdAndActiveTrue(studentId, transportId)
                .orElseThrow(() -> new EntityNotFoundException("Student was not found"));
    }

    public List<Guardian> findGuardians(UUID studentId) {
        Student student = findByIdForCurrentTransport(studentId);
        return guardianRepository.findByStudentIdAndTransportId(
                student.getId(), student.getTransport().getId());
    }

    @Transactional
    public void addGuardian(UUID studentId, UUID guardianId) {
        Student student = findByIdForCurrentTransport(studentId);
        Guardian guardian = guardianService.findById(guardianId);
        linkGuardian(guardian, student);
    }

    @Transactional
    public void removeGuardian(UUID studentId, UUID guardianId) {
        findByIdForCurrentTransport(studentId);
        GuardianStudent link = guardianStudentRepository.findByGuardianIdAndStudentId(guardianId, studentId)
                .filter(GuardianStudent::isActive)
                .orElseThrow(() -> new EntityNotFoundException("Guardian link was not found"));
        link.setActive(false);
        link.setDeletedAt(Instant.now());
        guardianStudentRepository.save(link);
    }

    private void linkGuardian(Guardian guardian, Student student) {
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

    private void applyData(Student student, StudentRequest request) {
        student.setName(request.name());
        student.setProfessor(request.professor());
        student.setBirthdate(request.birthdate());
        student.setGrade(request.grade());
        student.setClassroom(request.classroom());
        student.setMonthlyFee(request.monthlyFee());
        student.setDueDate(request.dueDate());
    }
}
