package com.safeway.tech.service.services;

import com.safeway.tech.api.dto.student.StudentRequest;
import com.safeway.tech.domain.models.School;
import com.safeway.tech.domain.models.Student;
import com.safeway.tech.domain.models.Transport;
import com.safeway.tech.domain.models.User;
import com.safeway.tech.infra.exception.OperationNotAllowedException;
import com.safeway.tech.infra.exception.StudentNotFoundException;
import com.safeway.tech.infra.messaging.publishers.EventPublisher;
import com.safeway.tech.repository.StudentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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

    public Student findById(UUID studentId) {
        UUID userId = currentUserService.getCurrentUserId();
        return studentRepository.findByIdAndUserId(studentId, userId)
                .orElseThrow(() -> new StudentNotFoundException("Student não encontrado"));
    }

    @Transactional
    public Student create(StudentRequest request) {
        UUID userId = currentUserService.getCurrentUserId();
        User user = userService.findById(userId);

        if (!user.isActive()) {
            throw new OperationNotAllowedException("O usuário não possúi permissão para realizar esta operação");
        }

        Student student = new Student();
        applyData(student, request);

        School school = schoolService.findById(request.schoolId());

        student.setSchool(school);

        Transport transport = transportService.getTransport();

        student.setTransport(transport);

        student = studentRepository.save(student);

        eventPublisher.publicarAlunoCriado(student, userId);
        return student;
    }

    @Transactional
    public Student update(UUID studentId, StudentRequest request) {
        UUID userId = currentUserService.getCurrentUserId();
        User user = userService.findById(userId);

        if (!user.isActive()) {
            throw new OperationNotAllowedException("O usuário não possúi permissão para realizar esta operação");
        }

        Student student = findById(studentId);
        applyData(student, request);

        School school = schoolService.findById(request.schoolId());

        student.setSchool(school);

        student = studentRepository.save(student);

        eventPublisher.publicarAlunoAtualizado(student, userId);
        return student;
    }

    @Transactional
    public void delete(UUID studentId) {
        Student student = findById(studentId);
        student.setActive(false);
        studentRepository.save(student);
        eventPublisher.publicarAlunoInativado(student, currentUserService.getCurrentUserId());
    }



    public List<Student> batchFindById(List<UUID> ids) {
        UUID userId = currentUserService.getCurrentUserId();
        return studentRepository.findByIdInAndUserId(ids, userId);
    }

    public List<Student> findAllActive() {
        UUID userId = currentUserService.getCurrentUserId();
        return studentRepository.findAllByActiveTrueAndUserId(userId);
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
