package com.safeway.tech.service.services;

import com.safeway.tech.domain.enums.AttendanceStatusEnum;
import com.safeway.tech.domain.enums.StatusPresencaEnum;
import com.safeway.tech.domain.models.Student;
import com.safeway.tech.domain.models.Attendance;
import com.safeway.tech.domain.models.ChamadaAluno;
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
    public void registrarPresenca(Map<UUID, StatusPresencaEnum> presencas, UUID idChamada) {
        Attendance attendance = chamadaService.buscarPorId(idChamada);

        if (!AttendanceStatusEnum.IN_PROGRESS.equals(attendance.getStatus())) {
            throw new RuntimeException("Chamada não está em andamento");
        }

        for (Map.Entry<UUID, StatusPresencaEnum> entry : presencas.entrySet()) {
            UUID idAluno = entry.getKey();
            StatusPresencaEnum status = entry.getValue();

            Student student = alunoService.buscarPorId(idAluno);

            ChamadaAluno chamadaAluno = chamadaAlunoRepository
                    .findByChamadaAndAluno(attendance, student)
                    .orElseGet(() -> {
                        ChamadaAluno ca = new ChamadaAluno();
                        ca.setAttendance(attendance);
                        ca.setStudent(student);
                        return ca;
                    });

            chamadaAluno.setPresenca(status);
            chamadaAluno.setData(LocalDateTime.now());

            chamadaAlunoRepository.save(chamadaAluno);
        }
    }

}
