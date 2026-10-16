package com.safeway.tech.api.controllers;

import com.safeway.tech.api.dto.attendance.AttendanceResponse;
import com.safeway.tech.domain.enums.AttendanceStatusEnum;
import com.safeway.tech.domain.enums.PresenceStatusEnum;
import com.safeway.tech.domain.models.Attendance;
import com.safeway.tech.service.mappers.AttendanceMapper;
import com.safeway.tech.service.services.AttendanceService;
import com.safeway.tech.service.services.AttendanceStudentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;
import java.util.UUID;

@RequestMapping("/attendances")
@RestController
@RequiredArgsConstructor
public class AttendanceController {

    private final AttendanceService attendanceService;
    private final AttendanceStudentService attendanceStudentService;

    @PutMapping("/{attendanceId}/presences")
    public ResponseEntity<Void> updatePresences(
            @PathVariable UUID attendanceId,
            @RequestBody Map<UUID, PresenceStatusEnum> presences) {
        attendanceStudentService.registerAttendance(presences, attendanceId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{attendanceId}/finish")
    public ResponseEntity<AttendanceResponse> finishAttendance(@PathVariable UUID attendanceId) {
        return updateStatus(attendanceId, AttendanceStatusEnum.FINISHED);
    }

    @PostMapping("/{attendanceId}/cancel")
    public ResponseEntity<AttendanceResponse> cancelAttendance(@PathVariable UUID attendanceId) {
        return updateStatus(attendanceId, AttendanceStatusEnum.CANCELLED);
    }

    private ResponseEntity<AttendanceResponse> updateStatus(UUID attendanceId, AttendanceStatusEnum status) {
        Attendance attendance = attendanceService.updateAttendanceById(attendanceId, status);
        return ResponseEntity.ok(AttendanceMapper.toResponse(attendance));
    }
}
