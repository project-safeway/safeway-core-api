package com.safeway.tech.api.controllers;

import com.safeway.tech.api.dto.itinerario.AlunoComLocalizacao;
import com.safeway.tech.api.dto.itinerario.ItinerarioAlunoRequest;
import com.safeway.tech.api.dto.itinerario.ItinerarioEscolaRequest;
import com.safeway.tech.api.dto.itinerario.ItinerarioRequest;
import com.safeway.tech.api.dto.itinerario.ItinerarioResponse;
import com.safeway.tech.api.dto.itinerario.ItinerarioUpdateRequest;
import com.safeway.tech.domain.models.Route;
import com.safeway.tech.service.mappers.ItinerarioMapper;
import com.safeway.tech.service.services.ItinerarioAlunoService;
import com.safeway.tech.service.services.ItinerarioEscolaService;
import com.safeway.tech.service.services.ItinerarioService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.apache.coyote.BadRequestException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/itinerarios")
@RequiredArgsConstructor
public class ItinerarioController {

    private final ItinerarioService itinerarioService;
    private final ItinerarioAlunoService itinerarioAlunoService;
    private final ItinerarioEscolaService itinerarioEscolaService;

    @PostMapping
    public ResponseEntity<ItinerarioResponse> criar(
            @Valid @RequestBody ItinerarioRequest request
    ) {
        Route route = itinerarioService.criar(request);
        ItinerarioResponse response = ItinerarioMapper.toResponse(route);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @GetMapping
    public ResponseEntity<List<ItinerarioResponse>> listarTodos() {
        List<Route> routes = itinerarioService.listarTodos();
        List<ItinerarioResponse> response = routes.stream().map(ItinerarioMapper::toResponse).toList();
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ItinerarioResponse> buscarPorId(@PathVariable UUID id) {
        Route route = itinerarioService.buscarPorId(id);
        ItinerarioResponse response = ItinerarioMapper.toResponse(route);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @GetMapping("/{id}/alunos")
    public ResponseEntity<List<AlunoComLocalizacao>> buscarAlunosDoItinerario(@PathVariable UUID id) {
        List<AlunoComLocalizacao> alunos = itinerarioAlunoService.buscarAlunosComLocalizacao(id);
        return ResponseEntity.status(HttpStatus.OK).body(alunos);
    }

    @PutMapping("/{id}")
    public ResponseEntity<ItinerarioResponse> atualizar(
            @PathVariable UUID id,
            @Valid @RequestBody ItinerarioUpdateRequest request
    ) {
        Route route = itinerarioService.atualizar(id, request);
        ItinerarioResponse response = ItinerarioMapper.toResponse(route);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> desativar(@PathVariable UUID id) {
        itinerarioService.desativar(id);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

    @PostMapping("/{id}/alunos")
    public ResponseEntity<Void> adicionarAluno(
            @PathVariable UUID id,
            @Valid @RequestBody ItinerarioAlunoRequest request
    ) {
        itinerarioAlunoService.adicionarAluno(id, request);
        return ResponseEntity.status(HttpStatus.OK).build();
    }

    @DeleteMapping("/{id}/alunos/{alunoId}")
    public ResponseEntity<Void> removerAluno(
            @PathVariable UUID id,
            @PathVariable String alunoId
    ) {
        if (alunoId == null || alunoId.isBlank() || "undefined".equalsIgnoreCase(alunoId)) {
            return ResponseEntity.badRequest().build();
        }
        UUID alunoIdLong;
        try {
            alunoIdLong = UUID.fromString(alunoId);
        } catch (NumberFormatException ex) {
            return ResponseEntity.badRequest().build();
        }

        itinerarioAlunoService.removerAluno(id, alunoIdLong);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

    @PatchMapping("/{id}/alunos/ordem")
    public ResponseEntity<Void> reordenar(
            @PathVariable UUID id,
            @RequestBody List<UUID> novaOrdemIds
    ) {
        itinerarioAlunoService.reordenar(id, novaOrdemIds);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

    @PostMapping("/{id}/escolas")
    public ResponseEntity<Void> adicionarEscola(
            @PathVariable UUID id,
            @Valid @RequestBody ItinerarioEscolaRequest request
    ) throws BadRequestException {
        itinerarioEscolaService.adicionarEscola(id, request);
        return ResponseEntity.status(HttpStatus.OK).build();
    }

    @DeleteMapping("/{id}/escolas/{escolaId}")
    public ResponseEntity<Void> removerEscola(
            @PathVariable UUID id,
            @PathVariable UUID escolaId
    ) {
        itinerarioEscolaService.removerEscola(id, escolaId);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

    @PatchMapping("/{id}/escolas/ordem")
    public ResponseEntity<Void> reordenarEscolas(
            @PathVariable UUID id,
            @RequestBody List<UUID> novaOrdemEscolaIds
    ) {
        itinerarioEscolaService.reordenar(id, novaOrdemEscolaIds);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }
}
