package com.safeway.tech.service.services;

import com.safeway.tech.domain.enums.AttendanceStatusEnum;
import com.safeway.tech.domain.models.Attendance;
import com.safeway.tech.domain.models.Route;
import com.safeway.tech.infra.exception.EntityNotFoundException;
import com.safeway.tech.repository.AttendanceRepository;
import com.safeway.tech.repository.specification.AttendanceSpecs;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AttendanceService {

    private final AttendanceRepository attendanceRepository;
    private final RouteService routeService;

    public Attendance findById(UUID id) {
        Attendance attendance = attendanceRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Attendance was not found"));
        routeService.findById(attendance.getRoute().getId());
        return attendance;
    }

    public Attendance findActiveByRoute(UUID routeId) {
        return attendanceRepository.findByRouteIdAndStatus(routeId, AttendanceStatusEnum.IN_PROGRESS)
                .orElse(null);
    }

    @Transactional
    public Attendance startAttendance(UUID routeId) {
        Route route = routeService.findByIdForUpdate(routeId);
        Attendance existingAttendance = findActiveByRoute(routeId);

        if (existingAttendance != null) {
            return existingAttendance;
        }

        Attendance attendance = new Attendance();
        attendance.setRoute(route);
        attendance.setStatus(AttendanceStatusEnum.IN_PROGRESS);

        attendance = attendanceRepository.save(attendance);

        return attendance;
    }

    public Attendance updateAttendanceById(UUID attendanceId, AttendanceStatusEnum status) {
        Attendance attendance = findById(attendanceId);
        if (!AttendanceStatusEnum.IN_PROGRESS.equals(attendance.getStatus())) {
            throw new RuntimeException("Attendance is not in progress");
        }

        attendance.setStatus(status);
        return attendanceRepository.save(attendance);
    }

    public Page<Attendance> findAttendanceHistory(UUID routeId, List<AttendanceStatusEnum> status, Pageable pageable) {
        routeService.findById(routeId);
        Specification<Attendance> specs = Specification.allOf(
                AttendanceSpecs.withRouteId(routeId),
                AttendanceSpecs.withStatus(status)
        );

        return attendanceRepository.findAll(specs, pageable);
    }
}
