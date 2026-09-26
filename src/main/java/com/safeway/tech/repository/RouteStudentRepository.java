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

public interface RouteStudentRepository extends JpaRepository<RouteStudent, UUID> {

    @Query("SELECT rs FROM RouteStudent rs WHERE rs.route.id = :routeId")
    List<RouteStudent> findByRouteId(@Param("routeId") UUID routeId);

    @Query("SELECT rs FROM RouteStudent rs WHERE rs.route.id = :routeId AND rs.student.id = :studentId")
    Optional<RouteStudent> findByRouteIdAndStudentId(
            @Param("routeId") UUID routeId,
            @Param("studentId") UUID studentId);

    @Modifying
    @Transactional
    @Query("DELETE FROM RouteStudent rs WHERE rs.route.id = :routeId")
    void deleteAllByRouteId(@Param("routeId") UUID routeId);

    @Query("SELECT rs FROM RouteStudent rs WHERE rs.route = :route ORDER BY rs.boardingOrder ASC")
    List<RouteStudent> findByRouteOrderByBoardingOrderAsc(@Param("route") Route route);
}
