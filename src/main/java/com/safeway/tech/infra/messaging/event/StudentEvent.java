package com.safeway.tech.infra.messaging.event;

import com.fasterxml.jackson.annotation.JsonFormat;

import java.time.LocalDateTime;
import java.util.UUID;

public record StudentEvent(
        UUID id,
        UUID studentId,
        UUID userId,
        String name,
        Double monthlyFee,
        Integer dueDate,
        Boolean active,
        String type,
        @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
        LocalDateTime timestamp
) {
}
