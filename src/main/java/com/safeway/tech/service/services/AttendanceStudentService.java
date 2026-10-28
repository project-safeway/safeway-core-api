package com.safeway.tech.service.services;

import com.safeway.tech.domain.enums.AttendanceStatusEnum;
import com.safeway.tech.domain.enums.PresenceStatusEnum;
import com.safeway.tech.domain.models.Attendance;
import com.safeway.tech.domain.models.AttendanceStudent;
import com.safeway.tech.domain.models.Student;
import com.safeway.tech.repository.AttendanceStudentRepository;
import com.safeway.tech.repository.RouteStudentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AttendanceStudentService {

    private final AttendanceStudentRepository attendanceStudentRepository;
    private final StudentService studentService;
    private final AttendanceService attendanceService;
    private final RouteStudentRepository routeStudentRepository;

    @Transactional(readOnly = true)
    public List<AttendanceStudent> findByAttendanceId(UUID attendanceId) {
        return attendanceStudentRepository.findByAttendanceId(attendanceId);
    }

    @Transactional
    public void registerAttendance(Map<UUID, PresenceStatusEnum> presences, UUID attendanceId) {
        Attendance attendance = attendanceService.findById(attendanceId);

        if (!AttendanceStatusEnum.IN_PROGRESS.equals(attendance.getStatus())) {
            throw new RuntimeException("Attendance is not in progress");
        }

        Map<UUID, Student> studentsById = new java.util.HashMap<>();
        for (Map.Entry<UUID, PresenceStatusEnum> entry : presences.entrySet()) {
            UUID studentId = entry.getKey();
            if (studentId == null || entry.getValue() == null) {
                throw new IllegalArgumentException("Each attendance record requires a student and presence status");
            }

            Student student = studentService.findByIdForCurrentTransport(studentId);
            if (!routeStudentRepository.existsActiveLink(attendance.getRoute().getId(), studentId)) {
                throw new IllegalArgumentException("Student is not assigned to this route");
            }
            studentsById.put(studentId, student);
        }

        for (Map.Entry<UUID, PresenceStatusEnum> entry : presences.entrySet()) {
            Student student = studentsById.get(entry.getKey());

            AttendanceStudent attendanceStudent = attendanceStudentRepository
                    .findByAttendanceAndStudent(attendance, student)
                    .orElseGet(() -> {
                        AttendanceStudent newAttendanceStudent = new AttendanceStudent();
                        newAttendanceStudent.setAttendance(attendance);
                        newAttendanceStudent.setStudent(student);
                        return newAttendanceStudent;
                    });

            attendanceStudent.setPresence(entry.getValue());
            attendanceStudent.setDate(LocalDateTime.now());

            attendanceStudentRepository.save(attendanceStudent);
        }
    }

}
