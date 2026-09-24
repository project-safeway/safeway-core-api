package com.safeway.tech.repository;

import com.safeway.tech.domain.models.Route;
import com.safeway.tech.domain.models.RouteStudent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ItinerarioAlunoRepository extends JpaRepository<RouteStudent, UUID> {

    @Query("SELECT ia FROM RouteStudent ia WHERE ia.itinerario.id = :itinerarioId")
    List<RouteStudent> findByItinerarioId(@Param("itinerarioId") UUID itinerarioId);

    @Query("SELECT ia FROM RouteStudent ia WHERE ia.itinerario.id = :itinerarioId AND ia.aluno.id = :alunoId")
    Optional<RouteStudent> findByItinerarioIdAndAlunoId(
            @Param("itinerarioId") UUID itinerarioId,
            @Param("alunoId") UUID alunoId);

    @Modifying
    @Transactional
    @Query("DELETE FROM RouteStudent ia WHERE ia.itinerario.id = :itinerarioId")
    void deleteAllByItinerarioId(
            @Param("itinerarioId") UUID itinerarioId);

    @Query("SELECT ia FROM RouteStudent ia WHERE ia.itinerario = :itinerario ORDER BY ia.ordemEmbarque ASC")
    List<RouteStudent> findByItinerarioOrderByOrdemEmbarqueAsc(@Param("itinerario") Route route);
}
