package com.safeway.tech.service.mappers;

import com.safeway.tech.api.dto.aluno.AlunoFeignResponse;
import com.safeway.tech.api.dto.aluno.AlunoResponse;
import com.safeway.tech.api.dto.aluno.AlunoResumeResponse;
import com.safeway.tech.api.dto.transporte.AlunoTransporteResponse;
import com.safeway.tech.domain.models.Student;

public class AlunoMapper {

    public static AlunoResponse toResponse(Student student) {
        return new AlunoResponse(
                student.getId(),
                student.getNome(),
                student.getProfessor(),
                student.getDtNascimento(),
                student.getSerie(),
                student.getSala(),
                EscolaMapper.toResumeResponse(student.getSchool()),
                student.getResponsaveis().stream().map(ResponsavelMapper::toResponse).toList(),
                student.getValorMensalidade(),
                student.getDiaVencimento()
        );
    }

    public static AlunoResumeResponse toResumeResponse(Student student) {
        return new AlunoResumeResponse(
                student.getId(),
                student.getNome(),
                student.getSerie(),
                student.getSala(),
                student.getAtivo()
        );
    }

    public static AlunoTransporteResponse toTransporteResponse(Student student) {
        return new AlunoTransporteResponse(
                student.getId(),
                student.getNome(),
                student.getSchool().getNome(),
                student.getResponsaveis().getFirst().getNome()
        );
    }

    public static AlunoFeignResponse toFeignResponse(Student student) {
        return new AlunoFeignResponse(
                student.getId(),
                student.getNome(),
                student.getValorMensalidade(),
                student.getDiaVencimento(),
                student.getAtivo(),
                new AlunoFeignResponse.UsuarioResponse(student.getUsuario().getId())
        );
    }

}
