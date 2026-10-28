package com.safeway.tech.service.mappers;

import com.safeway.tech.api.dto.route.RouteResponse;
import com.safeway.tech.api.dto.route.RouteStopResponse;
import com.safeway.tech.domain.models.Guardian;
import com.safeway.tech.domain.models.Route;
import com.safeway.tech.domain.models.RouteSchool;
import com.safeway.tech.domain.models.RouteStudent;

public class RouteMapper {

    public static RouteResponse toResponse(Route route) {
        return new RouteResponse(
                route.getId(),
                route.getName(),
                route.getStartTime(),
                route.getEndTime(),
                route.getRouteType(),
                route.isActive()
        );
    }

    public static RouteStopResponse toStopResponse(RouteStudent routeStudent, Guardian guardian) {
        return new RouteStopResponse(
                "ALUNO",
                routeStudent.getStudent().getId(),
                routeStudent.getStudent().getName(),
                routeStudent.getGeneralOrder(),
                routeStudent.getBoardingOrder(),
                routeStudent.getStudent().getSchool().getName(),
                routeStudent.getStudent().getClassroom(),
                guardian != null ? guardian.getName() : null,
                AddressMapper.toResponse(routeStudent.getAddress())
        );
    }

    public static RouteStopResponse toStopResponse(RouteSchool routeSchool) {
        return new RouteStopResponse(
                "ESCOLA",
                routeSchool.getSchool().getId(),
                routeSchool.getSchool().getName(),
                routeSchool.getGeneralOrder(),
                routeSchool.getStopOrder(),
                null,
                null,
                null,
                AddressMapper.toResponse(routeSchool.getAddress())
        );
    }
}
