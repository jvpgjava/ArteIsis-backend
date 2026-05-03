package com.arteisis.service;

import com.arteisis.config.ContactMailProperties;
import com.arteisis.model.dto.ContactRequest;
import jakarta.mail.internet.MimeMessage;
import java.util.Locale;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.HttpStatus;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring6.SpringTemplateEngine;

@Service
@RequiredArgsConstructor
@Slf4j
public class ContactMailService {

    private final ObjectProvider<JavaMailSender> mailSenderProvider;
    private final SpringTemplateEngine templateEngine;
    private final ContactMailProperties mailProperties;

    public void sendContactEmail(ContactRequest request) {
        JavaMailSender mailSender = mailSenderProvider.getIfAvailable();
        if (mailSender == null) {
            throw new ResponseStatusException(
                    HttpStatus.SERVICE_UNAVAILABLE,
                    "E-mail não configurado: define spring.mail.host (e credenciais SMTP) no perfil activo.");
        }
        String from = mailProperties.from();
        String to = mailProperties.contactTo();
        if (from == null || from.isBlank() || to == null || to.isBlank()) {
            throw new ResponseStatusException(
                    HttpStatus.SERVICE_UNAVAILABLE,
                    "Define arteisis.mail.from e arteisis.mail.contact-to (destino interno dos contactos).");
        }

        String subjectLabel = subjectLabel(request.subject());
        ClassPathResource logoResource = new ClassPathResource("templates/icons/Logo1-ArteIsis.png");
        boolean hasLogo = logoResource.exists();

        Context ctx = new Context(Locale.forLanguageTag("pt"));
        ctx.setVariable("hasLogo", hasLogo);
        ctx.setVariable("contactName", request.name());
        ctx.setVariable("contactEmail", request.email());
        ctx.setVariable("subjectCode", request.subject());
        ctx.setVariable("subjectLabel", subjectLabel);
        ctx.setVariable("messageBody", request.message());

        String html = templateEngine.process("mail/contact-notification", ctx);

        try {
            MimeMessage mimeMessage = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true, "UTF-8");
            helper.setFrom(from);
            helper.setTo(to);
            helper.setReplyTo(request.email());
            helper.setSubject("[Arte Isis — Site] " + subjectLabel);
            helper.setText(html, true);
            if (hasLogo) {
                helper.addInline("arteisisLogo", logoResource);
            } else {
                log.warn("Logo de e-mail não encontrado: classpath:templates/icons/Logo1-ArteIsis.png");
            }
            mailSender.send(mimeMessage);
        } catch (Exception e) {
            log.error("Falha ao enviar e-mail de contacto", e);
            throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, "Não foi possível enviar o e-mail. Tenta mais tarde.");
        }
    }

    private static String subjectLabel(String code) {
        return switch (code) {
            case "uniformes" -> "Orçamento";
            case "camisetas" -> "Pedido de Camisetas, Moletons ou Uniformes personalizados";
            case "estampas" -> "Estampas";
            case "parcerias" -> "Parcerias";
            case "outros" -> "Outros";
            default -> code;
        };
    }
}
