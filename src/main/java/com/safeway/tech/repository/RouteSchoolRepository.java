package com.safeway.tech.repository;

import com.safeway.tech.domain.models.RouteSchool;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface RouteSchoolRepository extends JpaRepository<RouteSchool, UUID> {

    @Query("SELECT ie FROM RouteSchool ie WHERE ie.itinerario.id = :itinerarioId AND ie.escola.id = :escolaId")
    Optional<RouteSchool> findByItinerarioIdAndEscolaIdEscola(@Param("itinerarioId") UUID itinerarioId, @Param("escolaId") UUID escolaId);

    @Query("SELECT ie FROM RouteSchool ie WHERE ie.itinerario.id = :itinerarioId")
    List<RouteSchool> findByItinerarioId(@Param("itinerarioId") UUID itinerarioId);
}

