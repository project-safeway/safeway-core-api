package com.safeway.tech.api.controllers;

import com.safeway.tech.api.dto.route.RouteRequest;
import com.safeway.tech.api.dto.route.RouteResponse;
import com.safeway.tech.api.dto.route.RouteSchoolRequest;
import com.safeway.tech.api.dto.route.RouteStudentRequest;
import com.safeway.tech.api.dto.route.RouteUpdateRequest;
import com.safeway.tech.api.dto.route.StudentWithAddress;
import com.safeway.tech.domain.models.Route;
import com.safeway.tech.service.mappers.RouteMapper;
import com.safeway.tech.service.services.RouteSchoolService;
import com.safeway.tech.service.services.RouteService;
import com.safeway.tech.service.services.RouteStudentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.apache.coyote.BadRequestException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/routes")
@RequiredArgsConstructor
public class RouteController {

    private final RouteService routeService;
    private final RouteStudentService routeStudentService;
    private final RouteSchoolService routeSchoolService;

    @PostMapping
    public ResponseEntity<RouteResponse> create(
            @Valid @RequestBody RouteRequest request
    ) {
        Route route = routeService.createRoute(request);
        RouteResponse response = RouteMapper.toResponse(route);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @GetMapping
    public ResponseEntity<List<RouteResponse>> getRoutes() {
        List<Route> routes = routeService.findAll();
        List<RouteResponse> response = routes.stream().map(RouteMapper::toResponse).toList();
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<RouteResponse> findById(@PathVariable UUID id) {
        Route route = routeService.findById(id);
        RouteResponse response = RouteMapper.toResponse(route);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @GetMapping("/{id}/students")
    public ResponseEntity<List<StudentWithAddress>> findAllStudentsWithAddress(@PathVariable UUID id) {
        List<StudentWithAddress> students = routeStudentService.findStudentWithAddress(id);
        return ResponseEntity.status(HttpStatus.OK).body(students);
    }

    @PutMapping("/{id}")
    public ResponseEntity<RouteResponse> update(
            @PathVariable UUID id,
            @Valid @RequestBody RouteUpdateRequest request
    ) {
        Route route = routeService.updateRoute(id, request);
        RouteResponse response = RouteMapper.toResponse(route);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deactivate(@PathVariable UUID id) {
        routeService.deactivate(id);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

    @PostMapping("/{id}/students")
    public ResponseEntity<Void> addStudent(
            @PathVariable UUID id,
            @Valid @RequestBody RouteStudentRequest request
    ) {
        routeStudentService.addStudent(id, request);
        return ResponseEntity.status(HttpStatus.OK).build();
    }

    @DeleteMapping("/{id}/students/{studentId}")
    public ResponseEntity<Void> removeStudent(
            @PathVariable UUID id,
            @PathVariable UUID studentId
    ) {
        routeStudentService.removeStudent(id, studentId);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

    @PatchMapping("/{id}/students/order")
    public ResponseEntity<Void> reorder(
            @PathVariable UUID id,
            @RequestBody List<UUID> newIdsOrder
    ) {
        routeStudentService.reorder(id, newIdsOrder);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

    @PostMapping("/{id}/schools")
    public ResponseEntity<Void> addSchool(
            @PathVariable UUID id,
            @Valid @RequestBody RouteSchoolRequest request
    ) throws BadRequestException {
        routeSchoolService.addSchool(id, request);
        return ResponseEntity.status(HttpStatus.OK).build();
    }

    @DeleteMapping("/{id}/schools/{schoolId}")
    public ResponseEntity<Void> removeSchool(
            @PathVariable UUID id,
            @PathVariable UUID schoolId
    ) {
        routeSchoolService.removeSchool(id, schoolId);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

    @PatchMapping("/{id}/schools/order")
    public ResponseEntity<Void> reorderSchools(
            @PathVariable UUID id,
            @RequestBody List<UUID> newSchoolIdsOrder
    ) {
        routeSchoolService.reorder(id, newSchoolIdsOrder);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }
}
