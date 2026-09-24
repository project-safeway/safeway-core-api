package com.safeway.tech.service.mappers;

import com.safeway.tech.api.dto.chamada.ChamadaAlunoResponse;
import com.safeway.tech.api.dto.chamada.ChamadaResponse;
import com.safeway.tech.domain.models.Attendance;
import com.safeway.tech.domain.models.AttendanceStudent;

import java.util.ArrayList;
import java.util.List;

public class ChamadaMapper {

    public static ChamadaResponse toResponse(Attendance attendance) {
        List<ChamadaAlunoResponse> alunosResponse = new ArrayList<>();
        if (attendance.getAlunos() != null && !attendance.getAlunos().isEmpty()) {
            alunosResponse = attendance.getAlunos().stream()
                    .map(ChamadaMapper::toAlunoResponse)
                    .toList();
        }

        return new ChamadaResponse(
                attendance.getId(),
                ItinerarioMapper.toResponse(attendance.getRoute()),
                attendance.getStatus(),
                alunosResponse
        );
    }

    public static ChamadaAlunoResponse toAlunoResponse(AttendanceStudent attendanceStudent) {
        return new ChamadaAlunoResponse(
                AlunoMapper.toResponse(attendanceStudent.getStudent()),
                attendanceStudent.getPresenca(),
                attendanceStudent.getData()
        );
    }

}
