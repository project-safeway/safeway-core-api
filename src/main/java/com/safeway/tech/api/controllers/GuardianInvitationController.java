package com.safeway.tech.api.controllers;

import com.safeway.tech.api.dto.guardian.GuardianInvitationAcceptRequest;
import com.safeway.tech.api.dto.guardian.GuardianInvitationApprovalRequest;
import com.safeway.tech.api.dto.guardian.GuardianInvitationCreateRequest;
import com.safeway.tech.api.dto.guardian.GuardianInvitationCreatedResponse;
import com.safeway.tech.api.dto.guardian.GuardianInvitationRegistrationResponse;
import com.safeway.tech.api.dto.guardian.GuardianInvitationResponse;
import com.safeway.tech.api.dto.guardian.PendingGuardianInvitationResponse;
import com.safeway.tech.service.services.GuardianInvitationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/guardian-invitations")
@RequiredArgsConstructor
public class GuardianInvitationController {

    private final GuardianInvitationService invitationService;

    @PostMapping
    public ResponseEntity<GuardianInvitationCreatedResponse> create(
            @Valid @RequestBody GuardianInvitationCreateRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(invitationService.create(request));
    }

    @GetMapping("/public/{token}")
    public ResponseEntity<GuardianInvitationResponse> getPublicInvitation(@PathVariable String token) {
        return ResponseEntity.ok(invitationService.getPublicInvitation(token));
    }

    @PostMapping("/public/{token}/accept")
    public ResponseEntity<GuardianInvitationRegistrationResponse> accept(
            @PathVariable String token,
            @Valid @RequestBody GuardianInvitationAcceptRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(invitationService.accept(token, request));
    }

    @GetMapping("/pending")
    public ResponseEntity<List<PendingGuardianInvitationResponse>> listPending() {
        return ResponseEntity.ok(invitationService.listPending());
    }

    @PostMapping("/{invitationId}/approve")
    public ResponseEntity<Void> approve(
            @PathVariable UUID invitationId,
            @Valid @RequestBody GuardianInvitationApprovalRequest request) {
        invitationService.approve(invitationId, request);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{invitationId}")
    public ResponseEntity<Void> cancel(@PathVariable UUID invitationId) {
        invitationService.cancel(invitationId);
        return ResponseEntity.noContent().build();
    }
}
