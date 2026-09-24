package com.safeway.tech.api.controllers;

import com.safeway.tech.api.dto.endereco.EnderecoResponse;
import com.safeway.tech.api.dto.escola.EscolaRequest;
import com.safeway.tech.api.dto.escola.EscolaResponse;
import com.safeway.tech.domain.models.Address;
import com.safeway.tech.domain.models.School;
import com.safeway.tech.service.mappers.EnderecoMapper;
import com.safeway.tech.service.mappers.EscolaMapper;
import com.safeway.tech.service.services.EscolaService;
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
@RequestMapping("/escolas")
@RequiredArgsConstructor
public class EscolaController {

    private final EscolaService escolaService;

    @PostMapping
    public ResponseEntity<EscolaResponse> cadastrarEscola(
            @Valid @RequestBody EscolaRequest request) {

        School school = escolaService.cadastrarEscola(request);
        EscolaResponse response = EscolaMapper.toResponse(school);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    public ResponseEntity<List<EscolaResponse>> listarEscolasComAlunos() {
        List<School> schools = escolaService.listarEscolasComAlunos();
        List<EscolaResponse> response = schools.stream().map(EscolaMapper::toResponse).toList();
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<EscolaResponse> buscarEscolaPorId(@PathVariable UUID id) {
        School school = escolaService.buscarPorId(id);
        EscolaResponse response = EscolaMapper.toResponse(school);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @GetMapping("/{id}/endereco")
    public ResponseEntity<EnderecoResponse> buscarEnderecoEscola(@PathVariable UUID id) {
        Address address = escolaService.buscarEnderecoDaEscola(id);
        EnderecoResponse response = EnderecoMapper.toResponse(address);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @PutMapping("/{id}")
    public ResponseEntity<EscolaResponse> atualizarEscola(
            @PathVariable UUID id,
            @Valid @RequestBody EscolaRequest request) {
        School school = escolaService.atualizarEscola(id, request);
        EscolaResponse response = EscolaMapper.toResponse(school);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletarEscola(@PathVariable UUID id) {
        escolaService.desativar(id);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }
}
