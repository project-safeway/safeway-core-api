package com.safeway.tech.service.services;

import com.safeway.tech.api.dto.school.SchoolRequest;
import com.safeway.tech.domain.models.Address;
import com.safeway.tech.domain.models.School;
import com.safeway.tech.domain.models.User;
import com.safeway.tech.infra.exception.EscolaNotFoundException;
import com.safeway.tech.repository.EscolaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class SchoolService {

    private final EscolaRepository escolaRepository;
    private final AddressService addressService;
    private final UserService userService;
    private final CurrentUserService currentUserService;

    public List<School> listarEscolasComAlunos() {
        UUID usuarioId = currentUserService.getCurrentUserId();
        return escolaRepository.findByUsuarioIdUsuario(usuarioId);
    }

    @Transactional(readOnly = true)
    public School buscarPorId(UUID escolaId) {
        UUID usuarioId = currentUserService.getCurrentUserId();
        return escolaRepository.findByIdEscolaAndIdUsuario(escolaId, usuarioId)
                .orElseThrow(() -> new EscolaNotFoundException("School não encontrada"));
    }

    @Transactional(readOnly = true)
    public Address buscarEnderecoDaEscola(UUID escolaId) {
        UUID usuarioId = currentUserService.getCurrentUserId();
        School school = escolaRepository.findByIdEscolaAndIdUsuario(escolaId, usuarioId)
                .orElseThrow(() -> new EscolaNotFoundException("School não encontrada"));
        return school.getAddress();
    }

    @Transactional
    public School cadastrarEscola(SchoolRequest request) {
        School school = new School();
        aplicarDados(school, request);

        Address address = addressService.criar(request.endereco());
        school.setAddress(address);

        UUID usuarioId = currentUserService.getCurrentUserId();
        User user = userService.buscarPorId(usuarioId);
        school.setUser(user);

        return escolaRepository.save(school);
    }

    @Transactional
    public School atualizarEscola(UUID escolaId, SchoolRequest request) {
        School school = buscarPorId(escolaId);

        aplicarDados(school, request);
        Address address = addressService.atualizar(school.getAddress().getId(), request.endereco());
        school.setAddress(address);

        return escolaRepository.save(school);
    }

    @Transactional
    public void desativar(UUID escolaId) {
        School school = buscarPorId(escolaId);
        school.setAtivo(false);
        escolaRepository.save(school);
    }

    private void aplicarDados(School school, SchoolRequest request) {
        school.setNome(request.nome());
        school.setNivelEnsino(request.nivelEnsino());
    }
}
