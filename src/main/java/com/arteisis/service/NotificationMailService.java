package com.arteisis.service;

import com.arteisis.config.ContactMailProperties;
import com.arteisis.model.dto.OrderResponse;
import jakarta.mail.internet.MimeMessage;
import java.math.BigDecimal;
import java.text.NumberFormat;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.core.env.Environment;
import org.springframework.core.io.ClassPathResource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring6.SpringTemplateEngine;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationMailService {

    private final ObjectProvider<JavaMailSender> mailSenderProvider;
    private final SpringTemplateEngine templateEngine;
    private final ContactMailProperties mailProperties;
    private final Environment environment;

    @Async
    public void sendOrderConfirmation(String customerEmail, String customerName, OrderResponse order) {
        JavaMailSender sender = mailSenderProvider.getIfAvailable();
        if (sender == null) {
            log.warn("E-mail não configurado — confirmação de pedido não enviada para {}", customerEmail);
            return;
        }
        String from = effectiveFrom();
        if (from.isBlank()) {
            log.warn("arteisis.mail.from não configurado — confirmação de pedido não enviada");
            return;
        }

        NumberFormat brl = NumberFormat.getCurrencyInstance(new Locale("pt", "BR"));
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("dd/MM/yyyy");

        List<Map<String, Object>> lines = order.colors() != null
                ? buildLineData(order, brl)
                : List.of();

        ClassPathResource logo = new ClassPathResource("templates/icons/Logo1-ArteIsis.png");
        boolean hasLogo = logo.exists();

        Context ctx = new Context(Locale.forLanguageTag("pt"));
        ctx.setVariable("hasLogo", hasLogo);
        ctx.setVariable("customerName", customerName);
        ctx.setVariable("orderId", order.id().toString().substring(0, 8).toUpperCase());
        ctx.setVariable("orderDate", order.date() != null ? order.date().format(dtf) : "—");
        ctx.setVariable("lines", lines);
        ctx.setVariable("totalFormatted", brl.format(order.total() != null ? order.total() : BigDecimal.ZERO));

        sendHtml(sender, from, customerEmail, "Pedido recebido — Arte Isis", "mail/order-confirmation", ctx, hasLogo ? logo : null);
    }

    @Async
    public void sendOrderStatusUpdate(String customerEmail, String customerName, OrderResponse order) {
        JavaMailSender sender = mailSenderProvider.getIfAvailable();
        if (sender == null) {
            log.warn("E-mail não configurado — atualização de pedido não enviada para {}", customerEmail);
            return;
        }
        String from = effectiveFrom();
        if (from.isBlank()) {
            log.warn("arteisis.mail.from não configurado — atualização de pedido não enviada");
            return;
        }

        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        String statusLabel = statusLabel(order.status());
        String statusColor = statusColor(order.status());
        String statusDesc  = statusDescription(order.status());

        ClassPathResource logo = new ClassPathResource("templates/icons/Logo1-ArteIsis.png");
        boolean hasLogo = logo.exists();

        Context ctx = new Context(Locale.forLanguageTag("pt"));
        ctx.setVariable("hasLogo", hasLogo);
        ctx.setVariable("customerName", customerName);
        ctx.setVariable("orderId", order.id().toString().substring(0, 8).toUpperCase());
        ctx.setVariable("orderDate", order.date() != null ? order.date().format(dtf) : "—");
        ctx.setVariable("productSummary", order.productSummary());
        ctx.setVariable("statusLabel", statusLabel);
        ctx.setVariable("statusColor", statusColor);
        ctx.setVariable("statusDescription", statusDesc);

        sendHtml(sender, from, customerEmail,
                "Atualização do seu pedido — " + statusLabel + " | Arte Isis",
                "mail/order-status-update", ctx, hasLogo ? logo : null);
    }

    @Async
    public void sendWelcome(String userEmail, String displayName) {
        JavaMailSender sender = mailSenderProvider.getIfAvailable();
        if (sender == null) {
            log.warn("E-mail não configurado — boas-vindas não enviado para {}", userEmail);
            return;
        }
        String from = effectiveFrom();
        if (from.isBlank()) {
            log.warn("arteisis.mail.from não configurado — boas-vindas não enviado");
            return;
        }

        ClassPathResource logo = new ClassPathResource("templates/icons/Logo1-ArteIsis.png");
        boolean hasLogo = logo.exists();

        Context ctx = new Context(Locale.forLanguageTag("pt"));
        ctx.setVariable("hasLogo", hasLogo);
        ctx.setVariable("displayName", displayName != null && !displayName.isBlank() ? displayName : userEmail);
        ctx.setVariable("email", userEmail);

        sendHtml(sender, from, userEmail, "Bem-vindo(a) à Arte Isis!", "mail/welcome", ctx, hasLogo ? logo : null);
    }

    private List<Map<String, Object>> buildLineData(OrderResponse order, NumberFormat brl) {
        if (order.productSummary() == null || order.productSummary().isBlank()) {
            return List.of();
        }
        String[] descriptions = order.productSummary().split(",\\s*");
        List<String> colors = order.colors() != null ? order.colors() : List.of();
        return java.util.stream.IntStream.range(0, descriptions.length)
                .mapToObj(i -> {
                    String color = i < colors.size() ? colors.get(i) : null;
                    BigDecimal lineTotal = order.total() != null && descriptions.length > 0
                            ? order.total().divide(BigDecimal.valueOf(descriptions.length), 2, java.math.RoundingMode.HALF_UP)
                            : BigDecimal.ZERO;
                    return Map.<String, Object>of(
                            "description", descriptions[i].trim(),
                            "quantity", 1,
                            "selectedColor", color != null ? color : "",
                            "lineTotalFormatted", brl.format(lineTotal));
                })
                .toList();
    }

    private void sendHtml(JavaMailSender sender, String from, String to, String subject,
                          String template, Context ctx, ClassPathResource logo) {
        try {
            String html = templateEngine.process(template, ctx);
            MimeMessage msg = sender.createMimeMessage();
            MimeMessageHelper h = new MimeMessageHelper(msg, true, "UTF-8");
            h.setFrom(from);
            h.setTo(to);
            h.setSubject(subject);
            h.setText(html, true);
            if (logo != null) {
                h.addInline("arteisisLogo", logo);
            }
            sender.send(msg);
        } catch (Exception e) {
            log.error("Falha ao enviar e-mail '{}' para {}", subject, to, e);
        }
    }

    private static String statusLabel(String status) {
        if (status == null) return "Atualizado";
        return switch (status.toUpperCase()) {
            case "PENDENTE"  -> "Pendente";
            case "PRODUCAO"  -> "Em Produção";
            case "CONCLUIDO" -> "Concluído";
            default          -> status;
        };
    }

    private static String statusColor(String status) {
        if (status == null) return "#cbd5e1";
        return switch (status.toUpperCase()) {
            case "PENDENTE"  -> "#f59e0b";
            case "PRODUCAO"  -> "#3b82f6";
            case "CONCLUIDO" -> "#22c55e";
            default          -> "#cbd5e1";
        };
    }

    private static String statusDescription(String status) {
        if (status == null) return "";
        return switch (status.toUpperCase()) {
            case "PENDENTE"  -> "Seu pedido foi recebido e aguarda início da produção.";
            case "PRODUCAO"  -> "Sua encomenda está sendo produzida pela nossa equipe!";
            case "CONCLUIDO" -> "Seu pedido está pronto. Entraremos em contato para combinar a entrega.";
            default          -> "";
        };
    }

    private String effectiveFrom() {
        String f = mailProperties.from();
        if (f != null && !f.isBlank()) return f.trim();
        String u = environment.getProperty("spring.mail.username");
        return u != null && !u.isBlank() ? u.trim() : "";
    }
}
