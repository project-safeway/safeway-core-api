package com.safeway.tech.facade;

import com.safeway.tech.api.dto.aluno.AlunoRequest;
import com.safeway.tech.api.dto.aluno.AlunoResponse;
import com.safeway.tech.domain.models.Student;
import com.safeway.tech.service.mappers.AlunoMapper;
import com.safeway.tech.service.services.AlunoService;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Component
@RequiredArgsConstructor
public class AlunoFacade {

    private final AlunoService alunoService;

    @Cacheable(cacheNames = "alunos", key = "#alunoId + ':' + @currentUserService.getCurrentUserId()")
    @Transactional(readOnly = true)
    public AlunoResponse buscarPorId(UUID alunoId) {
        Student student = alunoService.buscarPorId(alunoId);
        return AlunoMapper.toResponse(student);
    }

    @CachePut(cacheNames = "alunos", key = "#result.id() + ':' + @currentUserService.getCurrentUserId()")
    @Transactional
    public AlunoResponse criarAluno(AlunoRequest request) {
        Student student = alunoService.criarAluno(request);
        return AlunoMapper.toResponse(student);
    }

    @CachePut(cacheNames = "alunos", key = "#alunoId + ':' + @currentUserService.getCurrentUserId()")
    @Transactional
    public AlunoResponse atualizarAluno(UUID alunoId, AlunoRequest request) {
        Student student = alunoService.atualizarAluno(alunoId, request);
        return AlunoMapper.toResponse(student);
    }

    @CacheEvict(cacheNames = "alunos", key = "#alunoId + ':' + @currentUserService.getCurrentUserId()", beforeInvocation = true)
    public void deletarAluno(UUID alunoId) {
        alunoService.deletarAluno(alunoId);
    }
}
