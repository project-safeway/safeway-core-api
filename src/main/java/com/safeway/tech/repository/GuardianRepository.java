package com.safeway.tech.repository;

import com.safeway.tech.domain.models.Guardian;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface GuardianRepository extends JpaRepository<Guardian, UUID> {

    @Query("SELECT g FROM Guardian g WHERE g.user.id = :userId")
    List<Guardian> findByUserId(@Param("userId") UUID userId);

    @Query("SELECT g FROM Guardian g JOIN GuardianStudent gs WHERE gs.student = :studentId AND g.user.id = :userId")
    List<Guardian> findByStudentIdAndUserId(@Param("studentId") UUID studentId, @Param("userId") UUID userId);

    @Query("SELECT g FROM Guardian g WHERE g.id = :guardianId AND g.user.id = :userId")
    Optional<Guardian> findByGuardianIdAndUserId(@Param("guardianId") UUID guardianId, @Param("userId") UUID userId);
}
