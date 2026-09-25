package com.safeway.tech.service.services;

import com.google.maps.model.LatLng;
import com.safeway.tech.api.dto.address.AddressRequest;
import com.safeway.tech.domain.models.Address;
import com.safeway.tech.domain.models.Guardian;
import com.safeway.tech.infra.exception.AddressNotFoundException;
import com.safeway.tech.repository.AddressRepository;
import com.safeway.tech.repository.GuardianRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AddressService {

    private final AddressRepository addressRepository;
    private final GuardianRepository guardianRepository;
    private final GeocodingService geocodingService;
    private final CurrentUserService currentUserService;

    public Address buscarPorId(UUID id) {
        return addressRepository.findById(id)
                .orElseThrow(() -> new AddressNotFoundException("Endereço com ID " + id + " não encontrado"));
    }

    @Transactional(readOnly = true)
    public List<Address> listarEnderecosDisponiveis(UUID alunoId) {
        UUID userId = currentUserService.getCurrentUserId();
        List<Guardian> responsaveis = guardianRepository.findByAlunosIdAndUsuarioIdUsuario(alunoId, userId);

        return responsaveis.stream()
                .map(Guardian::getAddress)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    @Transactional
    public Address criar(AddressRequest request) {
        Address address = new Address();

        aplicarDados(address, request);

        calcularCoordenadas(address);
        return addressRepository.save(address);
    }

    public Address atualizar(UUID id, AddressRequest request) {
        Address address = buscarPorId(id);

        aplicarDados(address, request);
        calcularCoordenadas(address);

        return addressRepository.save(address);
    }

    public void desativar(UUID id) {
        Address address = buscarPorId(id);
        address.setAtivo(false);
        addressRepository.save(address);
    }

    private void aplicarDados(Address address, AddressRequest request) {
        address.setLogradouro(request.logradouro());
        address.setNumero(request.numero());
        address.setComplemento(request.complemento());
        address.setBairro(request.bairro());
        address.setCidade(request.cidade());
        address.setUf(request.uf());
        address.setCep(request.cep());
        address.setTipo(request.tipo());
    }

    private void calcularCoordenadas(Address address) {
        String enderecoCompleto = String.format("%s, %s, %s, %s, %s, %s",
                address.getLogradouro(),
                address.getNumero(),
                address.getBairro(),
                address.getCidade(),
                address.getUf(),
                address.getCep()
        );

        LatLng coordenadas = geocodingService.obterCoordenadas(enderecoCompleto);
        address.setLatitude(coordenadas.lat);
        address.setLongitude(coordenadas.lng);
    }

}
