package com.safeway.tech.api.dto.address;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@JsonIgnoreProperties(ignoreUnknown = true)
public record AddressRequest(
        @NotBlank @Size(max = 255) String street,
        @NotBlank @Size(max = 10) String number,
        @Size(max = 100) String additionalDetails,
        @NotBlank @Size(max = 100) String neighborhood,
        @NotBlank @Size(max = 100) String city,
        @NotBlank @Size(min = 2, max = 2) String federalUnit,
        @NotBlank @Size(max = 9) String zipCode,

        @NotBlank @Size(max = 50) String type,
        Boolean principal
) {
}
