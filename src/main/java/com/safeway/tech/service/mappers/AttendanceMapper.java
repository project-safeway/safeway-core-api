package com.safeway.tech.service.mappers;

import com.safeway.tech.api.dto.attendance.AttendancePresenceResponse;
import com.safeway.tech.api.dto.attendance.AttendanceResponse;
import com.safeway.tech.domain.models.Attendance;
import com.safeway.tech.domain.models.AttendanceStudent;

import java.util.List;

public class AttendanceMapper {

    public static AttendanceResponse toResponse(Attendance attendance, List<AttendanceStudent> presences) {

        return new AttendanceResponse(
                attendance.getId(),
                RouteMapper.toResponse(attendance.getRoute()),
                attendance.getStatus(),
                attendance.getCreatedAt(),
                presences.stream().map(AttendanceMapper::toPresenceResponse).toList()
        );
    }

    public static AttendancePresenceResponse toPresenceResponse(AttendanceStudent attendanceStudent) {
        return new AttendancePresenceResponse(
                attendanceStudent.getStudent().getId(),
                attendanceStudent.getStudent().getName(),
                attendanceStudent.getStudent().getSchool().getName(),
                attendanceStudent.getStudent().getClassroom(),
                attendanceStudent.getPresence(),
                attendanceStudent.getDate()
        );
    }

}
