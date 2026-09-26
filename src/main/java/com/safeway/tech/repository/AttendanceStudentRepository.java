package com.safeway.tech.repository;

import com.safeway.tech.domain.models.Student;
import com.safeway.tech.domain.models.Attendance;
import com.safeway.tech.domain.models.AttendanceStudent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.UUID;

public interface AttendanceStudentRepository extends JpaRepository<AttendanceStudent, UUID> {

    @Query("SELECT ca FROM AttendanceStudent ca WHERE ca.attendance = :attendance AND ca.student = :student")
    Optional<AttendanceStudent> findByAttendanceAndStudent(
            @Param("attendance") Attendance attendance,
            @Param("student") Student student);
}
