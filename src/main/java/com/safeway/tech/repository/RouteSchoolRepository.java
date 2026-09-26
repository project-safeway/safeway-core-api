package com.safeway.tech.repository;

import com.safeway.tech.domain.models.RouteSchool;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface RouteSchoolRepository extends JpaRepository<RouteSchool, UUID> {

    @Query("SELECT rs FROM RouteSchool rs WHERE rs.route.id = :routeId AND rs.school.id = :schoolId")
    Optional<RouteSchool> findByRouteIdAndSchoolId(@Param("routeId") UUID routeId, @Param("schoolId") UUID schoolId);

    @Query("SELECT rs FROM RouteSchool rs WHERE rs.route.id = :routeId")
    List<RouteSchool> findByRouteId(@Param("routeId") UUID routeId);
}

