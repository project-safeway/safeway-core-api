package com.safeway.tech.service.services;

import com.google.maps.model.LatLng;
import com.safeway.tech.api.dto.endereco.EnderecoRequest;
import com.safeway.tech.domain.models.Address;
import com.safeway.tech.domain.models.Guardian;
import com.safeway.tech.infra.exception.EnderecoNotFoundException;
import com.safeway.tech.repository.EnderecoRepository;
import com.safeway.tech.repository.ResponsavelRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class EnderecoService {

    private final EnderecoRepository enderecoRepository;
    private final ResponsavelRepository responsavelRepository;
    private final GeocodingService geocodingService;
    private final CurrentUserService currentUserService;

    public Address buscarPorId(UUID id) {
        return enderecoRepository.findById(id)
                .orElseThrow(() -> new EnderecoNotFoundException("Endereço com ID " + id + " não encontrado"));
    }

    @Transactional(readOnly = true)
    public List<Address> listarEnderecosDisponiveis(UUID alunoId) {
        UUID userId = currentUserService.getCurrentUserId();
        List<Guardian> responsaveis = responsavelRepository.findByAlunosIdAndUsuarioIdUsuario(alunoId, userId);

        return responsaveis.stream()
                .map(Guardian::getAddress)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    @Transactional
    public Address criar(EnderecoRequest request) {
        Address address = new Address();

        aplicarDados(address, request);

        calcularCoordenadas(address);
        return enderecoRepository.save(address);
    }

    public Address atualizar(UUID id, EnderecoRequest request) {
        Address address = buscarPorId(id);

        aplicarDados(address, request);
        calcularCoordenadas(address);

        return enderecoRepository.save(address);
    }

    public void desativar(UUID id) {
        Address address = buscarPorId(id);
        address.setAtivo(false);
        enderecoRepository.save(address);
    }

    private void aplicarDados(Address address, EnderecoRequest request) {
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
