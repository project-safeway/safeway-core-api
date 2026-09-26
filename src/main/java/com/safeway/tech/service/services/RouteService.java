package com.safeway.tech.service.services;

import com.safeway.tech.api.dto.route.RouteRequest;
import com.safeway.tech.api.dto.route.RouteUpdateRequest;
import com.safeway.tech.api.dto.route.RouteUpdateRequest.RouteStopUpdate;
import com.safeway.tech.domain.models.Route;
import com.safeway.tech.domain.models.RouteStudent;
import com.safeway.tech.domain.models.RouteSchool;
import com.safeway.tech.domain.models.Transport;
import com.safeway.tech.infra.exception.RouteNotFoundException;
import com.safeway.tech.repository.RouteRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RouteService {

    private final RouteRepository routeRepository;
    private final TransportService transportService;
    private final RouteStudentService routeStudentService;
    private final RouteSchoolService routeSchoolService;
    private final CurrentUserService currentUserService;

    public List<Route> findAll() {
        UUID transportId = currentUserService.getCurrentTransporteId();
        return routeRepository.findAllByTransporte(transportId);
    }

    public Route findById(UUID id) {
        return routeRepository.findById(id)
                .orElseThrow(() -> new RouteNotFoundException("Itinerário não encontrado"));
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
        route.setEndTime(request.startTime());
        route.setRouteType(request.routeType());

        UUID transportId = currentUserService.getCurrentTransporteId();
        Transport transport = transportService.findById(transportId);
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

        if (request.students() != null && !request.students().isEmpty()) {
            routeStudentService.syncStudents(route, request.students());
        }

        if (request.stops() != null && !request.stops().isEmpty()) {
            List<RouteStudent> actualStudents = routeStudentService.findByRouteId(route.getId());
            List<RouteSchool> actualSchools = routeSchoolService.findByRouteId(route.getId());

            Map<UUID, RouteStudent> studentMap = actualStudents.stream()
                    .collect(Collectors.toMap(a -> a.getStudent().getId(), a -> a));

            Map<UUID, RouteSchool> schoolMap = actualSchools.stream()
                    .collect(Collectors.toMap(e -> e.getSchool().getId(), e -> e));

            for (RouteStopUpdate stop : request.stops()) {
                if (stop == null || stop.id() == null) {
                    continue;
                }
                if ("ALUNO".equalsIgnoreCase(stop.type())) {
                    RouteStudent routeStudent = studentMap.get(stop.id());
                    if (routeStudent != null) {
                        routeStudent.setGeneralOrder(stop.generalOrder());
                        routeStudent.setBoardingOrder(stop.specificOrder());
                    }
                } else if ("ESCOLA".equalsIgnoreCase(stop.type())) {
                    RouteSchool routeSchool = schoolMap.get(stop.id());
                    if (routeSchool != null) {
                        routeSchool.setGeneralOrder(stop.generalOrder());
                        routeSchool.setStopOrder(stop.specificOrder());
                    }
                }
            }

            routeStudentService.saveAll(actualStudents);
            routeSchoolService.saveAll(actualSchools);
        }

        return routeRepository.save(route);
    }

}
