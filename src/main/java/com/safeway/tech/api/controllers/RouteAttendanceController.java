package com.safeway.tech.api.controllers;

import com.safeway.tech.api.dto.attendance.AttendanceResponse;
import com.safeway.tech.domain.enums.AttendanceStatusEnum;
import com.safeway.tech.domain.models.Attendance;
import com.safeway.tech.service.mappers.AttendanceMapper;
import com.safeway.tech.service.services.AttendanceService;
import com.safeway.tech.service.services.AttendanceStudentService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/routes/{routeId}/attendances")
@RequiredArgsConstructor
public class RouteAttendanceController {

    private final AttendanceService attendanceService;
    private final AttendanceStudentService attendanceStudentService;

    @PostMapping
    public ResponseEntity<AttendanceResponse> startAttendance(@PathVariable UUID routeId) {
        Attendance attendance = attendanceService.startAttendance(routeId);
        return ResponseEntity.status(HttpStatus.CREATED).body(toResponse(attendance));
    }

    @GetMapping
    public ResponseEntity<Page<AttendanceResponse>> getAttendanceHistory(
            @PathVariable UUID routeId,
            @RequestParam(required = false) List<AttendanceStatusEnum> status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id,desc") String[] sort) {
        Sort.Direction direction = sort.length > 1
                ? Sort.Direction.fromString(sort[1])
                : Sort.Direction.DESC;
        String property = sort.length > 0 ? sort[0] : "id";
        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, property));

        Page<AttendanceResponse> response = attendanceService
                .findAttendanceHistory(routeId, status, pageable)
                .map(this::toResponse);
        return ResponseEntity.ok(response);
    }

    private AttendanceResponse toResponse(Attendance attendance) {
        return AttendanceMapper.toResponse(attendance, attendanceStudentService.findByAttendanceId(attendance.getId()));
    }
}
