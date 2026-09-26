package com.safeway.tech.service.services;

import com.safeway.tech.domain.enums.AttendanceStatusEnum;
import com.safeway.tech.domain.enums.PresenceStatusEnum;
import com.safeway.tech.domain.models.Attendance;
import com.safeway.tech.domain.models.AttendanceStudent;
import com.safeway.tech.domain.models.Student;
import com.safeway.tech.repository.AttendanceStudentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AttendanceStudentService {

    private final AttendanceStudentRepository attendanceStudentRepository;
    private final StudentService studentService;
    private final AttendanceService attendanceService;

    @Transactional
    public void registerAttendance(Map<UUID, PresenceStatusEnum> presencas, UUID idChamada) {
        Attendance attendance = attendanceService.findById(idChamada);

        if (!AttendanceStatusEnum.IN_PROGRESS.equals(attendance.getStatus())) {
            throw new RuntimeException("Chamada não está em andamento");
        }

        for (Map.Entry<UUID, PresenceStatusEnum> entry : presencas.entrySet()) {
            UUID studentId = entry.getKey();
            PresenceStatusEnum status = entry.getValue();

            Student student = studentService.findById(studentId);

            AttendanceStudent attendanceStudent = attendanceStudentRepository
                    .findByChamadaAndAluno(attendance, student)
                    .orElseGet(() -> {
                        AttendanceStudent newAttendanceStudent = new AttendanceStudent();
                        newAttendanceStudent.setAttendance(attendance);
                        newAttendanceStudent.setStudent(student);
                        return newAttendanceStudent;
                    });

            attendanceStudent.setPresence(status);
            attendanceStudent.setDate(LocalDateTime.now());

            attendanceStudentRepository.save(attendanceStudent);
        }
    }

}
