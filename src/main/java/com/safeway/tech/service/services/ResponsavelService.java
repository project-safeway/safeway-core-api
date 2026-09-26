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
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ResponsavelService {

    private final GuardianRepository guardianRepository;
    private final UserService userService;
    private final AddressService addressService;
    private final CurrentUserService currentUserService;

    public Guardian findById(UUID id) {
        UUID userId = currentUserService.getCurrentUserId();
        return guardianRepository.findByIdResponsavelAndIdUsuario(id, userId)
                .orElseThrow(() -> new GuardianNotFoundException("O responsável com ID " + id + "não foi encontrado"));
    }

    public List<Guardian> listGuardians() {
        UUID userId = currentUserService.getCurrentUserId();
        return guardianRepository.findAllByIdUsuario(userId);
    }

    @Transactional
    public Guardian createGuardian(GuardianRequest request) {
        Guardian guardian = new Guardian();
        applyData(guardian, request);

        Address address = addressService.create(request.address());
        guardian.setAddress(address);

        UUID userId = currentUserService.getCurrentUserId();
        User user = userService.findById(userId);
        guardian.setUser(user);

        return guardianRepository.save(guardian);
    }

    @Transactional
    public Guardian updateGuardian(GuardianRequest request, UUID guardianId) {
        Guardian guardian = findById(guardianId);
        applyData(guardian, request);

        if (request.address() != null) {
            Address currentAddress = guardian.getAddress();
            Address address = currentAddress != null && currentAddress.getId() != null
                    ? addressService.update(currentAddress.getId(), request.address())
                    : addressService.create(request.address());
            guardian.setAddress(address);
        }

        return guardianRepository.save(guardian);
    }

    public void deactivate(UUID id) {
        Guardian guardian = findById(id);
        guardian.setActive(false);
        guardianRepository.save(guardian);
    }

    private void applyData(Guardian guardian, GuardianRequest request) {
        guardian.setName(request.name());
        guardian.setPrimaryPhoneNumber(request.primaryPhoneNumber());
        guardian.setSecondaryPhoneNumber(request.secondaryPhoneNumber());
        guardian.setEmail(request.email());
    }
}
