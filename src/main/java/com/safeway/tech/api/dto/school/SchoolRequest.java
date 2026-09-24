package com.safeway.tech.api.dto.school;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.safeway.tech.api.dto.address.AddressRequest;
import com.safeway.tech.domain.enums.EducationLevelEnum;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

@JsonIgnoreProperties(ignoreUnknown = true)
public record SchoolRequest(
        @NotBlank @Size(max = 100) String name,
        @NotNull EducationLevelEnum educationLevel,
        @NotNull @Valid AddressRequest address
) {
}
