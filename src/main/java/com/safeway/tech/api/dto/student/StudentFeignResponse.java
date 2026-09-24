package com.safeway.tech.api.dto.student;

import java.util.UUID;

public record StudentFeignResponse(
        UUID id,
        String name,
        Double monthlyFee,
        Integer dueDate,
        Boolean active
) {
}
