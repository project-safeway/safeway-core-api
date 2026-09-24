package com.safeway.tech.api.controllers;

import com.safeway.tech.api.dto.responsavel.ResponsavelRequest;
import com.safeway.tech.api.dto.responsavel.ResponsavelResponse;
import com.safeway.tech.domain.models.Guardian;
import com.safeway.tech.service.mappers.ResponsavelMapper;
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
public class ResponsavelController {

    private final ResponsavelService responsavelService;

    @PostMapping
    public ResponseEntity<ResponsavelResponse> salvarResponsavel(@RequestBody @Valid ResponsavelRequest request) {
        Guardian guardian = responsavelService.criarResponsavel(request);
        ResponsavelResponse response = ResponsavelMapper.toResponse(guardian);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    public ResponseEntity<List<ResponsavelResponse>> listarResponsaveis() {
        List<Guardian> responsaveis = responsavelService.listarResponsaveis();
        List<ResponsavelResponse> response = responsaveis.stream().map(ResponsavelMapper::toResponse).toList();
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ResponsavelResponse> retornarUm(@PathVariable UUID id) {
        Guardian guardian = responsavelService.buscarPorId(id);
        ResponsavelResponse response = ResponsavelMapper.toResponse(guardian);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @DeleteMapping("/{idResponsavel}")
    public ResponseEntity<Void> excluir(@PathVariable UUID idResponsavel) {
        responsavelService.desativar(idResponsavel);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

    @PutMapping("/{idResponsavel}")
    public ResponseEntity<ResponsavelResponse> alterarResponsavel(
            @RequestBody @Valid ResponsavelRequest novoResponsavel,
            @PathVariable UUID idResponsavel) {
        Guardian guardian = responsavelService.alterarResponsavel(novoResponsavel, idResponsavel);
        ResponsavelResponse response = ResponsavelMapper.toResponse(guardian);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }
}
