package com.safeway.tech.service.services;

import com.safeway.tech.api.dto.route.ItinerarioRequest;
import com.safeway.tech.api.dto.route.ItinerarioUpdateRequest;
import com.safeway.tech.api.dto.route.ItinerarioUpdateRequest.ItinerarioParadaUpdate;
import com.safeway.tech.domain.models.Route;
import com.safeway.tech.domain.models.RouteStudent;
import com.safeway.tech.domain.models.RouteSchool;
import com.safeway.tech.domain.models.Transport;
import com.safeway.tech.infra.exception.ItinerarioNotFoundException;
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

    public List<Route> listarTodos() {
        UUID transporteId = currentUserService.getCurrentTransporteId();
        return routeRepository.findAllByTransporte(transporteId);
    }

    public Route buscarPorId(UUID id) {
        return routeRepository.findById(id)
                .orElseThrow(() -> new ItinerarioNotFoundException("Itinerário não encontrado"));
    }

    @Transactional
    public void desativar(UUID id) {
        Route route = buscarPorId(id);
        route.setAtivo(false);
        routeRepository.save(route);
    }

    @Transactional
    public Route criar(ItinerarioRequest request) {

        Route route = new Route();

        route.setNome(request.nome());
        route.setHorarioInicio(request.horarioInicio());
        route.setHorarioFim(request.horarioFim());
        route.setTipoViagem(request.tipoViagem());

        UUID transporteId = currentUserService.getCurrentTransporteId();
        Transport transport = transportService.buscarPorId(transporteId);
        route.setTransport(transport);

        return routeRepository.save(route);
    }

    @Transactional
    public Route atualizar(UUID id, ItinerarioUpdateRequest request) {
        Route route = buscarPorId(id);

        route.setNome(request.nome());
        route.setHorarioInicio(request.horarioInicio());
        route.setHorarioFim(request.horarioFim());
        route.setTipoViagem(request.tipoViagem());
        route.setAtivo(request.ativo());

        if (request.alunos() != null && !request.alunos().isEmpty()) {
            routeStudentService.sincronizarAlunos(route, request.alunos());
        }

        if (request.paradas() != null && !request.paradas().isEmpty()) {
            List<RouteStudent> alunosAtuais = routeStudentService.buscarPorItinerarioId(route.getId());
            List<RouteSchool> escolasAtuais = routeSchoolService.buscarPorItinerarioId(route.getId());

            Map<UUID, RouteStudent> alunosPorId = alunosAtuais.stream()
                    .collect(Collectors.toMap(a -> a.getStudent().getId(), a -> a));

            Map<UUID, RouteSchool> escolasPorId = escolasAtuais.stream()
                    .collect(Collectors.toMap(e -> e.getSchool().getId(), e -> e));

            for (ItinerarioParadaUpdate parada : request.paradas()) {
                if (parada == null || parada.id() == null) {
                    continue;
                }
                if ("ALUNO".equalsIgnoreCase(parada.tipo())) {
                    RouteStudent ia = alunosPorId.get(parada.id());
                    if (ia != null) {
                        ia.setOrdemGlobal(parada.ordemGlobal());
                        ia.setOrdemEmbarque(parada.ordemEspecifica());
                    }
                } else if ("ESCOLA".equalsIgnoreCase(parada.tipo())) {
                    RouteSchool ie = escolasPorId.get(parada.id());
                    if (ie != null) {
                        ie.setOrdemGlobal(parada.ordemGlobal());
                        ie.setOrdemParada(parada.ordemEspecifica());
                    }
                }
            }

            routeStudentService.salvarTodos(alunosAtuais);
            routeSchoolService.salvarTodos(escolasAtuais);
        }

        return routeRepository.save(route);
    }

}
