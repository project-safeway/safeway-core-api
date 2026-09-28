package com.safeway.tech.api.dto.route;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.safeway.tech.domain.enums.RouteTypeEnum;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.sql.Time;

@JsonIgnoreProperties(ignoreUnknown = true)
public record RouteRequest(
        @NotBlank String name,
        Time startTime,
        Time endTime,
        @NotNull RouteTypeEnum routeType
) {
}
