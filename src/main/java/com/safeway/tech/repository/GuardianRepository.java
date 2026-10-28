package com.safeway.tech.repository;

import com.safeway.tech.domain.models.Guardian;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface GuardianRepository extends JpaRepository<Guardian, UUID> {

    @Query("SELECT DISTINCT g FROM Guardian g LEFT JOIN g.user owner "
            + "WHERE g.active = true AND (owner.id = :userId OR EXISTS "
            + "(SELECT link.id FROM GuardianStudent link WHERE link.guardian = g "
            + "AND link.active = true AND link.student.transport.id = :transportId))")
    List<Guardian> findByOwnerOrTransport(
            @Param("userId") UUID userId,
            @Param("transportId") UUID transportId);

    @Query("SELECT DISTINCT g FROM Guardian g JOIN GuardianStudent link ON link.guardian = g "
            + "WHERE link.student.id = :studentId AND link.student.transport.id = :transportId "
            + "AND g.active = true AND link.active = true")
    List<Guardian> findByStudentIdAndTransportId(
            @Param("studentId") UUID studentId,
            @Param("transportId") UUID transportId);

    Optional<Guardian> findByUserIdAndActiveTrue(UUID userId);

    @Query("SELECT g FROM Guardian g LEFT JOIN g.user owner "
            + "WHERE g.id = :guardianId AND g.active = true "
            + "AND (owner.id = :userId OR EXISTS "
            + "(SELECT link.id FROM GuardianStudent link WHERE link.guardian = g "
            + "AND link.active = true AND link.student.transport.id = :transportId))")
    Optional<Guardian> findByGuardianIdAndOwnerOrTransport(
            @Param("guardianId") UUID guardianId,
            @Param("userId") UUID userId,
            @Param("transportId") UUID transportId);

    @Query("SELECT DISTINCT g FROM Guardian g LEFT JOIN g.user owner WHERE g.active = true "
            + "AND LOWER(g.email) = LOWER(:email) "
            + "AND (owner.id = :userId OR EXISTS "
            + "(SELECT link.id FROM GuardianStudent link WHERE link.guardian = g "
            + "AND link.active = true AND link.student.transport.id = :transportId))")
    List<Guardian> findByEmailAndOwnerOrTransport(
            @Param("email") String email,
            @Param("userId") UUID userId,
            @Param("transportId") UUID transportId);
}
