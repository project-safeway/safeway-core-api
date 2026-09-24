package com.safeway.tech.service.mappers;

import com.safeway.tech.api.dto.student.StudentFeignResponse;
import com.safeway.tech.api.dto.student.SchoolResponse;
import com.safeway.tech.api.dto.transport.StudentTransportResponse;
import com.safeway.tech.domain.models.Student;

public class StudentMapper {

    public static SchoolResponse toResponse(Student student) {
        return new SchoolResponse(
                student.getId(),
                student.getName(),
                student.getProfessor(),
                student.getBirthdate(),
                student.getGrade(),
                student.getClassroom(),
                SchoolMapper.toResumeResponse(student.getSchool()),
                student.getMonthlyFee(),
                student.getDueDate()
        );
    }

    public static StudentTransportResponse toTransportResponse(Student student) {
        return new StudentTransportResponse(
                student.getId(),
                student.getName(),
                student.getSchool().getName()
        );
    }

    public static StudentFeignResponse toFeignResponse(Student student) {
        return new StudentFeignResponse(
                student.getId(),
                student.getName(),
                student.getMonthlyFee(),
                student.getDueDate(),
                student.isActive()
        );
    }

}
