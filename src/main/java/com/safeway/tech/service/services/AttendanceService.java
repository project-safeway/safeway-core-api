package com.safeway.tech.service.services;

import com.safeway.tech.domain.enums.AttendanceStatusEnum;
import com.safeway.tech.domain.models.Attendance;
import com.safeway.tech.domain.models.Route;
import com.safeway.tech.repository.AttendanceRepository;
import com.safeway.tech.repository.specification.AttendanceSpecs;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AttendanceService {

    private final AttendanceRepository attendanceRepository;
    private final RouteService routeService;
    private final CurrentUserService currentUserService;

    public Attendance buscarPorId(UUID id) {
        return attendanceRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Chamada não encontrada"));
    }

    public Attendance buscarChamadaAtivaPorItinerario(UUID idItinerario) {
        return attendanceRepository.findByItinerarioIdAndStatus(idItinerario, AttendanceStatusEnum.IN_PROGRESS)
                .orElse(null);
    }

    public Attendance iniciarChamada(UUID idItinerario) {
        Attendance attendanceExistente = buscarChamadaAtivaPorItinerario(idItinerario);
        if (attendanceExistente != null) {
            return attendanceExistente;
        }

        Route route = routeService.buscarPorId(idItinerario);

        Attendance attendance = new Attendance();
        attendance.setRoute(route);
        attendance.setStatus(AttendanceStatusEnum.IN_PROGRESS);

        attendance = attendanceRepository.save(attendance);

        return attendance;
    }

    public Attendance atualizarChamada(UUID idItinerario, AttendanceStatusEnum statusChamada) {
        Attendance attendance = buscarChamadaAtivaPorItinerario(idItinerario);
        if (attendance == null) {
            throw new RuntimeException("Nenhuma chamada em andamento encontrada para este itinerário");
        }

        attendance.setStatus(statusChamada);

        attendance = attendanceRepository.save(attendance);

        return attendance;
    }

    public Page<Attendance> buscarHistoricoChamadas(UUID idItinerario, List<AttendanceStatusEnum> status, Pageable pageable) {
        UUID transporteId = currentUserService.getCurrentTransporteId();
        UUID userId = currentUserService.getCurrentUserId();

        Specification<Attendance> specs = Specification.allOf(
                AttendanceSpecs.comItinerarioId(idItinerario),
                AttendanceSpecs.comStatus(status),
                AttendanceSpecs.comTransporte(transporteId),
                AttendanceSpecs.comUsuario(userId)
        );

        return attendanceRepository.findAll(specs, pageable);
    }
}
