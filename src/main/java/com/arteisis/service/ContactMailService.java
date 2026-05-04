package com.arteisis.service;

import com.arteisis.config.ContactMailProperties;
import com.arteisis.model.dto.ContactRequest;
import jakarta.mail.internet.MimeMessage;
import java.util.Locale;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.core.env.Environment;
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
    private final Environment environment;

    public void sendContactEmail(ContactRequest request) {
        JavaMailSender mailSender = mailSenderProvider.getIfAvailable();
        if (mailSender == null) {
            throw new ResponseStatusException(
                    HttpStatus.SERVICE_UNAVAILABLE,
                    "E-mail não configurado: defina spring.mail.host (e credenciais SMTP) no perfil ativo.");
        }
        String from = effectiveFromAddress();
        String to = effectiveContactDestination();
        if (from.isBlank() || to.isBlank()) {
            throw new ResponseStatusException(
                    HttpStatus.SERVICE_UNAVAILABLE,
                    "Defina arteisis.mail.from e arteisis.mail.contact-to "
                            + "(ou use spring.mail.username como remetente e destino quando só houver uma caixa interna).");
        }

        String resolvedSubjectLabel = subjectLabel(request.subject());
        ClassPathResource logoResource = new ClassPathResource("templates/icons/Logo1-ArteIsis.png");
        boolean hasLogo = logoResource.exists();

        Context ctx = new Context(Locale.forLanguageTag("pt"));
        ctx.setVariable("hasLogo", hasLogo);
        ctx.setVariable("contactName", request.name());
        ctx.setVariable("contactEmail", request.email());
        ctx.setVariable("subjectLabel", resolvedSubjectLabel);
        ctx.setVariable("messageBody", request.message());

        String html = templateEngine.process("mail/contact-notification", ctx);

        try {
            MimeMessage mimeMessage = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true, "UTF-8");
            helper.setFrom(from);
            helper.setTo(to);
            helper.setReplyTo(request.email());
            helper.setSubject("Arte Isis - " + resolvedSubjectLabel);
            helper.setText(html, true);
            if (hasLogo) {
                helper.addInline("arteisisLogo", logoResource);
            } else {
                log.warn("Logo de e-mail não encontrado: classpath:templates/icons/Logo1-ArteIsis.png");
            }
            mailSender.send(mimeMessage);
        } catch (Exception e) {
            log.error("Falha ao enviar e-mail de contato", e);
            throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, "Não foi possível enviar o e-mail. Tente mais tarde.");
        }
    }

    /** Preferência: {@code arteisis.mail.*}; se vazio, usa {@code spring.mail.username} (comum no Gmail). */
    private String effectiveFromAddress() {
        String f = mailProperties.from();
        if (f != null && !f.isBlank()) {
            return f.trim();
        }
        return mailUsernameFallback();
    }

    private String effectiveContactDestination() {
        String t = mailProperties.contactTo();
        if (t != null && !t.isBlank()) {
            return t.trim();
        }
        return mailUsernameFallback();
    }

    private String mailUsernameFallback() {
        String u = environment.getProperty("spring.mail.username");
        return u != null && !u.isBlank() ? u.trim() : "";
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
