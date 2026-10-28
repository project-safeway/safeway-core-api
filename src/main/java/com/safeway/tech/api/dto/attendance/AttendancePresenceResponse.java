package com.safeway.tech.api.dto.attendance;

import com.safeway.tech.domain.enums.PresenceStatusEnum;

import java.time.LocalDateTime;
import java.util.UUID;

public record AttendancePresenceResponse(
        UUID studentId,
        String studentName,
        String schoolName,
        String classroom,
        PresenceStatusEnum presence,
        LocalDateTime date
) {
}
