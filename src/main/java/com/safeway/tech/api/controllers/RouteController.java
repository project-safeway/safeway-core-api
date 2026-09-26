package com.safeway.tech.api.controllers;

import com.safeway.tech.api.dto.route.StudentWithAddress;
import com.safeway.tech.api.dto.route.RouteStudentRequest;
import com.safeway.tech.api.dto.route.RouteSchoolRequest;
import com.safeway.tech.api.dto.route.RouteRequest;
import com.safeway.tech.api.dto.route.RouteResponse;
import com.safeway.tech.api.dto.route.RouteUpdateRequest;
import com.safeway.tech.domain.models.Route;
import com.safeway.tech.service.mappers.RouteMapper;
import com.safeway.tech.service.services.RouteStudentService;
import com.safeway.tech.service.services.RouteSchoolService;
import com.safeway.tech.service.services.RouteService;
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
public class RouteController {

    private final RouteService routeService;
    private final RouteStudentService routeStudentService;
    private final RouteSchoolService routeSchoolService;

    @PostMapping
    public ResponseEntity<RouteResponse> criar(
            @Valid @RequestBody RouteRequest request
    ) {
        Route route = routeService.createRoute(request);
        RouteResponse response = RouteMapper.toResponse(route);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @GetMapping
    public ResponseEntity<List<RouteResponse>> listarTodos() {
        List<Route> routes = routeService.findAll();
        List<RouteResponse> response = routes.stream().map(RouteMapper::toResponse).toList();
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<RouteResponse> buscarPorId(@PathVariable UUID id) {
        Route route = routeService.findById(id);
        RouteResponse response = RouteMapper.toResponse(route);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @GetMapping("/{id}/alunos")
    public ResponseEntity<List<StudentWithAddress>> buscarAlunosDoItinerario(@PathVariable UUID id) {
        List<StudentWithAddress> alunos = routeStudentService.findStudentWithAddress(id);
        return ResponseEntity.status(HttpStatus.OK).body(alunos);
    }

    @PutMapping("/{id}")
    public ResponseEntity<RouteResponse> atualizar(
            @PathVariable UUID id,
            @Valid @RequestBody RouteUpdateRequest request
    ) {
        Route route = routeService.updateRoute(id, request);
        RouteResponse response = RouteMapper.toResponse(route);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> desativar(@PathVariable UUID id) {
        routeService.deactivate(id);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

    @PostMapping("/{id}/alunos")
    public ResponseEntity<Void> adicionarAluno(
            @PathVariable UUID id,
            @Valid @RequestBody RouteStudentRequest request
    ) {
        routeStudentService.addStudent(id, request);
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

        routeStudentService.removeStudent(id, alunoIdLong);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

    @PatchMapping("/{id}/alunos/ordem")
    public ResponseEntity<Void> reordenar(
            @PathVariable UUID id,
            @RequestBody List<UUID> novaOrdemIds
    ) {
        routeStudentService.reorder(id, novaOrdemIds);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

    @PostMapping("/{id}/escolas")
    public ResponseEntity<Void> adicionarEscola(
            @PathVariable UUID id,
            @Valid @RequestBody RouteSchoolRequest request
    ) throws BadRequestException {
        routeSchoolService.addSchool(id, request);
        return ResponseEntity.status(HttpStatus.OK).build();
    }

    @DeleteMapping("/{id}/escolas/{escolaId}")
    public ResponseEntity<Void> removerEscola(
            @PathVariable UUID id,
            @PathVariable UUID escolaId
    ) {
        routeSchoolService.removeSchool(id, escolaId);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

    @PatchMapping("/{id}/escolas/ordem")
    public ResponseEntity<Void> reordenarEscolas(
            @PathVariable UUID id,
            @RequestBody List<UUID> novaOrdemEscolaIds
    ) {
        routeSchoolService.reorder(id, novaOrdemEscolaIds);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }
}
