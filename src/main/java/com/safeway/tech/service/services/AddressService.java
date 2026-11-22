package com.safeway.tech.service.services;

import com.google.maps.model.LatLng;
import com.safeway.tech.api.dto.address.AddressRequest;
import com.safeway.tech.domain.models.Address;
import com.safeway.tech.infra.exception.EntityNotFoundException;
import com.safeway.tech.repository.AddressRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Objects;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AddressService {

    private final AddressRepository addressRepository;
    private final GeocodingService geocodingService;

    public Address findById(UUID id) {
        return addressRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Address with ID " + id + " was not found"));
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
        boolean addressTextChanged = !Objects.equals(address.getStreet(), request.street())
                || !Objects.equals(address.getNumber(), request.number())
                || !Objects.equals(address.getNeighborhood(), request.neighborhood())
                || !Objects.equals(address.getCity(), request.city())
                || !Objects.equals(address.getFederalUnit(), request.federalUnit())
                || !Objects.equals(address.getZipCode(), request.zipCode());
        boolean missingCoordinates = address.getLatitude() == null || address.getLongitude() == null;

        consumeData(address, request);
        if (addressTextChanged || missingCoordinates) {
            calculateCoordinates(address);
        }

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
        if (request.principal() != null) {
            address.setPrincipal(request.principal());
        }
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
