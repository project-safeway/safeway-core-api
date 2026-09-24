package com.safeway.tech.service.mappers;

import com.safeway.tech.api.dto.endereco.EnderecoResponse;
import com.safeway.tech.domain.models.Address;

public class EnderecoMapper {

    public static EnderecoResponse toResponse(Address escola) {
        return new EnderecoResponse(
                escola.getId(),
                escola.getLogradouro(),
                escola.getNumero(),
                escola.getComplemento(),
                escola.getBairro(),
                escola.getCidade(),
                escola.getUf(),
                escola.getCep(),
                escola.getLatitude(),
                escola.getLongitude(),
                escola.getTipo(),
                escola.getAtivo(),
                escola.getPrincipal()
        );
    }

}
