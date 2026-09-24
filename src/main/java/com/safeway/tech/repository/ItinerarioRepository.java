package com.safeway.tech.repository;

import com.safeway.tech.domain.models.Route;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface ItinerarioRepository extends JpaRepository<Route, UUID> {

    @Query("SELECT i FROM Route i WHERE i.transporte.id = :idTransporte AND i.ativo = true")
    List<Route> findAllByTransporte(@Param("idTransporte") UUID idTransporte);
}
