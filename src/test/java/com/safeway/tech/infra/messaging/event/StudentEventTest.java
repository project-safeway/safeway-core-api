package com.safeway.tech.infra.messaging.event;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;

class StudentEventTest {

    private final ObjectMapper objectMapper = new ObjectMapper().findAndRegisterModules();

    @Test
    void serializesPropertiesInPortuguese() throws Exception {
        StudentEvent event = new StudentEvent(
                UUID.randomUUID(),
                UUID.randomUUID(),
                UUID.randomUUID(),
                "Ana",
                250.0,
                10,
                true,
                "ALUNO_CRIADO",
                LocalDateTime.of(2026, 10, 22, 21, 15)
        );

        JsonNode json = objectMapper.readTree(objectMapper.writeValueAsString(event));
        Set<String> properties = new HashSet<>();
        json.fieldNames().forEachRemaining(properties::add);

        assertEquals(Set.of(
                "id",
                "alunoId",
                "usuarioId",
                "nome",
                "valorMensalidade",
                "diaVencimento",
                "ativo",
                "tipo",
                "timestamp"
        ), properties);
        assertEquals("2026-10-22T21:15:00", json.get("timestamp").asText());
    }
}
