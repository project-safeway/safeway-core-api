package com.safeway.tech.repository;

import com.safeway.tech.domain.enums.AttendanceStatusEnum;
import com.safeway.tech.domain.models.Attendance;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.UUID;


public interface ChamadaRepository extends JpaRepository<Attendance, UUID>, JpaSpecificationExecutor<Attendance> {

    @Query("SELECT c FROM Attendance c WHERE c.itinerario.id = :idItinerario AND c.status = :status")
    Optional<Attendance> findByItinerarioIdAndStatus(
            @Param("idItinerario") UUID idItinerario,
            @Param("status") AttendanceStatusEnum status
    );
}
