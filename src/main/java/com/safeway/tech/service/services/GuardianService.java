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
public class GuardianService {

    private final GuardianRepository guardianRepository;
    private final UserService userService;
    private final AddressService addressService;
    private final CurrentUserService currentUserService;

    public Guardian findById(UUID id) {
        UUID userId = currentUserService.getCurrentUserId();
        UUID transportId = currentUserService.getCurrentTransporteId();
        return guardianRepository.findByGuardianIdAndOwnerOrTransport(id, userId, transportId)
                .orElseThrow(() -> new GuardianNotFoundException("O responsável com ID " + id + "não foi encontrado"));
    }

    public List<Guardian> listGuardians() {
        UUID userId = currentUserService.getCurrentUserId();
        UUID transportId = currentUserService.getCurrentTransporteId();
        return guardianRepository.findByOwnerOrTransport(userId, transportId);
    }

    @Transactional
    public Guardian createGuardian(GuardianRequest request) {
        return createGuardian(request, true);
    }

    @Transactional
    public Guardian createPreRegistrationGuardian(GuardianRequest request) {
        return createGuardian(request, false);
    }

    private Guardian createGuardian(GuardianRequest request, boolean attachToCurrentUser) {
        Guardian guardian = new Guardian();
        applyData(guardian, request);

        Address address = addressService.create(request.address());
        guardian.setAddress(address);

        if (attachToCurrentUser) {
            UUID userId = currentUserService.getCurrentUserId();
            User user = userService.findById(userId);
            guardian.setUser(user);
        }

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
