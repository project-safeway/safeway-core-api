package com.safeway.tech.service.mappers;

import com.safeway.tech.api.dto.school.SchoolResponse;
import com.safeway.tech.api.dto.school.SchoolResumeResponse;
import com.safeway.tech.domain.models.School;

public class SchoolMapper {

    public static SchoolResponse toResponse(School school) {

        return new SchoolResponse(
                school.getId(),
                school.getName(),
                school.getEducationLevel(),
                AddressMapper.toResponse(school.getAddress())
        );
    }

    public static SchoolResumeResponse toResumeResponse(School school) {
        return new SchoolResumeResponse(
                school.getName(),
                school.getEducationLevel(),
                AddressMapper.toResponse(school.getAddress())
        );
    }

}
