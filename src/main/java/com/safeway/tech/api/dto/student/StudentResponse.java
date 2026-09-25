package com.safeway.tech.api.dto.student;

import com.safeway.tech.api.dto.school.SchoolResumeResponse;

import java.time.LocalDate;
import java.util.UUID;

public record StudentResponse(
        UUID id,
        String name,
        String professor,
        LocalDate birthdate,
        Integer grade,
        String classroom,
        SchoolResumeResponse school,
        Double monthlyFee,
        Integer dueDate
) {
}
