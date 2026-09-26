package com.safeway.tech.repository;

import com.safeway.tech.domain.models.Transport;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.UUID;

public interface TransportRepository extends JpaRepository<Transport, UUID> {

    @Query("SELECT t FROM Transport t WHERE t.licensePlate = :placa")
    Optional<Transport> findByPlaca(@Param("licensePlate") String placa);

    @Query("SELECT t FROM Transport t WHERE t.usuario.id = :userId")
    Transport findByUserId(@Param("userId") UUID userId);
}
