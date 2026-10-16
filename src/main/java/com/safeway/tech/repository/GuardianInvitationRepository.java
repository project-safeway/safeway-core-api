package com.safeway.tech.repository;

import com.safeway.tech.domain.models.GuardianInvitation;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface GuardianInvitationRepository extends JpaRepository<GuardianInvitation, UUID> {
}
