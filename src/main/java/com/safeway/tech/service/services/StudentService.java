package com.safeway.tech.service.services;

import com.safeway.tech.api.dto.student.StudentRequest;
import com.safeway.tech.api.dto.guardian.GuardianRequest;
import com.safeway.tech.domain.models.Student;
import com.safeway.tech.domain.models.School;
import com.safeway.tech.domain.models.Guardian;
import com.safeway.tech.domain.models.Transport;
import com.safeway.tech.domain.models.User;
import com.safeway.tech.infra.exception.AlunoNotFoundException;
import com.safeway.tech.infra.exception.OperationNotAllowedException;
import com.safeway.tech.infra.messaging.publishers.EventPublisher;
import com.safeway.tech.repository.AlunoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class StudentService {

    private final AlunoRepository alunoRepository;
    private final SchoolService schoolService;
    private final UserService userService;
    private final ResponsavelService responsavelService;
    private final TransportService transportService;
    private final EventPublisher eventPublisher;
    private final CurrentUserService currentUserService;

    public Student buscarPorId(UUID alunoId) {
        UUID userId = currentUserService.getCurrentUserId();
        return alunoRepository.findByIdAndUsuarioId(alunoId, userId)
                .orElseThrow(() -> new AlunoNotFoundException("Student não encontrado"));
    }

    @Transactional
    public Student criarAluno(StudentRequest request) {
        UUID userId = currentUserService.getCurrentUserId();
        User user = userService.buscarPorId(userId);

        if (!user.getAtivo()) {
            throw new OperationNotAllowedException("O usuário não possúi permissão para realizar esta operação");
        }

        UUID transporteId = currentUserService.getCurrentTransporteId();
        Transport transport = transportService.buscarPorId(transporteId);

        School school = schoolService.buscarPorId(request.escolaId());

        Student student = new Student();
        aplicarDados(student, request);
        student.setSchool(school);
        student.setUsuario(user);
        student.setTransport(transport);

        for (GuardianRequest guardianRequest : request.responsaveis()) {
            Guardian guardian = responsavelService
                    .buscarPorCpfAndUsuario(guardianRequest.cpf(), userId)
                    .orElseGet(() -> responsavelService.criarResponsavel(guardianRequest));

            student.adicionarResponsavel(guardian);
        }

        student = alunoRepository.save(student);

        eventPublisher.publicarAlunoCriado(student);
        return student;
    }

    @Transactional
    public Student atualizarAluno(UUID alunoId, StudentRequest request) {
        UUID userId = currentUserService.getCurrentUserId();
        User user = userService.buscarPorId(userId);

        if (!user.getAtivo()) {
            throw new OperationNotAllowedException("O usuário não possúi permissão para realizar esta operação");
        }

        School school = schoolService.buscarPorId(request.escolaId());

        Student student = buscarPorId(alunoId);
        aplicarDados(student, request);
        student.setSchool(school);

        atualizarResponsaveis(student, request.responsaveis(), userId);
        student = alunoRepository.save(student);

        eventPublisher.publicarAlunoAtualizado(student);
        return student;
    }

    @Transactional
    public void deletarAluno(UUID alunoId) {
        Student student = buscarPorId(alunoId);
        student.setAtivo(false);
        alunoRepository.save(student);
        eventPublisher.publicarAlunoInativado(student);
    }

    public List<Student> buscarPorIdEmLote(List<UUID> ids) {
        UUID userId = currentUserService.getCurrentUserId();
        return alunoRepository.findByIdInAndIdUsuario(ids, userId);
    }

    public List<Student> buscarTodosAtivos() {
        UUID userId = currentUserService.getCurrentUserId();
        return alunoRepository.findByAtivoTrueAndIdUsuario(userId);
    }

    private void aplicarDados(Student student, StudentRequest request) {
        student.setNome(request.nome());
        student.setProfessor(request.professor());
        student.setDtNascimento(request.dtNascimento());
        student.setSerie(request.serie());
        student.setSala(request.sala());
        student.setValorMensalidade(request.valorMensalidade());
        student.setDiaVencimento(request.diaVencimento());
    }

    private void atualizarResponsaveis(Student student, List<GuardianRequest> requests, UUID userId) {
        List<Guardian> novosResponsaveis = new ArrayList<>();

        for (GuardianRequest request : requests) {
            Guardian guardian = responsavelService.buscarPorCpfAndUsuario(request.cpf(), userId)
                    .orElseGet(() -> responsavelService.criarResponsavel(request));
            novosResponsaveis.add(guardian);
        }

        for (Guardian guardianAtual : new ArrayList<>(student.getResponsaveis())) {
            if (!novosResponsaveis.contains(guardianAtual)) {
                student.removerResponsavel(guardianAtual);
            }
        }

        for (Guardian guardian : novosResponsaveis) {
            student.adicionarResponsavel(guardian);
        }
    }
}
