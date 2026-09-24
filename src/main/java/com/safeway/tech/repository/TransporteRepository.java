package com.safeway.tech.repository;

import com.safeway.tech.domain.models.Transport;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface TransporteRepository extends JpaRepository<Transport, UUID> {

    @Query("SELECT t FROM Transport t WHERE t.placa = :placa")
    Optional<Transport> findByPlaca(@Param("placa") String placa);

    @Query("SELECT t FROM Transport t WHERE t.id = :idTransporte AND t.usuario.id = :userId")
    Optional<Transport> findByIdAndUsuarioId(@Param("idTransporte") UUID idTransporte, @Param("userId") UUID userId);

    @Query("SELECT t FROM Transport t WHERE t.usuario.id = :userId")
    List<Transport> findAllByIdUsuario(@Param("userId") UUID userId);
}
