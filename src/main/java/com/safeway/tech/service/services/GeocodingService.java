package com.safeway.tech.service.services;

import com.google.maps.GeoApiContext;
import com.google.maps.GeocodingApi;
import com.google.maps.model.GeocodingResult;
import com.google.maps.model.LatLng;
import com.safeway.tech.infra.exception.AddressNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class GeocodingService {

    private final GeoApiContext context;

    public LatLng obterCoordenadas(String endereco) {
        try {
            GeocodingResult[] results = GeocodingApi.geocode(context, endereco).await();

            if (results != null && results.length > 0) {
                return results[0].geometry.location;
            }

            throw new AddressNotFoundException("Não foi possível encontrar o endereço com as informações: " + endereco);
        } catch (Exception e) {
            throw new RuntimeException("Erro ao buscar coordenadas: " + e.getMessage(), e);
        }
    }
}
