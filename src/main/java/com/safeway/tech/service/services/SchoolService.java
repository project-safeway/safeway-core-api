package com.safeway.tech.service.services;

import com.safeway.tech.api.dto.school.SchoolRequest;
import com.safeway.tech.domain.models.Address;
import com.safeway.tech.domain.models.School;
import com.safeway.tech.infra.exception.EntityNotFoundException;
import com.safeway.tech.repository.SchoolRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class SchoolService {

    private final SchoolRepository schoolRepository;
    private final AddressService addressService;
    private final CurrentUserService currentUserService;
    private final TransportService transportService;

    public List<School> findAll() {
        UUID transportId = currentUserService.getCurrentTransportId();
        return schoolRepository.findAllByTransportId(transportId);
    }

    @Transactional(readOnly = true)
    public School findById(UUID schoolId) {
        UUID transportId = currentUserService.getCurrentTransportId();
        return schoolRepository.findByIdAndTransportId(schoolId, transportId)
                .orElseThrow(() -> new EntityNotFoundException("School was not found"));
    }

    @Transactional
    public School create(SchoolRequest request) {
        School school = new School();

        applyData(school, request);

        Address address = addressService.create(request.address());
        school.setAddress(address);
        school.setTransport(transportService.getTransport());

        return schoolRepository.save(school);
    }

    @Transactional
    public School update(UUID schoolId, SchoolRequest request) {
        School school = findById(schoolId);

        applyData(school, request);

        Address address = addressService.update(school.getAddress().getId(), request.address());
        school.setAddress(address);

        return schoolRepository.save(school);
    }

    @Transactional
    public void delete(UUID schoolId) {
        School school = findById(schoolId);
        school.setActive(false);
        schoolRepository.save(school);
    }

    private void applyData(School school, SchoolRequest request) {
        school.setName(request.name());
        school.setEducationLevel(request.educationLevel());
    }
}
