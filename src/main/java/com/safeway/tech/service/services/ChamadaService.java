package com.safeway.tech.service.services;

import com.safeway.tech.domain.enums.StatusChamadaEnum;
import com.safeway.tech.domain.models.Chamada;
import com.safeway.tech.domain.models.Route;
import com.safeway.tech.repository.ChamadaRepository;
import com.safeway.tech.repository.specification.ChamadaSpecs;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ChamadaService {

    private final ChamadaRepository chamadaRepository;
    private final ItinerarioService itinerarioService;
    private final CurrentUserService currentUserService;

    public Chamada buscarPorId(UUID id) {
        return chamadaRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Chamada não encontrada"));
    }

    public Chamada buscarChamadaAtivaPorItinerario(UUID idItinerario) {
        return chamadaRepository.findByItinerarioIdAndStatus(idItinerario, StatusChamadaEnum.EM_ANDAMENTO)
                .orElse(null);
    }

    public Chamada iniciarChamada(UUID idItinerario) {
        Chamada chamadaExistente = buscarChamadaAtivaPorItinerario(idItinerario);
        if (chamadaExistente != null) {
            return chamadaExistente;
        }

        Route route = itinerarioService.buscarPorId(idItinerario);

        Chamada chamada = new Chamada();
        chamada.setRoute(route);
        chamada.setStatus(StatusChamadaEnum.EM_ANDAMENTO);

        chamada = chamadaRepository.save(chamada);

        return chamada;
    }

    public Chamada atualizarChamada(UUID idItinerario, StatusChamadaEnum statusChamada) {
        Chamada chamada = buscarChamadaAtivaPorItinerario(idItinerario);
        if (chamada == null) {
            throw new RuntimeException("Nenhuma chamada em andamento encontrada para este itinerário");
        }

        chamada.setStatus(statusChamada);

        chamada = chamadaRepository.save(chamada);

        return chamada;
    }

    public Page<Chamada> buscarHistoricoChamadas(UUID idItinerario, List<StatusChamadaEnum> status, Pageable pageable) {
        UUID transporteId = currentUserService.getCurrentTransporteId();
        UUID userId = currentUserService.getCurrentUserId();

        Specification<Chamada> specs = Specification.allOf(
                ChamadaSpecs.comItinerarioId(idItinerario),
                ChamadaSpecs.comStatus(status),
                ChamadaSpecs.comTransporte(transporteId),
                ChamadaSpecs.comUsuario(userId)
        );

        return chamadaRepository.findAll(specs, pageable);
    }
}
