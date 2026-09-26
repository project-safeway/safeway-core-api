package com.safeway.tech.api.controllers;

import com.safeway.tech.api.dto.school.SchoolRequest;
import com.safeway.tech.api.dto.school.SchoolResponse;
import com.safeway.tech.domain.models.School;
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

        School school = schoolService.createSchool(request);
        SchoolResponse response = SchoolMapper.toResponse(school);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    public ResponseEntity<List<SchoolResponse>> listarEscolasComAlunos() {
        List<School> schools = schoolService.findAllSchools();
        List<SchoolResponse> response = schools.stream().map(SchoolMapper::toResponse).toList();
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<SchoolResponse> buscarEscolaPorId(@PathVariable UUID id) {
        School school = schoolService.findById(id);
        SchoolResponse response = SchoolMapper.toResponse(school);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @PutMapping("/{id}")
    public ResponseEntity<SchoolResponse> atualizarEscola(
            @PathVariable UUID id,
            @Valid @RequestBody SchoolRequest request) {
        School school = schoolService.updateSchool(id, request);
        SchoolResponse response = SchoolMapper.toResponse(school);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletarEscola(@PathVariable UUID id) {
        schoolService.deactivate(id);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }
}
