package com.safeway.tech.service.mappers;

import com.safeway.tech.api.dto.transport.TransportResponse;
import com.safeway.tech.domain.models.Transport;

public class TransportMapper {

    public static TransportResponse toResponse(Transport transport) {
        return new TransportResponse(
                transport.getId(),
                transport.getLicensePlate(),
                transport.getModel(),
                transport.getCapacity()
        );
    }

}
