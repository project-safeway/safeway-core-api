package com.safeway.tech.api.controllers;

import com.safeway.tech.api.dto.transport.StudentTransportResponse;
import com.safeway.tech.api.dto.transport.TransportRequest;
import com.safeway.tech.api.dto.transport.TransportResponse;
import com.safeway.tech.domain.models.Student;
import com.safeway.tech.domain.models.Transport;
import com.safeway.tech.service.mappers.StudentMapper;
import com.safeway.tech.service.mappers.TransportMapper;
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
    public ResponseEntity<List<TransportResponse>> listarTransportes() {
        List<Transport> transports = transporteService.listarTransportes();
        List<TransportResponse> response = transports.stream().map(TransportMapper::toResponse).toList();
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @GetMapping("/{idTransporte}")
    public ResponseEntity<TransportResponse> retornarUm(@PathVariable UUID idTransporte) {
        Transport transport = transporteService.buscarPorId(idTransporte);
        TransportResponse response = TransportMapper.toResponse(transport);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @GetMapping("/{idTransporte}/alunos")
    public ResponseEntity<List<StudentTransportResponse>> listarAlunosDoTransporte(@PathVariable UUID idTransporte) {
        List<Student> studentTransporte = transporteService.listarAlunos(idTransporte);
        List<StudentTransportResponse> response = studentTransporte.stream().map(StudentMapper::toTransportResponse).toList();
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @PostMapping
    public ResponseEntity<TransportResponse> salvarTransporte(@RequestBody @Valid TransportRequest request) {
        Transport transport = transporteService.salvarTransporte(request);
        TransportResponse response = TransportMapper.toResponse(transport);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PutMapping("/{idTransporte}")
    public ResponseEntity<TransportResponse> alterarTransporte(@RequestBody @Valid TransportRequest request, @PathVariable UUID idTransporte) {
        Transport transport = transporteService.atualizarTransporte(idTransporte, request);
        TransportResponse response = TransportMapper.toResponse(transport);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @DeleteMapping("/{idTransporte}")
    public ResponseEntity<Void> excluir(@PathVariable UUID idTransporte) {
        transporteService.excluirTransporte(idTransporte);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }
}
