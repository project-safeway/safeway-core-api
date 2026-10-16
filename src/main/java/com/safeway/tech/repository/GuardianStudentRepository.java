package com.safeway.tech.repository;

import com.safeway.tech.domain.models.GuardianStudent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.UUID;

public interface GuardianStudentRepository extends JpaRepository<GuardianStudent, UUID> {

    @Query("SELECT CASE WHEN COUNT(link) > 0 THEN true ELSE false END "
            + "FROM GuardianStudent link WHERE link.guardian.id = :guardianId "
            + "AND link.student.id = :studentId AND link.active = true "
            + "AND link.guardian.active = true AND link.student.active = true")
    boolean existsActiveLink(
            @Param("guardianId") UUID guardianId,
            @Param("studentId") UUID studentId);

    @Query("SELECT link FROM GuardianStudent link WHERE link.guardian.id = :guardianId "
            + "AND link.student.id = :studentId")
    Optional<GuardianStudent> findByGuardianIdAndStudentId(
            @Param("guardianId") UUID guardianId,
            @Param("studentId") UUID studentId);
}
