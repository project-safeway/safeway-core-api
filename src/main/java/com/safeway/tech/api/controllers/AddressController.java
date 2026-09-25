package com.safeway.tech.api.controllers;

import com.safeway.tech.api.dto.address.AddressRequest;
import com.safeway.tech.api.dto.address.AddressResponse;
import com.safeway.tech.domain.models.Address;
import com.safeway.tech.service.mappers.AddressMapper;
import com.safeway.tech.service.services.AddressService;
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
public class AddressController {

    private final AddressService addressService;

    @PostMapping
    public ResponseEntity<AddressResponse> criar(@Valid @RequestBody AddressRequest request) {
        Address address = addressService.create(request);
        AddressResponse response = AddressMapper.toResponse(address);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<AddressResponse> buscar(@PathVariable UUID id) {
        Address address = addressService.findById(id);
        AddressResponse response = AddressMapper.toResponse(address);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @PutMapping("/{id}")
    public ResponseEntity<AddressResponse> atualizar(
            @PathVariable UUID id,
            @Valid @RequestBody AddressRequest request
    ) {
        Address address = addressService.update(id, request);
        AddressResponse response = AddressMapper.toResponse(address);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> desativar(@PathVariable UUID id) {
        addressService.deactivate(id);
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
