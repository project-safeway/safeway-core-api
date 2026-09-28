package com.safeway.tech.service.services;

import com.safeway.tech.api.dto.transport.TransportRequest;
import com.safeway.tech.domain.models.Transport;
import com.safeway.tech.repository.TransportRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class TransportService {

    private final TransportRepository transportRepository;
    private final CurrentUserService currentUserService;

    public Transport getTransport() {
        UUID userId = currentUserService.getCurrentUserId();
        return transportRepository.findByUserId(userId);
    }

    public Transport saveTransport(TransportRequest request) {
        Transport transport = new Transport();

        applyData(transport, request);

        return transportRepository.save(transport);
    }

    public Transport updateTransport(TransportRequest request) {
        Transport transport = getTransport();

        applyData(transport, request);

        return transportRepository.save(transport);
    }

    public void deleteTransport() {
        Transport transport = getTransport();
        transportRepository.delete(transport);
    }

    private void applyData(Transport transport, TransportRequest request) {
        transport.setLicensePlate(request.licensePlate());
        transport.setModel(request.model());
        transport.setCapacity(request.capacity());
    }
}
