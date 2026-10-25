package com.safeway.tech.service.mappers;

import com.safeway.tech.api.dto.student.StudentResponse;
import com.safeway.tech.domain.models.Guardian;
import com.safeway.tech.domain.models.Student;

import java.util.List;

public class StudentMapper {

    public static StudentResponse toResponse(Student student, List<Guardian> guardians) {
        return new StudentResponse(
                student.getId(),
                student.getName(),
                student.getProfessor(),
                student.getBirthdate(),
                student.getGrade(),
                student.getClassroom(),
                SchoolMapper.toResumeResponse(student.getSchool()),
                student.getMonthlyFee(),
                student.getDueDate(),
                guardians.stream().map(GuardianMapper::toResponse).toList()
        );
    }
}
