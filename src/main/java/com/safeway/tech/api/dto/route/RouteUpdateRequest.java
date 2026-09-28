package com.safeway.tech.api.dto.route;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.safeway.tech.domain.enums.RouteTypeEnum;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.sql.Time;
import java.util.List;
import java.util.UUID;

@JsonIgnoreProperties(ignoreUnknown = true)
public record RouteUpdateRequest(
        @NotBlank String name,
        Time startTime,
        Time endTime,
        @NotNull RouteTypeEnum routeType,
        @NotNull Boolean active,
        List<@Valid RouteStudentRequest> students,
        List<RouteStopUpdate> stops
) {
    public record RouteStopUpdate(
            String type,      // "ALUNO" ou "ESCOLA"
            UUID id,          // studentId ou schoolId
            Integer generalOrder,
            Integer specificOrder // boardingOrder ou ordemVisita
    ) {
    }
}
