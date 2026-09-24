package com.safeway.tech.service.services;

import com.safeway.tech.domain.enums.AttendanceStatusEnum;
import com.safeway.tech.domain.enums.PresenceStatusEnum;
import com.safeway.tech.domain.models.Student;
import com.safeway.tech.domain.models.Attendance;
import com.safeway.tech.domain.models.AttendanceStudent;
import com.safeway.tech.repository.ChamadaAlunoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ChamadaAlunoService {

    private final ChamadaAlunoRepository chamadaAlunoRepository;
    private final AlunoService alunoService;
    private final ChamadaService chamadaService;

    @Transactional
    public void registrarPresenca(Map<UUID, PresenceStatusEnum> presencas, UUID idChamada) {
        Attendance attendance = chamadaService.buscarPorId(idChamada);

        if (!AttendanceStatusEnum.IN_PROGRESS.equals(attendance.getStatus())) {
            throw new RuntimeException("Chamada não está em andamento");
        }

        for (Map.Entry<UUID, PresenceStatusEnum> entry : presencas.entrySet()) {
            UUID idAluno = entry.getKey();
            PresenceStatusEnum status = entry.getValue();

            Student student = alunoService.buscarPorId(idAluno);

            AttendanceStudent attendanceStudent = chamadaAlunoRepository
                    .findByChamadaAndAluno(attendance, student)
                    .orElseGet(() -> {
                        AttendanceStudent ca = new AttendanceStudent();
                        ca.setAttendance(attendance);
                        ca.setStudent(student);
                        return ca;
                    });

            attendanceStudent.setPresenca(status);
            attendanceStudent.setData(LocalDateTime.now());

            chamadaAlunoRepository.save(attendanceStudent);
        }
    }

}
