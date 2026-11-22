package com.safeway.tech.auth.infrastructure.mail;

import com.safeway.tech.auth.core.exception.PasswordResetDeliveryException;
import jakarta.mail.Session;
import jakarta.mail.internet.MimeMessage;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.mail.MailSendException;
import org.springframework.mail.javamail.JavaMailSender;

import java.util.Properties;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SmtpPasswordResetNotifierAdapterTest {

    @Mock
    private ObjectProvider<JavaMailSender> mailSenderProvider;

    @Mock
    private JavaMailSender mailSender;

    @Test
    void notifyCodeShouldSendEmailWhenMailSenderIsAvailable() throws Exception {
        MimeMessage mimeMessage = new MimeMessage(Session.getInstance(new Properties()));
        when(mailSenderProvider.getIfAvailable()).thenReturn(mailSender);
        when(mailSender.createMimeMessage()).thenReturn(mimeMessage);

        SmtpPasswordResetNotifierAdapter adapter =
                new SmtpPasswordResetNotifierAdapter(mailSenderProvider, "safeway@example.com");

        adapter.notifyCode("motorista@example.com", "482910", 15);

        verify(mailSender).send(mimeMessage);
        assertEquals("SafeWay — Código de recuperação de senha", mimeMessage.getSubject());
        assertEquals("motorista@example.com", mimeMessage.getAllRecipients()[0].toString());
    }

    @Test
    void notifyCodeShouldThrowWhenMailSenderIsNotConfigured() {
        when(mailSenderProvider.getIfAvailable()).thenReturn(null);
        SmtpPasswordResetNotifierAdapter adapter =
                new SmtpPasswordResetNotifierAdapter(mailSenderProvider, "");

        assertThrows(
                PasswordResetDeliveryException.class,
                () -> adapter.notifyCode("motorista@example.com", "482910", 15)
        );
    }

    @Test
    void notifyCodeShouldWrapMailExceptionsInDeliveryException() {
        MimeMessage mimeMessage = new MimeMessage(Session.getInstance(new Properties()));
        when(mailSenderProvider.getIfAvailable()).thenReturn(mailSender);
        when(mailSender.createMimeMessage()).thenReturn(mimeMessage);
        doThrow(new MailSendException("SMTP down")).when(mailSender).send(any(MimeMessage.class));

        SmtpPasswordResetNotifierAdapter adapter =
                new SmtpPasswordResetNotifierAdapter(mailSenderProvider, "safeway@example.com");

        assertThrows(
                PasswordResetDeliveryException.class,
                () -> adapter.notifyCode("motorista@example.com", "482910", 15)
        );
    }
}
