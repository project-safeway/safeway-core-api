package com.safeway.tech.api.dto.guardian;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.safeway.tech.api.dto.address.AddressRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

@JsonIgnoreProperties(ignoreUnknown = true)
public record GuardianRequest(
        @NotBlank @Size(max = 45) String name,
        @NotBlank String primaryPhoneNumber,
        String secondaryPhoneNumber,
        @Email String email,
        @NotNull @Valid AddressRequest address
) {}

