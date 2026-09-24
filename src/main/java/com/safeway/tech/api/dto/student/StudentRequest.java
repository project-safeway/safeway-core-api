package com.safeway.tech.api.dto.student;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.safeway.tech.api.dto.guardian.GuardianRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Positive;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@JsonIgnoreProperties(ignoreUnknown = true)
public record StudentRequest(
        @NotBlank String name,
        @NotBlank String professor,
        @Past LocalDate birthdate,
        Integer grade,
        String classroom,
        @NotNull @Positive Double monthlyFee,
        @NotNull @Min(1) @Max(31) Integer dueDate,
        @NotNull @NotEmpty @Valid List<GuardianRequest> guardian,
        @NotNull UUID schoolId
) {
}
