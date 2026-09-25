package com.safeway.tech.api.controllers;

import com.safeway.tech.api.dto.attendance.AttendanceResponse;
import com.safeway.tech.domain.enums.AttendanceStatusEnum;
import com.safeway.tech.domain.enums.PresenceStatusEnum;
import com.safeway.tech.domain.models.Attendance;
import com.safeway.tech.service.mappers.AttendanceMapper;
import com.safeway.tech.service.services.AttendanceStudentService;
import com.safeway.tech.service.services.AttendanceService;
import jakarta.websocket.server.PathParam;
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
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RequestMapping("/chamada")
@RestController
@RequiredArgsConstructor
public class AttendanceController {

    private final AttendanceService attendanceService;
    private final AttendanceStudentService attendanceStudentService;

    @PostMapping("/iniciar/{id}")
    public ResponseEntity<AttendanceResponse> iniciarChamada(@PathVariable UUID id) {
        Attendance attendance = attendanceService.iniciarChamada(id);
        AttendanceResponse response = AttendanceMapper.toResponse(attendance);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @PutMapping("/alterar/{id}")
    public ResponseEntity<AttendanceResponse> alterarChamada(@PathVariable UUID id, @PathParam("status") AttendanceStatusEnum status) {
        Attendance attendance = attendanceService.atualizarChamada(id, status);
        AttendanceResponse response = AttendanceMapper.toResponse(attendance);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @PutMapping("/{id}/registrar-presenca")
    public ResponseEntity<Void> registrarPresenca(
            @PathVariable UUID id,
            @RequestBody Map<UUID, PresenceStatusEnum> presencas) {
        attendanceStudentService.registrarPresenca(presencas, id);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

    @GetMapping("/historico/{id}")
    public ResponseEntity<Page<AttendanceResponse>> historicoChamada(
            @PathVariable UUID id,
            @RequestParam(required = false) List<AttendanceStatusEnum> status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id,desc") String[] sort) {

        Sort.Direction direction = Sort.Direction.DESC;
        String property = "id";

        if (sort.length > 0) {
            property = sort[0];
            if (sort.length > 1) {
                direction = Sort.Direction.fromString(sort[1]);
            }
        }

        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, property));

        Page<Attendance> chamadas = attendanceService.buscarHistoricoChamadas(id, status, pageable);
        Page<AttendanceResponse> response = chamadas.map(AttendanceMapper::toResponse);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

}
