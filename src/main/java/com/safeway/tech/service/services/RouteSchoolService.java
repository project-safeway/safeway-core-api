package com.safeway.tech.service.services;

import com.safeway.tech.api.dto.route.RouteSchoolRequest;
import com.safeway.tech.api.dto.route.RouteUpdateRequest.RouteStopUpdate;
import com.safeway.tech.domain.models.Address;
import com.safeway.tech.domain.models.Route;
import com.safeway.tech.domain.models.RouteSchool;
import com.safeway.tech.domain.models.School;
import com.safeway.tech.infra.exception.CoordinatesNotValidException;
import com.safeway.tech.infra.exception.EntityNotFoundException;
import com.safeway.tech.repository.RouteRepository;
import com.safeway.tech.repository.RouteSchoolRepository;
import lombok.RequiredArgsConstructor;
import org.apache.coyote.BadRequestException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RouteSchoolService {

    private final RouteSchoolRepository routeSchoolRepository;
    private final RouteRepository routeRepository;
    private final SchoolService schoolService;

    public List<RouteSchool> findByRouteId(UUID routeId) {
        return routeSchoolRepository.findByRouteId(routeId);
    }

    public void saveAll(List<RouteSchool> schools) {
        routeSchoolRepository.saveAll(schools);
    }

    @Transactional
    public void syncStops(Route route, List<RouteStopUpdate> stops) {
        List<RouteStopUpdate> schoolStops = stops.stream()
                .filter(stop -> "ESCOLA".equalsIgnoreCase(stop.type()))
                .toList();
        routeSchoolRepository.deleteAllByRouteId(route.getId());

        List<RouteSchool> entities = schoolStops.stream().map(stop -> {
            if (stop.id() == null || stop.generalOrder() == null || stop.specificOrder() == null) {
                throw new IllegalArgumentException("School stops require an id and order values");
            }

            School school = schoolService.findById(stop.id());
            Address address = school.getAddress();
            if (address.getLatitude() == null || address.getLongitude() == null) {
                throw new CoordinatesNotValidException("School address does not have valid coordinates");
            }
            BigDecimal latitude = address.getLatitude();
            BigDecimal longitude = address.getLongitude();
            if (latitude.compareTo(BigDecimal.valueOf(-90)) < 0
                    || latitude.compareTo(BigDecimal.valueOf(90)) > 0
                    || longitude.compareTo(BigDecimal.valueOf(-180)) < 0
                    || longitude.compareTo(BigDecimal.valueOf(180)) > 0) {
                throw new CoordinatesNotValidException("Invalid school address coordinates");
            }

            RouteSchool entity = new RouteSchool();
            entity.setRoute(route);
            entity.setSchool(school);
            entity.setAddress(address);
            entity.setStopOrder(stop.specificOrder());
            entity.setGeneralOrder(stop.generalOrder());
            return entity;
        }).toList();

        routeSchoolRepository.saveAll(entities);
    }

    @Transactional
    public void addSchool(UUID routeId, RouteSchoolRequest request) throws BadRequestException {
        Route route = routeRepository.findById(routeId)
                .orElseThrow(() -> new EntityNotFoundException("Route was not found"));

        School school = schoolService.findById(request.schoolId());

        Address address;
        if (request.addressId() != null) {
            address = school.getAddress();
            if (!request.addressId().equals(address.getId())) {
                throw new IllegalArgumentException("Address must match the selected school's address");
            }
        } else {
            address = school.getAddress();
        }

        if (address.getLatitude() == null || address.getLongitude() == null) {
            throw new CoordinatesNotValidException("School address does not have valid latitude/longitude");
        }

        BigDecimal lat = address.getLatitude();
        BigDecimal lng = address.getLongitude();

        if (lat.compareTo(BigDecimal.valueOf(-90)) < 0 || lat.compareTo(BigDecimal.valueOf(90)) > 0
                || lng.compareTo(BigDecimal.valueOf(-180)) < 0 || lng.compareTo(BigDecimal.valueOf(180)) > 0) {
            throw new BadRequestException("Invalid school address coordinates: " + lat + ", " + lng);
        }

        routeSchoolRepository.findByRouteIdAndSchoolId(routeId, school.getId())
                .ifPresent(e -> {
                    throw new IllegalArgumentException("School is already assigned to this route");
                });

        RouteSchool entity = new RouteSchool();
        entity.setRoute(route);
        entity.setSchool(school);
        entity.setAddress(address);
        entity.setStopOrder(request.stopOrder());

        routeSchoolRepository.save(entity);
    }

    @Transactional
    public void removeSchool(UUID routeId, UUID schoolId) {
        RouteSchool entity = routeSchoolRepository
                .findByRouteIdAndSchoolId(routeId, schoolId)
                .orElseThrow(() -> new EntityNotFoundException("School was not found on this route"));

        routeSchoolRepository.delete(entity);
    }

    @Transactional
    public void reorder(UUID routeId, List<UUID> newSchoolIdsOrder) {
        List<RouteSchool> actual = routeSchoolRepository.findByRouteId(routeId);

        Map<UUID, RouteSchool> map = actual.stream()
                .collect(Collectors.toMap(e -> e.getSchool().getId(), e -> e));

        int order = 1;
        for (UUID id : newSchoolIdsOrder) {
            RouteSchool ie = map.get(id);
            if (ie != null) {
                ie.setStopOrder(order++);
            }
        }

        routeSchoolRepository.saveAll(actual);
    }
}
