package com.safeway.tech.service.services;

import com.safeway.tech.domain.enums.AttendanceStatusEnum;
import com.safeway.tech.domain.models.Attendance;
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

    public Attendance buscarPorId(UUID id) {
        return chamadaRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Chamada não encontrada"));
    }

    public Attendance buscarChamadaAtivaPorItinerario(UUID idItinerario) {
        return chamadaRepository.findByItinerarioIdAndStatus(idItinerario, AttendanceStatusEnum.IN_PROGRESS)
                .orElse(null);
    }

    public Attendance iniciarChamada(UUID idItinerario) {
        Attendance attendanceExistente = buscarChamadaAtivaPorItinerario(idItinerario);
        if (attendanceExistente != null) {
            return attendanceExistente;
        }

        Route route = itinerarioService.buscarPorId(idItinerario);

        Attendance attendance = new Attendance();
        attendance.setRoute(route);
        attendance.setStatus(AttendanceStatusEnum.IN_PROGRESS);

        attendance = chamadaRepository.save(attendance);

        return attendance;
    }

    public Attendance atualizarChamada(UUID idItinerario, AttendanceStatusEnum statusChamada) {
        Attendance attendance = buscarChamadaAtivaPorItinerario(idItinerario);
        if (attendance == null) {
            throw new RuntimeException("Nenhuma chamada em andamento encontrada para este itinerário");
        }

        attendance.setStatus(statusChamada);

        attendance = chamadaRepository.save(attendance);

        return attendance;
    }

    public Page<Attendance> buscarHistoricoChamadas(UUID idItinerario, List<AttendanceStatusEnum> status, Pageable pageable) {
        UUID transporteId = currentUserService.getCurrentTransporteId();
        UUID userId = currentUserService.getCurrentUserId();

        Specification<Attendance> specs = Specification.allOf(
                ChamadaSpecs.comItinerarioId(idItinerario),
                ChamadaSpecs.comStatus(status),
                ChamadaSpecs.comTransporte(transporteId),
                ChamadaSpecs.comUsuario(userId)
        );

        return chamadaRepository.findAll(specs, pageable);
    }
}
