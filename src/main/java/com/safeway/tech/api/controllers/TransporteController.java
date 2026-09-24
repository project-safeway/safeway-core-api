package com.safeway.tech.api.controllers;

import com.safeway.tech.api.dto.transporte.AlunoTransporteResponse;
import com.safeway.tech.api.dto.transporte.TransporteRequest;
import com.safeway.tech.api.dto.transporte.TransporteResponse;
import com.safeway.tech.domain.models.Aluno;
import com.safeway.tech.domain.models.Transport;
import com.safeway.tech.service.mappers.AlunoMapper;
import com.safeway.tech.service.mappers.TransporteMapper;
import com.safeway.tech.service.services.TransporteService;
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
@RequestMapping("/transporte")
@RequiredArgsConstructor
public class TransporteController {

    private final TransporteService transporteService;

    @GetMapping
    public ResponseEntity<List<TransporteResponse>> listarTransportes() {
        List<Transport> transports = transporteService.listarTransportes();
        List<TransporteResponse> response = transports.stream().map(TransporteMapper::toResponse).toList();
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @GetMapping("/{idTransporte}")
    public ResponseEntity<TransporteResponse> retornarUm(@PathVariable UUID idTransporte) {
        Transport transport = transporteService.buscarPorId(idTransporte);
        TransporteResponse response = TransporteMapper.toResponse(transport);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @GetMapping("/{idTransporte}/alunos")
    public ResponseEntity<List<AlunoTransporteResponse>> listarAlunosDoTransporte(@PathVariable UUID idTransporte) {
        List<Aluno> alunoTransporte = transporteService.listarAlunos(idTransporte);
        List<AlunoTransporteResponse> response = alunoTransporte.stream().map(AlunoMapper::toTransporteResponse).toList();
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @PostMapping
    public ResponseEntity<TransporteResponse> salvarTransporte(@RequestBody @Valid TransporteRequest request) {
        Transport transport = transporteService.salvarTransporte(request);
        TransporteResponse response = TransporteMapper.toResponse(transport);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PutMapping("/{idTransporte}")
    public ResponseEntity<TransporteResponse> alterarTransporte(@RequestBody @Valid TransporteRequest request, @PathVariable UUID idTransporte) {
        Transport transport = transporteService.atualizarTransporte(idTransporte, request);
        TransporteResponse response = TransporteMapper.toResponse(transport);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @DeleteMapping("/{idTransporte}")
    public ResponseEntity<Void> excluir(@PathVariable UUID idTransporte) {
        transporteService.excluirTransporte(idTransporte);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }
}
