package com.safeway.tech.api.controllers;

import com.safeway.tech.api.dto.transport.TransportRequest;
import com.safeway.tech.api.dto.transport.TransportResponse;
import com.safeway.tech.domain.models.Transport;
import com.safeway.tech.service.mappers.TransportMapper;
import com.safeway.tech.service.services.TransportService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/transports")
@RequiredArgsConstructor
public class TransportController {

    private final TransportService transportService;

    @GetMapping
    public ResponseEntity<TransportResponse> getTransport() {
        Transport transport = transportService.getTransport();
        TransportResponse response = TransportMapper.toResponse(transport);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @PostMapping
    public ResponseEntity<TransportResponse> createTransport(@RequestBody @Valid TransportRequest request) {
        Transport transport = transportService.saveTransport(request);
        TransportResponse response = TransportMapper.toResponse(transport);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PutMapping
    public ResponseEntity<TransportResponse> updateTransport(@RequestBody @Valid TransportRequest request) {
        Transport transport = transportService.updateTransport(request);
        TransportResponse response = TransportMapper.toResponse(transport);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @DeleteMapping
    public ResponseEntity<Void> delete() {
        transportService.deleteTransport();
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }
}
