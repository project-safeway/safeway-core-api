package com.safeway.tech.service.services;

import com.safeway.tech.api.dto.itinerario.ItinerarioRequest;
import com.safeway.tech.api.dto.itinerario.ItinerarioUpdateRequest;
import com.safeway.tech.api.dto.itinerario.ItinerarioUpdateRequest.ItinerarioParadaUpdate;
import com.safeway.tech.domain.models.Itinerario;
import com.safeway.tech.domain.models.ItinerarioAluno;
import com.safeway.tech.domain.models.ItinerarioEscola;
import com.safeway.tech.domain.models.Transport;
import com.safeway.tech.infra.exception.ItinerarioNotFoundException;
import com.safeway.tech.repository.ItinerarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ItinerarioService {

    private final ItinerarioRepository itinerarioRepository;
    private final TransporteService transporteService;
    private final ItinerarioAlunoService itinerarioAlunoService;
    private final ItinerarioEscolaService itinerarioEscolaService;
    private final CurrentUserService currentUserService;

    public List<Itinerario> listarTodos() {
        UUID transporteId = currentUserService.getCurrentTransporteId();
        return itinerarioRepository.findAllByTransporte(transporteId);
    }

    public Itinerario buscarPorId(UUID id) {
        return itinerarioRepository.findById(id)
                .orElseThrow(() -> new ItinerarioNotFoundException("Itinerário não encontrado"));
    }

    @Transactional
    public void desativar(UUID id) {
        Itinerario itinerario = buscarPorId(id);
        itinerario.setAtivo(false);
        itinerarioRepository.save(itinerario);
    }

    @Transactional
    public Itinerario criar(ItinerarioRequest request) {

        Itinerario itinerario = new Itinerario();

        itinerario.setNome(request.nome());
        itinerario.setHorarioInicio(request.horarioInicio());
        itinerario.setHorarioFim(request.horarioFim());
        itinerario.setTipoViagem(request.tipoViagem());

        UUID transporteId = currentUserService.getCurrentTransporteId();
        Transport transport = transporteService.buscarPorId(transporteId);
        itinerario.setTransport(transport);

        return itinerarioRepository.save(itinerario);
    }

    @Transactional
    public Itinerario atualizar(UUID id, ItinerarioUpdateRequest request) {
        Itinerario itinerario = buscarPorId(id);

        itinerario.setNome(request.nome());
        itinerario.setHorarioInicio(request.horarioInicio());
        itinerario.setHorarioFim(request.horarioFim());
        itinerario.setTipoViagem(request.tipoViagem());
        itinerario.setAtivo(request.ativo());

        if (request.alunos() != null && !request.alunos().isEmpty()) {
            itinerarioAlunoService.sincronizarAlunos(itinerario, request.alunos());
        }

        if (request.paradas() != null && !request.paradas().isEmpty()) {
            List<ItinerarioAluno> alunosAtuais = itinerarioAlunoService.buscarPorItinerarioId(itinerario.getId());
            List<ItinerarioEscola> escolasAtuais = itinerarioEscolaService.buscarPorItinerarioId(itinerario.getId());

            Map<UUID, ItinerarioAluno> alunosPorId = alunosAtuais.stream()
                    .collect(Collectors.toMap(a -> a.getStudent().getId(), a -> a));

            Map<UUID, ItinerarioEscola> escolasPorId = escolasAtuais.stream()
                    .collect(Collectors.toMap(e -> e.getSchool().getId(), e -> e));

            for (ItinerarioParadaUpdate parada : request.paradas()) {
                if (parada == null || parada.id() == null) {
                    continue;
                }
                if ("ALUNO".equalsIgnoreCase(parada.tipo())) {
                    ItinerarioAluno ia = alunosPorId.get(parada.id());
                    if (ia != null) {
                        ia.setOrdemGlobal(parada.ordemGlobal());
                        ia.setOrdemEmbarque(parada.ordemEspecifica());
                    }
                } else if ("ESCOLA".equalsIgnoreCase(parada.tipo())) {
                    ItinerarioEscola ie = escolasPorId.get(parada.id());
                    if (ie != null) {
                        ie.setOrdemGlobal(parada.ordemGlobal());
                        ie.setOrdemParada(parada.ordemEspecifica());
                    }
                }
            }

            itinerarioAlunoService.salvarTodos(alunosAtuais);
            itinerarioEscolaService.salvarTodos(escolasAtuais);
        }

        return itinerarioRepository.save(itinerario);
    }

}
