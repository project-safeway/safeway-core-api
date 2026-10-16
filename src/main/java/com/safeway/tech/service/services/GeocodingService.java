package com.safeway.tech.service.services;

import com.google.maps.GeoApiContext;
import com.google.maps.GeocodingApi;
import com.google.maps.model.GeocodingResult;
import com.google.maps.model.LatLng;
import com.safeway.tech.infra.exception.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class GeocodingService {

    private final GeoApiContext context;

    public LatLng getCoordinates(String address) {
        try {
            GeocodingResult[] results = GeocodingApi.geocode(context, address).await();

            if (results != null && results.length > 0) {
                return results[0].geometry.location;
            }

            throw new EntityNotFoundException("Address could not be found with the provided details: " + address);
        } catch (EntityNotFoundException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException("Failed to retrieve coordinates: " + e.getMessage(), e);
        }
    }
}
