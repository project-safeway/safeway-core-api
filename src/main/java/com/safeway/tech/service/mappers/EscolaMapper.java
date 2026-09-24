package com.safeway.tech.service.mappers;

import com.safeway.tech.api.dto.escola.EscolaResponse;
import com.safeway.tech.api.dto.escola.EscolaResumeResponse;
import com.safeway.tech.domain.models.Student;
import com.safeway.tech.domain.models.School;

import java.util.Collections;
import java.util.List;

public class EscolaMapper {

    public static EscolaResponse toResponse(School school) {
        List<Student> students = school.getAlunos() != null ? school.getAlunos() : Collections.emptyList();

        return new EscolaResponse(
                school.getId(),
                school.getNome(),
                school.getNivelEnsino(),
                EnderecoMapper.toResponse(school.getAddress()),
                students.stream().map(AlunoMapper::toResumeResponse).toList()
        );
    }

    public static EscolaResumeResponse toResumeResponse(School school) {
        return new EscolaResumeResponse(
                school.getNome(),
                school.getNivelEnsino(),
                EnderecoMapper.toResponse(school.getAddress())
        );
    }

}
