package com.safeway.tech.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface GuardianInvitationRepository extends JpaRepository<GuardianInvitationRepository, UUID> {
}
