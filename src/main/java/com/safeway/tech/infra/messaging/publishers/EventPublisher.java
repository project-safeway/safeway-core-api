package com.safeway.tech.infra.messaging.publishers;

import com.safeway.tech.domain.models.Student;
import com.safeway.tech.infra.messaging.config.RabbitMQProperties;
import com.safeway.tech.infra.messaging.event.StudentEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class EventPublisher {

    private final RabbitTemplate rabbitTemplate;
    private final RabbitMQProperties rabbitMQProperties;

    public void publicarAlunoCriado(Student student) {
        log.info("Publicando evento de student criado: {}", student.getId());

        try {
            StudentEvent event = new StudentEvent(
                    UUID.randomUUID(),
                    student.getId(),
                    student.getUsuario().getId(),
                    student.getNome(),
                    student.getValorMensalidade(),
                    student.getDiaVencimento(),
                    student.getAtivo(),
                    "ALUNO_CRIADO",
                    LocalDateTime.now()
            );

            rabbitTemplate.convertAndSend(
                    rabbitMQProperties.getExchanges().getAluno(),
                    rabbitMQProperties.getRoutingKeys().getAlunoCriado(),
                    event
            );

            log.info("Evento de student criado publicado com sucesso: {}", event.alunoId());
        } catch (Exception e) {
            log.error("Erro ao publicar evento de student criado", e);
        }
    }

    public void publicarAlunoAtualizado(Student student) {
        log.info("Publicando evento de student atualizado: {}", student.getId());

        try {
            StudentEvent event = new StudentEvent(
                    UUID.randomUUID(),
                    student.getId(),
                    student.getUsuario().getId(),
                    student.getNome(),
                    student.getValorMensalidade(),
                    student.getDiaVencimento(),
                    student.getAtivo(),
                    "ALUNO_ATUALIZADO",
                    LocalDateTime.now()
            );

            rabbitTemplate.convertAndSend(
                    rabbitMQProperties.getExchanges().getAluno(),
                    rabbitMQProperties.getRoutingKeys().getAlunoAtualizado(),
                    event
            );

            log.info("Evento de student atualizado publicado com sucesso: {}", event.alunoId());
        } catch (Exception e) {
            log.error("Erro ao publicar evento de student atualizado", e);
        }
    }

    public void publicarAlunoInativado(Student student) {
        log.info("Publicando evento de student inativado: {}", student.getId());

        try {
            StudentEvent event = new StudentEvent(
                    UUID.randomUUID(),
                    student.getId(),
                    student.getUsuario().getId(),
                    student.getNome(),
                    student.getValorMensalidade(),
                    student.getDiaVencimento(),
                    student.getAtivo(),
                    "ALUNO_INATIVADO",
                    LocalDateTime.now()
            );

            rabbitTemplate.convertAndSend(
                    rabbitMQProperties.getExchanges().getAluno(),
                    rabbitMQProperties.getRoutingKeys().getAlunoInativado(),
                    event
            );

            log.info("Evento de student inativado publicado com sucesso: {}", event.alunoId());
        } catch (Exception e) {
            log.error("Erro ao publicar evento de student inativado", e);
        }
    }

}
