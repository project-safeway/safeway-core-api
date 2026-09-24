package com.safeway.tech.api.controllers;

import com.safeway.tech.api.dto.endereco.EnderecoRequest;
import com.safeway.tech.api.dto.endereco.EnderecoResponse;
import com.safeway.tech.domain.models.Address;
import com.safeway.tech.service.mappers.EnderecoMapper;
import com.safeway.tech.service.services.EnderecoService;
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

import java.util.UUID;

@RestController
@RequestMapping("/enderecos")
@RequiredArgsConstructor
public class EnderecoController {

    private final EnderecoService enderecoService;

    @PostMapping
    public ResponseEntity<EnderecoResponse> criar(@Valid @RequestBody EnderecoRequest request) {
        Address address = enderecoService.criar(request);
        EnderecoResponse response = EnderecoMapper.toResponse(address);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<EnderecoResponse> buscar(@PathVariable UUID id) {
        Address address = enderecoService.buscarPorId(id);
        EnderecoResponse response = EnderecoMapper.toResponse(address);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @PutMapping("/{id}")
    public ResponseEntity<EnderecoResponse> atualizar(
            @PathVariable UUID id,
            @Valid @RequestBody EnderecoRequest request
    ) {
        Address address = enderecoService.atualizar(id, request);
        EnderecoResponse response = EnderecoMapper.toResponse(address);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> desativar(@PathVariable UUID id) {
        enderecoService.desativar(id);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

    // Endpoints para testar o GeocodingService diretamente

//    @GetMapping("/coordenadas")
//    public ResponseEntity<Map<String, Object>> obterCoordenadas(@RequestParam String endereco) {
//        try {
//            LatLng coordenadas = geocodingService.obterCoordenadas(endereco);
//
//            Map<String, Object> response = new HashMap<>();
//            response.put("endereco", endereco);
//            response.put("latitude", coordenadas.lat);
//            response.put("longitude", coordenadas.lng);
//
//            return ResponseEntity.ok(response);
//
//        } catch (Exception e) {
//            Map<String, Object> error = new HashMap<>();
//            error.put("erro", e.getMessage());
//            return ResponseEntity.badRequest().body(error);
//        }
//    }
//
//    @GetMapping("/endereco-formatado")
//    public ResponseEntity<Map<String, String>> obterEnderecoFormatado(@RequestParam String endereco) {
//        try {
//            String enderecoFormatado = geocodingService.obterEnderecoFormatado(endereco);
//
//            Map<String, String> response = new HashMap<>();
//            response.put("original", endereco);
//            response.put("formatado", enderecoFormatado);
//
//            return ResponseEntity.ok(response);
//
//        } catch (Exception e) {
//            Map<String, String> error = new HashMap<>();
//            error.put("erro", e.getMessage());
//            return ResponseEntity.badRequest().body(error);
//        }
//    }
}
