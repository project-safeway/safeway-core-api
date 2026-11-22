package com.safeway.tech.auth.infrastructure.mail;

import com.safeway.tech.auth.core.exception.PasswordResetDeliveryException;
import com.safeway.tech.auth.core.port.PasswordResetNotifierPort;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;

@Slf4j
@Component
public class SmtpPasswordResetNotifierAdapter implements PasswordResetNotifierPort {

    private static final String SUBJECT = "SafeWay — Código de recuperação de senha";

    private final ObjectProvider<JavaMailSender> mailSenderProvider;
    private final String fromAddress;

    public SmtpPasswordResetNotifierAdapter(
            ObjectProvider<JavaMailSender> mailSenderProvider,
            @Value("${safeway.mail.from:${spring.mail.username:}}") String fromAddress
    ) {
        this.mailSenderProvider = mailSenderProvider;
        this.fromAddress = fromAddress;
    }

    @Override
    public void notifyCode(String email, String code, long expiresInMinutes) {
        JavaMailSender mailSender = mailSenderProvider.getIfAvailable();
        if (mailSender == null) {
            throw new PasswordResetDeliveryException(
                    "Serviço de e-mail não configurado no servidor. Configure spring.mail.host no application.properties."
            );
        }

        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, StandardCharsets.UTF_8.name());
            if (fromAddress != null && !fromAddress.isBlank()) {
                helper.setFrom(fromAddress.trim());
            }
            helper.setTo(email);
            helper.setSubject(SUBJECT);
            helper.setText(buildPlainText(code, expiresInMinutes), buildHtml(code, expiresInMinutes));
            mailSender.send(message);
        } catch (MailException | MessagingException e) {
            log.error("Failed to send password reset email to {}: {}", email, e.getMessage());
            throw new PasswordResetDeliveryException(
                    "Não foi possível enviar o e-mail de recuperação. Tente novamente em instantes.", e
            );
        }
    }

    private String buildPlainText(String code, long expiresInMinutes) {
        return """
                Olá!

                Recebemos uma solicitação para redefinir a senha da sua conta SafeWay.

                Seu código de recuperação é: %s

                Este código expira em %d minutos. Se você não fez esta solicitação, ignore este e-mail.
                """.formatted(code, expiresInMinutes);
    }

    private String buildHtml(String code, long expiresInMinutes) {
        return """
                <div style="font-family: Arial, sans-serif; color: #332211; max-width: 480px; margin: 0 auto; padding: 24px;">
                  <h2 style="color: #F6923E; margin-bottom: 8px;">SafeWay</h2>
                  <p>Recebemos uma solicitação para redefinir a senha da sua conta.</p>
                  <p style="margin: 20px 0;">Seu código de recuperação é:</p>
                  <div style="font-size: 32px; font-weight: bold; letter-spacing: 6px; color: #E05D26;
                              background: #FFF0E4; border: 1px solid #FDE4D0; border-radius: 12px;
                              padding: 16px; text-align: center;">
                    %s
                  </div>
                  <p style="margin-top: 20px; font-size: 13px; color: #70665C;">
                    Este código expira em <strong>%d minutos</strong>. Se você não solicitou a alteração, ignore este e-mail.
                  </p>
                </div>
                """.formatted(code, expiresInMinutes);
    }
}
