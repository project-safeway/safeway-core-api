package com.safeway.tech.api.controllers;

import com.safeway.tech.api.dto.student.StudentFeignResponse;
import com.safeway.tech.api.dto.student.StudentRequest;
import com.safeway.tech.api.dto.student.StudentResponse;
import com.safeway.tech.api.dto.guardian.GuardianResponse;
import com.safeway.tech.domain.models.Student;
import com.safeway.tech.service.mappers.StudentMapper;
import com.safeway.tech.service.mappers.GuardianMapper;
import com.safeway.tech.service.services.StudentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/students")
@RequiredArgsConstructor
public class StudentController {

    private final StudentService studentService;

    @PostMapping
    public ResponseEntity<StudentResponse> create(
            @RequestBody @Valid StudentRequest request
    ) {
        StudentResponse response = StudentMapper.toResponse(studentService.create(request));
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    public ResponseEntity<List<StudentResponse>> findAll() {
        List<StudentResponse> response = studentService.findAllForCurrentTransport().stream()
                .map(StudentMapper::toResponse)
                .toList();
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{studentId}")
    public ResponseEntity<StudentResponse> findById(@PathVariable UUID studentId) {
        StudentResponse response = StudentMapper.toResponse(studentService.findByIdForCurrentTransport(studentId));
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @PutMapping("/{studentId}")
    public ResponseEntity<StudentResponse> update(
            @PathVariable UUID studentId,
            @RequestBody @Valid StudentRequest request
    ) {
        StudentResponse response = StudentMapper.toResponse(
                studentService.updateForCurrentTransport(studentId, request));
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @DeleteMapping("/{studentId}")
    public ResponseEntity<Void> delete(@PathVariable UUID studentId) {
        studentService.deleteForCurrentTransport(studentId);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

    @GetMapping("/{studentId}/guardians")
    public ResponseEntity<List<GuardianResponse>> findGuardians(@PathVariable UUID studentId) {
        List<GuardianResponse> response = studentService.findGuardians(studentId).stream()
                .map(GuardianMapper::toResponse)
                .toList();
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{studentId}/guardians/{guardianId}")
    public ResponseEntity<Void> addGuardian(@PathVariable UUID studentId, @PathVariable UUID guardianId) {
        studentService.addGuardian(studentId, guardianId);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{studentId}/guardians/{guardianId}")
    public ResponseEntity<Void> removeGuardian(@PathVariable UUID studentId, @PathVariable UUID guardianId) {
        studentService.removeGuardian(studentId, guardianId);
        return ResponseEntity.noContent().build();
    }

    /*

        Methods used by the Feign client.

     */

    @GetMapping("/feign/{studentId}")
    public ResponseEntity<StudentFeignResponse> findByIdFeign(@PathVariable UUID studentId) {
        Student student = studentService.findById(studentId);
        StudentFeignResponse response = StudentMapper.toFeignResponse(student);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @GetMapping("/ativos")
    public ResponseEntity<List<StudentFeignResponse>> findAllActive() {
        List<Student> students = studentService.findAllActive();
        List<StudentFeignResponse> response = students.stream().map(StudentMapper::toFeignResponse).toList();
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @PostMapping("/lote")
    public ResponseEntity<List<StudentFeignResponse>> batchFindById(@RequestBody List<UUID> ids) {
        List<Student> students = studentService.batchFindById(ids);
        List<StudentFeignResponse> response = students.stream().map(StudentMapper::toFeignResponse).toList();
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }
}
