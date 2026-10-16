package com.safeway.tech.repository;

import com.safeway.tech.domain.models.GuardianInvitation;
import com.safeway.tech.domain.enums.InvitationStatusEnum;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface GuardianInvitationRepository extends JpaRepository<GuardianInvitation, UUID> {

    Optional<GuardianInvitation> findByTokenAndActiveTrue(String token);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT invitation FROM GuardianInvitation invitation "
            + "WHERE invitation.token = :token AND invitation.active = true")
    Optional<GuardianInvitation> findActiveByTokenForUpdate(@Param("token") String token);

    @Query("SELECT invitation FROM GuardianInvitation invitation "
            + "WHERE invitation.transport.id = :transportId "
            + "AND invitation.status = :status AND invitation.active = true")
    List<GuardianInvitation> findActiveByTransportAndStatus(
            @Param("transportId") UUID transportId,
            @Param("status") InvitationStatusEnum status);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT invitation FROM GuardianInvitation invitation "
            + "WHERE invitation.id = :id AND invitation.transport.id = :transportId "
            + "AND invitation.active = true")
    Optional<GuardianInvitation> findActiveByIdAndTransportForUpdate(
            @Param("id") UUID id,
            @Param("transportId") UUID transportId);
}
