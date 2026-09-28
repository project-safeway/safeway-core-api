package com.safeway.tech.repository;

import com.safeway.tech.domain.models.Route;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface RouteRepository extends JpaRepository<Route, UUID> {

    @Query("SELECT i FROM Route i WHERE i.transport.id = :transportId AND i.active = true")
    List<Route> findAllByTransportId(@Param("transportId") UUID transportId);
}
