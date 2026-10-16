package com.safeway.tech.service.services;

import com.google.maps.model.LatLng;
import com.safeway.tech.api.dto.address.AddressRequest;
import com.safeway.tech.domain.models.Address;
import com.safeway.tech.infra.exception.AddressNotFoundException;
import com.safeway.tech.repository.AddressRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AddressService {

    private final AddressRepository addressRepository;
    private final GeocodingService geocodingService;

    public Address findById(UUID id) {
        return addressRepository.findById(id)
                .orElseThrow(() -> new AddressNotFoundException("Endereço com ID " + id + " não encontrado"));
    }

    @Transactional
    public Address create(AddressRequest request) {
        Address address = new Address();

        consumeData(address, request);
        calculateCoordinates(address);

        return addressRepository.save(address);
    }

    @Transactional
    public Address update(UUID id, AddressRequest request) {
        Address address = findById(id);

        consumeData(address, request);
        calculateCoordinates(address);

        return addressRepository.save(address);
    }

    private void consumeData(Address address, AddressRequest request) {
        address.setStreet(request.street());
        address.setNumber(request.number());
        address.setAdditionalDetails(request.additionalDetails());
        address.setNeighborhood(request.neighborhood());
        address.setCity(request.city());
        address.setFederalUnit(request.federalUnit());
        address.setZipCode(request.zipCode());
        address.setType(request.type());
    }

    private void calculateCoordinates(Address address) {
        String fullAddress = String.format("%s, %s, %s, %s, %s, %s",
                address.getStreet(),
                address.getNumber(),
                address.getNeighborhood(),
                address.getCity(),
                address.getFederalUnit(),
                address.getZipCode()
        );

        LatLng coordinates = geocodingService.getCoordinates(fullAddress);
        address.setLatitude(BigDecimal.valueOf(coordinates.lat));
        address.setLongitude(BigDecimal.valueOf(coordinates.lng));
    }

}
