package com.safeway.tech.service.services;

import com.safeway.tech.api.dto.itinerario.ItinerarioEscolaRequest;
import com.safeway.tech.domain.models.Address;
import com.safeway.tech.domain.models.School;
import com.safeway.tech.domain.models.Route;
import com.safeway.tech.domain.models.RouteSchool;
import com.safeway.tech.infra.exception.EnderecoNotFoundException;
import com.safeway.tech.infra.exception.ItinerarioEscolaNotFound;
import com.safeway.tech.infra.exception.ItinerarioNotFoundException;
import com.safeway.tech.repository.ItinerarioEscolaRepository;
import com.safeway.tech.repository.ItinerarioRepository;
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
public class ItinerarioEscolaService {

    private final ItinerarioEscolaRepository itinerarioEscolaRepository;
    private final ItinerarioRepository itinerarioRepository;
    private final EscolaService escolaService;
    private final EnderecoService enderecoService;

    public List<RouteSchool> buscarPorItinerarioId(UUID itinerarioId) {
        return itinerarioEscolaRepository.findByItinerarioId(itinerarioId);
    }

    public void salvarTodos(List<RouteSchool> escolas) {
        itinerarioEscolaRepository.saveAll(escolas);
    }

    @Transactional
    public void adicionarEscola(UUID itinerarioId, ItinerarioEscolaRequest request) throws BadRequestException {
        Route route = itinerarioRepository.findById(itinerarioId)
                .orElseThrow(() -> new ItinerarioNotFoundException("Itinerário não encontrado"));

        School school = escolaService.buscarPorId(request.escolaId());

        Address address;
        if (request.enderecoId() != null) {
            address = enderecoService.buscarPorId(request.enderecoId());
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

        itinerarioEscolaRepository.findByItinerarioIdAndEscolaIdEscola(itinerarioId, school.getId())
                .ifPresent(e -> {
                    throw new ItinerarioEscolaNotFound("School já está vinculada a este itinerário");
                });

        RouteSchool entity = new RouteSchool();
        entity.setRoute(route);
        entity.setSchool(school);
        entity.setAddress(address);
        entity.setOrdemParada(request.ordemParada());

        itinerarioEscolaRepository.save(entity);
    }

    @Transactional
    public void removerEscola(UUID itinerarioId, UUID escolaId) {
        RouteSchool entity = itinerarioEscolaRepository
                .findByItinerarioIdAndEscolaIdEscola(itinerarioId, escolaId)
                .orElseThrow(() -> new RuntimeException("School não encontrada no itinerário"));

        itinerarioEscolaRepository.delete(entity);
    }

    @Transactional
    public void reordenar(UUID itinerarioId, List<UUID> novaOrdemEscolaIds) {
        List<RouteSchool> atuais = itinerarioEscolaRepository.findByItinerarioId(itinerarioId);

        Map<UUID, RouteSchool> map = atuais.stream()
                .collect(Collectors.toMap(e -> e.getSchool().getId(), e -> e));

        int ordem = 1;
        for (UUID id : novaOrdemEscolaIds) {
            RouteSchool ie = map.get(id);
            if (ie != null) {
                ie.setOrdemParada(ordem++);
            }
        }

        itinerarioEscolaRepository.saveAll(atuais);
    }
}
