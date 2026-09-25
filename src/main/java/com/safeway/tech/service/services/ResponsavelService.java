package com.safeway.tech.service.services;

import com.safeway.tech.api.dto.guardian.GuardianRequest;
import com.safeway.tech.domain.models.Address;
import com.safeway.tech.domain.models.Guardian;
import com.safeway.tech.domain.models.User;
import com.safeway.tech.infra.exception.GuardianNotFoundException;
import com.safeway.tech.repository.GuardianRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ResponsavelService {

    private final GuardianRepository guardianRepository;
    private final UserService userService;
    private final AddressService addressService;
    private final CurrentUserService currentUserService;

    public Guardian buscarPorId(UUID id) {
        UUID userId = currentUserService.getCurrentUserId();
        return guardianRepository.findByIdResponsavelAndIdUsuario(id, userId)
                .orElseThrow(() -> new GuardianNotFoundException("O responsável com ID " + id + "não foi encontrado"));
    }

    public Optional<Guardian> buscarPorCpfAndUsuario(String cpf, UUID userId) {
        return guardianRepository.findByCpfAndIdUsuario(cpf, userId);
    }

    public List<Guardian> listarResponsaveis() {
        UUID userId = currentUserService.getCurrentUserId();
        return guardianRepository.findAllByIdUsuario(userId);
    }

    @Transactional
    public Guardian criarResponsavel(GuardianRequest request) {
        Guardian guardian = new Guardian();
        aplicaDados(guardian, request);

        Address address = addressService.create(request.endereco());
        guardian.setAddress(address);

        UUID userId = currentUserService.getCurrentUserId();
        User user = userService.buscarPorId(userId);
        guardian.setUser(user);

        return guardianRepository.save(guardian);
    }

    @Transactional
    public Guardian alterarResponsavel(GuardianRequest request, UUID idResponsavel) {
        Guardian guardian = buscarPorId(idResponsavel);
        aplicaDados(guardian, request);

        if (request.endereco() != null) {
            Address addressAtual = guardian.getAddress();
            Address address = addressAtual != null && addressAtual.getId() != null
                    ? addressService.update(addressAtual.getId(), request.endereco())
                    : addressService.create(request.endereco());
            guardian.setAddress(address);
        }

        return guardianRepository.save(guardian);
    }

    public void desativar(UUID id) {
        Guardian guardian = buscarPorId(id);
        guardian.setAtivo(false);
        guardianRepository.save(guardian);
    }

    private void aplicaDados(Guardian guardian, GuardianRequest request) {
        guardian.setNome(request.nome());
        guardian.setCpf(request.cpf());
        guardian.setTel1(request.tel1());
        guardian.setTel2(request.tel2());
        guardian.setEmail(request.email());
    }
}
