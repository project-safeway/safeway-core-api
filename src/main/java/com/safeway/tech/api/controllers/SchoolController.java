package com.safeway.tech.api.controllers;

import com.safeway.tech.api.dto.address.AddressResponse;
import com.safeway.tech.api.dto.school.SchoolRequest;
import com.safeway.tech.api.dto.school.SchoolResponse;
import com.safeway.tech.domain.models.Address;
import com.safeway.tech.domain.models.School;
import com.safeway.tech.service.mappers.AddressMapper;
import com.safeway.tech.service.mappers.SchoolMapper;
import com.safeway.tech.service.services.SchoolService;
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
public class SchoolController {

    private final SchoolService schoolService;

    @PostMapping
    public ResponseEntity<SchoolResponse> cadastrarEscola(
            @Valid @RequestBody SchoolRequest request) {

        School school = schoolService.cadastrarEscola(request);
        SchoolResponse response = SchoolMapper.toResponse(school);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    public ResponseEntity<List<SchoolResponse>> listarEscolasComAlunos() {
        List<School> schools = schoolService.listarEscolasComAlunos();
        List<SchoolResponse> response = schools.stream().map(SchoolMapper::toResponse).toList();
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<SchoolResponse> buscarEscolaPorId(@PathVariable UUID id) {
        School school = schoolService.buscarPorId(id);
        SchoolResponse response = SchoolMapper.toResponse(school);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @GetMapping("/{id}/endereco")
    public ResponseEntity<AddressResponse> buscarEnderecoEscola(@PathVariable UUID id) {
        Address address = schoolService.buscarEnderecoDaEscola(id);
        AddressResponse response = AddressMapper.toResponse(address);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @PutMapping("/{id}")
    public ResponseEntity<SchoolResponse> atualizarEscola(
            @PathVariable UUID id,
            @Valid @RequestBody SchoolRequest request) {
        School school = schoolService.atualizarEscola(id, request);
        SchoolResponse response = SchoolMapper.toResponse(school);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletarEscola(@PathVariable UUID id) {
        schoolService.desativar(id);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }
}
