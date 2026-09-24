package com.safeway.tech.service.mappers;

import com.safeway.tech.api.dto.attendance.AttendanceResponse;
import com.safeway.tech.domain.models.Attendance;

public class AttendanceMapper {

    public static AttendanceResponse toResponse(Attendance attendance) {

        return new AttendanceResponse(
                attendance.getId(),
                RouteMapper.toResponse(attendance.getRoute()),
                attendance.getStatus()
        );
    }

}
