package com.safeway.tech.service.services;

import com.safeway.tech.api.dto.school.SchoolRequest;
import com.safeway.tech.domain.models.Address;
import com.safeway.tech.domain.models.School;
import com.safeway.tech.infra.exception.SchoolNotFoundException;
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

    public List<School> findAllSchools() {
        UUID transportId = currentUserService.getCurrentTransporteId();
        return schoolRepository.findAllByTransportId(transportId);
    }

    @Transactional(readOnly = true)
    public School findById(UUID schoolId) {
        UUID transportId = currentUserService.getCurrentTransporteId();
        return schoolRepository.findByIdAndTransportId(schoolId, transportId)
                .orElseThrow(() -> new SchoolNotFoundException("School não encontrada"));
    }

    @Transactional
    public School createSchool(SchoolRequest request) {
        School school = new School();

        applyData(school, request);

        Address address = addressService.create(request.address());
        school.setAddress(address);

        return schoolRepository.save(school);
    }

    @Transactional
    public School updateSchool(UUID schoolId, SchoolRequest request) {
        School school = findById(schoolId);

        applyData(school, request);

        Address address = addressService.update(school.getAddress().getId(), request.address());
        school.setAddress(address);

        return schoolRepository.save(school);
    }

    @Transactional
    public void deactivate(UUID schoolId) {
        School school = findById(schoolId);
        school.setActive(false);
        schoolRepository.save(school);
    }

    private void applyData(School school, SchoolRequest request) {
        school.setName(request.name());
        school.setEducationLevel(request.educationLevel());
    }
}
