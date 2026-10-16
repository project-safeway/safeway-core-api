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

    public void publicarAlunoCriado(Student student, UUID userId) {
        publish(student, userId, "ALUNO_CRIADO", rabbitMQProperties.getRoutingKeys().getAlunoCriado());
    }

    public void publicarAlunoAtualizado(Student student, UUID userId) {
        publish(student, userId, "ALUNO_ATUALIZADO", rabbitMQProperties.getRoutingKeys().getAlunoAtualizado());
    }

    public void publicarAlunoInativado(Student student, UUID userId) {
        publish(student, userId, "ALUNO_INATIVADO", rabbitMQProperties.getRoutingKeys().getAlunoInativado());
    }

    private void publish(Student student, UUID userId, String eventType, String routingKey) {
        String exchange = rabbitMQProperties.getExchanges().getAluno();
        if (exchange == null || exchange.isBlank() || routingKey == null || routingKey.isBlank()) {
            throw new IllegalStateException("Student event exchange and routing key must be configured");
        }

        StudentEvent event = new StudentEvent(
                UUID.randomUUID(),
                student.getId(),
                userId,
                student.getName(),
                student.getMonthlyFee(),
                student.getDueDate(),
                student.isActive(),
                eventType,
                LocalDateTime.now()
        );
        log.info("Publishing student event {} for student {}", eventType, student.getId());
        rabbitTemplate.convertAndSend(exchange, routingKey, event);
    }

}
