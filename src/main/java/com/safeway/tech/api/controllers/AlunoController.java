package com.safeway.tech.api.controllers;

import com.safeway.tech.api.dto.aluno.AlunoFeignResponse;
import com.safeway.tech.api.dto.aluno.AlunoRequest;
import com.safeway.tech.api.dto.aluno.AlunoResponse;
import com.safeway.tech.api.dto.endereco.EnderecoResponse;
import com.safeway.tech.domain.models.Student;
import com.safeway.tech.domain.models.Address;
import com.safeway.tech.facade.AlunoFacade;
import com.safeway.tech.service.mappers.AlunoMapper;
import com.safeway.tech.service.mappers.EnderecoMapper;
import com.safeway.tech.service.services.AlunoService;
import com.safeway.tech.service.services.EnderecoService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/alunos")
@RequiredArgsConstructor
public class AlunoController {

    private final AlunoService alunoService;
    private final AlunoFacade alunoFacade;
    private final EnderecoService enderecoService;

    @PostMapping
    public ResponseEntity<AlunoResponse> cadastrarAlunoCompleto(
            @RequestBody @Valid AlunoRequest request
    ) {
        AlunoResponse response = alunoFacade.criarAluno(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{alunoId}/enderecos")
    public ResponseEntity<List<EnderecoResponse>> listarEnderecosDoAluno(
            @PathVariable UUID alunoId
    ) {
        List<Address> addresses = enderecoService.listarEnderecosDisponiveis(alunoId);
        List<EnderecoResponse> response = addresses.stream().map(EnderecoMapper::toResponse).toList();
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @GetMapping("/{alunoId}")
    public ResponseEntity<AlunoResponse> listarDadosAluno(@PathVariable UUID alunoId) {
        AlunoResponse response = alunoFacade.buscarPorId(alunoId);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @PutMapping("/{alunoId}")
    public ResponseEntity<AlunoResponse> atualizarAluno(
            @PathVariable UUID alunoId,
            @RequestBody @Valid AlunoRequest request
    ) {
        AlunoResponse response = alunoFacade.atualizarAluno(alunoId, request);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @DeleteMapping("/{alunoId}")
    public ResponseEntity<Void> deletarAluno(@PathVariable UUID alunoId) {
        alunoFacade.deletarAluno(alunoId);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

    /*

        MÉTODOS USADOS NO FEIGN CLIENT

     */

    @GetMapping("/feign/{alunoId}")
    public ResponseEntity<AlunoFeignResponse> buscarAlunoPorId(@PathVariable UUID alunoId) {
        Student student = alunoService.buscarPorId(alunoId);
        AlunoFeignResponse response = AlunoMapper.toFeignResponse(student);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @GetMapping("/ativos")
    public ResponseEntity<List<AlunoFeignResponse>> buscarTodosAtivos() {
        List<Student> students = alunoService.buscarTodosAtivos();
        List<AlunoFeignResponse> response = students.stream().map(AlunoMapper::toFeignResponse).toList();
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @PostMapping("/lote")
    public ResponseEntity<List<AlunoFeignResponse>> buscarPorIdEmLote(@RequestBody List<UUID> ids) {
        List<Student> students = alunoService.buscarPorIdEmLote(ids);
        List<AlunoFeignResponse> response = students.stream().map(AlunoMapper::toFeignResponse).toList();
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }
}
