package com.safeway.tech.api.controllers;

import com.safeway.tech.api.dto.guardian.GuardianRequest;
import com.safeway.tech.api.dto.guardian.GuardianResponse;
import com.safeway.tech.domain.models.Guardian;
import com.safeway.tech.service.mappers.GuardianMapper;
import com.safeway.tech.service.services.ResponsavelService;
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
@RequestMapping("/responsavel")
@RequiredArgsConstructor
public class GuardianController {

    private final ResponsavelService responsavelService;

    @PostMapping
    public ResponseEntity<GuardianResponse> salvarResponsavel(@RequestBody @Valid GuardianRequest request) {
        Guardian guardian = responsavelService.criarResponsavel(request);
        GuardianResponse response = GuardianMapper.toResponse(guardian);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    public ResponseEntity<List<GuardianResponse>> listarResponsaveis() {
        List<Guardian> responsaveis = responsavelService.listarResponsaveis();
        List<GuardianResponse> response = responsaveis.stream().map(GuardianMapper::toResponse).toList();
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<GuardianResponse> retornarUm(@PathVariable UUID id) {
        Guardian guardian = responsavelService.buscarPorId(id);
        GuardianResponse response = GuardianMapper.toResponse(guardian);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @DeleteMapping("/{idResponsavel}")
    public ResponseEntity<Void> excluir(@PathVariable UUID idResponsavel) {
        responsavelService.desativar(idResponsavel);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

    @PutMapping("/{idResponsavel}")
    public ResponseEntity<GuardianResponse> alterarResponsavel(
            @RequestBody @Valid GuardianRequest novoResponsavel,
            @PathVariable UUID idResponsavel) {
        Guardian guardian = responsavelService.alterarResponsavel(novoResponsavel, idResponsavel);
        GuardianResponse response = GuardianMapper.toResponse(guardian);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }
}
