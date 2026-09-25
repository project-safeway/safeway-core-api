package com.safeway.tech.service.services;

import com.safeway.tech.api.dto.route.ItinerarioEscolaRequest;
import com.safeway.tech.domain.models.Address;
import com.safeway.tech.domain.models.School;
import com.safeway.tech.domain.models.Route;
import com.safeway.tech.domain.models.RouteSchool;
import com.safeway.tech.infra.exception.EnderecoNotFoundException;
import com.safeway.tech.infra.exception.ItinerarioEscolaNotFound;
import com.safeway.tech.infra.exception.ItinerarioNotFoundException;
import com.safeway.tech.repository.RouteSchoolRepository;
import com.safeway.tech.repository.RouteRepository;
import lombok.RequiredArgsConstructor;
import org.apache.coyote.BadRequestException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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

    public List<RouteSchool> buscarPorItinerarioId(UUID itinerarioId) {
        return routeSchoolRepository.findByItinerarioId(itinerarioId);
    }

    public void salvarTodos(List<RouteSchool> escolas) {
        routeSchoolRepository.saveAll(escolas);
    }

    @Transactional
    public void adicionarEscola(UUID itinerarioId, ItinerarioEscolaRequest request) throws BadRequestException {
        Route route = routeRepository.findById(itinerarioId)
                .orElseThrow(() -> new ItinerarioNotFoundException("Itinerário não encontrado"));

        School school = schoolService.buscarPorId(request.escolaId());

        Address address;
        if (request.enderecoId() != null) {
            address = addressService.buscarPorId(request.enderecoId());
        } else {
            address = school.getAddress();
        }

        if (address.getLatitude() == null || address.getLongitude() == null) {
            throw new EnderecoNotFoundException("Endereço da school não possui latitude/longitude válidas");
        }
        double lat = address.getLatitude();
        double lng = address.getLongitude();
        if (lat < -90 || lat > 90 || lng < -180 || lng > 180) {
            // TODO: Alteração para exception personalizada
            throw new BadRequestException("Coordenadas do endereço da school inválidas: " + lat + ", " + lng);
        }

        routeSchoolRepository.findByItinerarioIdAndEscolaIdEscola(itinerarioId, school.getId())
                .ifPresent(e -> {
                    throw new ItinerarioEscolaNotFound("School já está vinculada a este itinerário");
                });

        RouteSchool entity = new RouteSchool();
        entity.setRoute(route);
        entity.setSchool(school);
        entity.setAddress(address);
        entity.setOrdemParada(request.ordemParada());

        routeSchoolRepository.save(entity);
    }

    @Transactional
    public void removerEscola(UUID itinerarioId, UUID escolaId) {
        RouteSchool entity = routeSchoolRepository
                .findByItinerarioIdAndEscolaIdEscola(itinerarioId, escolaId)
                .orElseThrow(() -> new RuntimeException("School não encontrada no itinerário"));

        routeSchoolRepository.delete(entity);
    }

    @Transactional
    public void reordenar(UUID itinerarioId, List<UUID> novaOrdemEscolaIds) {
        List<RouteSchool> atuais = routeSchoolRepository.findByItinerarioId(itinerarioId);

        Map<UUID, RouteSchool> map = atuais.stream()
                .collect(Collectors.toMap(e -> e.getSchool().getId(), e -> e));

        int ordem = 1;
        for (UUID id : novaOrdemEscolaIds) {
            RouteSchool ie = map.get(id);
            if (ie != null) {
                ie.setOrdemParada(ordem++);
            }
        }

        routeSchoolRepository.saveAll(atuais);
    }
}
