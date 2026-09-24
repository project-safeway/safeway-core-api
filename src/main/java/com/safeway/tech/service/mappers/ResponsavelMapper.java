package com.safeway.tech.service.mappers;

import com.safeway.tech.api.dto.responsavel.ResponsavelResponse;
import com.safeway.tech.domain.models.Guardian;

public class ResponsavelMapper {

    public static ResponsavelResponse toResponse(Guardian guardian) {
        return new ResponsavelResponse(
                guardian.getId(),
                guardian.getNome(),
                guardian.getCpf(),
                guardian.getTel1(),
                guardian.getTel2(),
                guardian.getEmail(),
                EnderecoMapper.toResponse(guardian.getAddress())
        );
    }

}
