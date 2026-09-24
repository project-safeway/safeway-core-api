package com.safeway.tech.api.controllers;

import com.safeway.tech.api.dto.student.StudentFeignResponse;
import com.safeway.tech.api.dto.student.StudentRequest;
import com.safeway.tech.api.dto.student.SchoolResponse;
import com.safeway.tech.api.dto.address.AddressResponse;
import com.safeway.tech.domain.models.Student;
import com.safeway.tech.domain.models.Address;
import com.safeway.tech.facade.AlunoFacade;
import com.safeway.tech.service.mappers.StudentMapper;
import com.safeway.tech.service.mappers.AddressMapper;
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
    public ResponseEntity<SchoolResponse> cadastrarAlunoCompleto(
            @RequestBody @Valid StudentRequest request
    ) {
        SchoolResponse response = alunoFacade.criarAluno(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{alunoId}/enderecos")
    public ResponseEntity<List<AddressResponse>> listarEnderecosDoAluno(
            @PathVariable UUID alunoId
    ) {
        List<Address> addresses = enderecoService.listarEnderecosDisponiveis(alunoId);
        List<AddressResponse> response = addresses.stream().map(AddressMapper::toResponse).toList();
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @GetMapping("/{alunoId}")
    public ResponseEntity<SchoolResponse> listarDadosAluno(@PathVariable UUID alunoId) {
        SchoolResponse response = alunoFacade.buscarPorId(alunoId);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @PutMapping("/{alunoId}")
    public ResponseEntity<SchoolResponse> atualizarAluno(
            @PathVariable UUID alunoId,
            @RequestBody @Valid StudentRequest request
    ) {
        SchoolResponse response = alunoFacade.atualizarAluno(alunoId, request);
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
    public ResponseEntity<StudentFeignResponse> buscarAlunoPorId(@PathVariable UUID alunoId) {
        Student student = alunoService.buscarPorId(alunoId);
        StudentFeignResponse response = StudentMapper.toFeignResponse(student);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @GetMapping("/ativos")
    public ResponseEntity<List<StudentFeignResponse>> buscarTodosAtivos() {
        List<Student> students = alunoService.buscarTodosAtivos();
        List<StudentFeignResponse> response = students.stream().map(StudentMapper::toFeignResponse).toList();
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @PostMapping("/lote")
    public ResponseEntity<List<StudentFeignResponse>> buscarPorIdEmLote(@RequestBody List<UUID> ids) {
        List<Student> students = alunoService.buscarPorIdEmLote(ids);
        List<StudentFeignResponse> response = students.stream().map(StudentMapper::toFeignResponse).toList();
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }
}
