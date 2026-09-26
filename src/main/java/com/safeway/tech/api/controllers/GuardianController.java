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
@RequestMapping("/guardians")
@RequiredArgsConstructor
public class GuardianController {

    private final ResponsavelService responsavelService;

    @PostMapping
    public ResponseEntity<GuardianResponse> createGuardian(@RequestBody @Valid GuardianRequest request) {
        Guardian guardian = responsavelService.createGuardian(request);
        GuardianResponse response = GuardianMapper.toResponse(guardian);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    public ResponseEntity<List<GuardianResponse>> getGuardians() {
        List<Guardian> guardians = responsavelService.listGuardians();
        List<GuardianResponse> response = guardians.stream().map(GuardianMapper::toResponse).toList();
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<GuardianResponse> findById(@PathVariable UUID id) {
        Guardian guardian = responsavelService.findById(id);
        GuardianResponse response = GuardianMapper.toResponse(guardian);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @DeleteMapping("/{guardianId}")
    public ResponseEntity<Void> delete(@PathVariable UUID guardianId) {
        responsavelService.deactivate(guardianId);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

    @PutMapping("/{guardianId}")
    public ResponseEntity<GuardianResponse> updateGuardian(
            @RequestBody @Valid GuardianRequest newGuardian,
            @PathVariable UUID guardianId) {
        Guardian guardian = responsavelService.updateGuardian(newGuardian, guardianId);
        GuardianResponse response = GuardianMapper.toResponse(guardian);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }
}
