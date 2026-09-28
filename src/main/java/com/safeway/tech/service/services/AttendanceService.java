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

    public Attendance findById(UUID id) {
        return attendanceRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Chamada não encontrada"));
    }

    public Attendance findActiveByRoute(UUID routeId) {
        return attendanceRepository.findByRouteIdAndStatus(routeId, AttendanceStatusEnum.IN_PROGRESS)
                .orElse(null);
    }

    public Attendance startAttendance(UUID routeId) {
        Attendance attendanceExistente = findActiveByRoute(routeId);

        if (attendanceExistente != null) {
            return attendanceExistente;
        }

        Route route = routeService.findById(routeId);

        Attendance attendance = new Attendance();
        attendance.setRoute(route);
        attendance.setStatus(AttendanceStatusEnum.IN_PROGRESS);

        attendance = attendanceRepository.save(attendance);

        return attendance;
    }

    public Attendance updateAttendance(UUID routeId, AttendanceStatusEnum statusChamada) {
        Attendance attendance = findActiveByRoute(routeId);
        if (attendance == null) {
            throw new RuntimeException("Nenhuma chamada em andamento encontrada para este itinerário");
        }

        attendance.setStatus(statusChamada);

        attendance = attendanceRepository.save(attendance);

        return attendance;
    }

    public Page<Attendance> findAttendanceHistory(UUID routeId, List<AttendanceStatusEnum> status, Pageable pageable) {
        Specification<Attendance> specs = Specification.allOf(
                AttendanceSpecs.withRouteId(routeId),
                AttendanceSpecs.withStatus(status)
        );

        return attendanceRepository.findAll(specs, pageable);
    }
}
