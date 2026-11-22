package com.safeway.tech.service.services;

import com.google.maps.model.LatLng;
import com.safeway.tech.api.dto.address.AddressRequest;
import com.safeway.tech.domain.models.Address;
import com.safeway.tech.repository.AddressRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AddressServiceTest {

    @Mock
    private AddressRepository addressRepository;

    @Mock
    private GeocodingService geocodingService;

    private AddressService addressService;

    @BeforeEach
    void setUp() {
        addressService = new AddressService(addressRepository, geocodingService);
    }

    @Test
    void createShouldPersistPrincipalFlag() {
        AddressRequest request = new AddressRequest(
                "Rua A", "10", null, "Centro", "Sao Caetano", "SP", "09561000", "Residential", true
        );
        when(geocodingService.getCoordinates(anyString())).thenReturn(new LatLng(-23.6, -46.5));
        when(addressRepository.save(any(Address.class))).thenAnswer(inv -> inv.getArgument(0));

        Address saved = addressService.create(request);

        assertTrue(saved.getPrincipal());
        verify(geocodingService).getCoordinates(anyString());
    }

    @Test
    void updateShouldPersistPrincipalWithoutReGeocodingWhenAddressTextIsUnchanged() {
        UUID addressId = UUID.randomUUID();
        Address existing = new Address();
        existing.setStreet("Rua A");
        existing.setNumber("10");
        existing.setNeighborhood("Centro");
        existing.setCity("Sao Caetano");
        existing.setFederalUnit("SP");
        existing.setZipCode("09561000");
        existing.setType("Residential");
        existing.setLatitude(BigDecimal.valueOf(-23.6));
        existing.setLongitude(BigDecimal.valueOf(-46.5));
        existing.setPrincipal(false);

        AddressRequest request = new AddressRequest(
                "Rua A", "10", "Apto 2", "Centro", "Sao Caetano", "SP", "09561000", "Residential", true
        );
        when(addressRepository.findById(addressId)).thenReturn(Optional.of(existing));
        when(addressRepository.save(any(Address.class))).thenAnswer(inv -> inv.getArgument(0));

        Address updated = addressService.update(addressId, request);

        assertTrue(updated.getPrincipal());
        assertEquals("Apto 2", updated.getAdditionalDetails());
        verify(geocodingService, never()).getCoordinates(anyString());
    }

    @Test
    void updateShouldAllowUnsettingPrincipalAndReGeocodeWhenStreetChanges() {
        UUID addressId = UUID.randomUUID();
        Address existing = new Address();
        existing.setStreet("Rua A");
        existing.setNumber("10");
        existing.setNeighborhood("Centro");
        existing.setCity("Sao Caetano");
        existing.setFederalUnit("SP");
        existing.setZipCode("09561000");
        existing.setType("Residential");
        existing.setLatitude(BigDecimal.valueOf(-23.6));
        existing.setLongitude(BigDecimal.valueOf(-46.5));
        existing.setPrincipal(true);

        AddressRequest request = new AddressRequest(
                "Rua B", "20", null, "Centro", "Sao Caetano", "SP", "09561000", "Residential", false
        );
        when(addressRepository.findById(addressId)).thenReturn(Optional.of(existing));
        when(geocodingService.getCoordinates(anyString())).thenReturn(new LatLng(-23.7, -46.6));
        when(addressRepository.save(any(Address.class))).thenAnswer(inv -> inv.getArgument(0));

        Address updated = addressService.update(addressId, request);

        assertFalse(updated.getPrincipal());
        verify(geocodingService).getCoordinates(anyString());
    }
}
