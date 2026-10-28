package com.safeway.tech.repository;

import com.safeway.tech.domain.models.School;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface SchoolRepository extends JpaRepository<School, UUID> {
    @Query("SELECT s FROM School s WHERE s.transport.id = :transportId AND s.active = true")
    List<School> findAllByTransportId(@Param("transportId") UUID transportId);

    @Query("SELECT s FROM School s WHERE s.id = :schoolId AND s.transport.id = :transportId AND s.active = true")
    Optional<School> findByIdAndTransportId(@Param("schoolId") UUID schoolId, @Param("transportId") UUID transportId);
}
