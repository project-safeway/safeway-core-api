package com.safeway.tech.service.services;

import com.safeway.tech.api.dto.route.RouteSchoolRequest;
import com.safeway.tech.domain.models.Address;
import com.safeway.tech.domain.models.School;
import com.safeway.tech.domain.models.Route;
import com.safeway.tech.domain.models.RouteSchool;
import com.safeway.tech.infra.exception.AddressNotFoundException;
import com.safeway.tech.infra.exception.RouteSchoolNotFound;
import com.safeway.tech.infra.exception.RouteNotFoundException;
import com.safeway.tech.repository.RouteSchoolRepository;
import com.safeway.tech.repository.RouteRepository;
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
    private final AddressService addressService;

    public List<RouteSchool> findByRouteId(UUID routeId) {
        return routeSchoolRepository.findByItinerarioId(routeId);
    }

    public void saveAll(List<RouteSchool> schools) {
        routeSchoolRepository.saveAll(schools);
    }

    @Transactional
    public void addSchool(UUID routeId, RouteSchoolRequest request) throws BadRequestException {
        Route route = routeRepository.findById(routeId)
                .orElseThrow(() -> new RouteNotFoundException("Itinerário não encontrado"));

        School school = schoolService.findById(request.schoolId());

        Address address;
        if (request.addressId() != null) {
            address = addressService.findById(request.addressId());
        } else {
            address = school.getAddress();
        }

        if (address.getLatitude() == null || address.getLongitude() == null) {
            throw new AddressNotFoundException("Endereço da school não possui latitude/longitude válidas");
        }

        BigDecimal lat = address.getLatitude();
        BigDecimal lng = address.getLongitude();

        if (lat.compareTo(BigDecimal.valueOf(-90)) < 0 || lat.compareTo(BigDecimal.valueOf(90)) > 0
                || lng.compareTo(BigDecimal.valueOf(-180)) < 0 || lng.compareTo(BigDecimal.valueOf(180)) > 0) {
            throw new BadRequestException("Coordenadas do endereço da school inválidas: " + lat + ", " + lng);
        }

        routeSchoolRepository.findByItinerarioIdAndEscolaIdEscola(routeId, school.getId())
                .ifPresent(e -> {
                    throw new RouteSchoolNotFound("School já está vinculada a este itinerário");
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
                .findByItinerarioIdAndEscolaIdEscola(routeId, schoolId)
                .orElseThrow(() -> new RuntimeException("School não encontrada no itinerário"));

        routeSchoolRepository.delete(entity);
    }

    @Transactional
    public void reorder(UUID routeId, List<UUID> newSchoolIdsOrder) {
        List<RouteSchool> actual = routeSchoolRepository.findByItinerarioId(routeId);

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
