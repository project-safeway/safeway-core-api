package com.safeway.tech.facade;

import com.safeway.tech.api.dto.student.StudentRequest;
import com.safeway.tech.api.dto.student.SchoolResponse;
import com.safeway.tech.domain.models.Student;
import com.safeway.tech.service.mappers.StudentMapper;
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
    public SchoolResponse buscarPorId(UUID alunoId) {
        Student student = alunoService.buscarPorId(alunoId);
        return StudentMapper.toResponse(student);
    }

    @CachePut(cacheNames = "alunos", key = "#result.id() + ':' + @currentUserService.getCurrentUserId()")
    @Transactional
    public SchoolResponse criarAluno(StudentRequest request) {
        Student student = alunoService.criarAluno(request);
        return StudentMapper.toResponse(student);
    }

    @CachePut(cacheNames = "alunos", key = "#alunoId + ':' + @currentUserService.getCurrentUserId()")
    @Transactional
    public SchoolResponse atualizarAluno(UUID alunoId, StudentRequest request) {
        Student student = alunoService.atualizarAluno(alunoId, request);
        return StudentMapper.toResponse(student);
    }

    @CacheEvict(cacheNames = "alunos", key = "#alunoId + ':' + @currentUserService.getCurrentUserId()", beforeInvocation = true)
    public void deletarAluno(UUID alunoId) {
        alunoService.deletarAluno(alunoId);
    }
}
