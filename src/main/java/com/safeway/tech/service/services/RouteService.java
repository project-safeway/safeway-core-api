package com.safeway.tech.service.services;

import com.safeway.tech.api.dto.route.RouteRequest;
import com.safeway.tech.api.dto.route.RouteStudentRequest;
import com.safeway.tech.api.dto.route.RouteUpdateRequest;
import com.safeway.tech.domain.models.Route;
import com.safeway.tech.domain.models.Transport;
import com.safeway.tech.infra.exception.EntityNotFoundException;
import com.safeway.tech.repository.RouteRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class RouteService {

    private final RouteRepository routeRepository;
    private final TransportService transportService;
    private final RouteStudentService routeStudentService;
    private final RouteSchoolService routeSchoolService;
    private final CurrentUserService currentUserService;

    public List<Route> findAll() {
        UUID transportId = currentUserService.getCurrentTransportId();
        return routeRepository.findAllByTransportId(transportId);
    }

    public Route findById(UUID id) {
        UUID transportId = currentUserService.getCurrentTransportId();
        return routeRepository.findByIdAndTransportId(id, transportId)
                .orElseThrow(() -> new EntityNotFoundException("Route was not found"));
    }

    @Transactional
    public Route findByIdForUpdate(UUID id) {
        UUID transportId = currentUserService.getCurrentTransportId();
        return routeRepository.findByIdAndTransportIdForUpdate(id, transportId)
                .orElseThrow(() -> new EntityNotFoundException("Route was not found"));
    }

    @Transactional
    public void deactivate(UUID id) {
        Route route = findById(id);
        route.setActive(false);
        routeRepository.save(route);
    }

    @Transactional
    public Route createRoute(RouteRequest request) {

        Route route = new Route();

        route.setName(request.name());
        route.setStartTime(request.startTime());
        route.setEndTime(request.endTime());
        route.setRouteType(request.routeType());

        Transport transport = transportService.getTransport();
        route.setTransport(transport);

        return routeRepository.save(route);
    }

    @Transactional
    public Route updateRoute(UUID id, RouteUpdateRequest request) {
        Route route = findById(id);

        route.setName(request.name());
        route.setStartTime(request.startTime());
        route.setEndTime(request.endTime());
        route.setRouteType(request.routeType());
        route.setActive(request.active());

        if (request.stops() != null) {
            boolean invalidStop = request.stops().stream().anyMatch(stop ->
                    stop == null
                            || stop.id() == null
                            || stop.generalOrder() == null
                            || stop.specificOrder() == null
                            || (!"ALUNO".equalsIgnoreCase(stop.type())
                                    && !"ESCOLA".equalsIgnoreCase(stop.type())));
            if (invalidStop) {
                throw new IllegalArgumentException("Every stop requires a valid type, id, and order");
            }

            List<RouteStudentRequest> studentStops = request.stops().stream()
                    .filter(stop -> "ALUNO".equalsIgnoreCase(stop.type()))
                    .map(stop -> new RouteStudentRequest(stop.id(), stop.specificOrder(), null))
                    .toList();
            routeStudentService.syncStudents(route, studentStops);
            routeStudentService.updateGeneralOrder(route.getId(), request.stops());

            routeSchoolService.syncStops(route, request.stops());
        } else if (request.students() != null) {
            routeStudentService.syncStudents(route, request.students());
        }

        return routeRepository.save(route);
    }

}
