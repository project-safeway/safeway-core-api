package com.safeway.tech.api.dto.route;

import com.safeway.tech.api.dto.address.AddressResponse;

import java.util.UUID;

public record RouteStopResponse(
        String type,          // "ALUNO" or "ESCOLA", same values accepted by RouteUpdateRequest.stops
        UUID id,              // studentId or schoolId
        String name,
        Integer generalOrder,
        Integer specificOrder, // boardingOrder or stopOrder
        String schoolName,
        String classroom,
        String guardianName,
        AddressResponse address
) {
}
