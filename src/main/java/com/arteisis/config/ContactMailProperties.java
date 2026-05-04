package com.arteisis.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "arteisis.mail")
public record ContactMailProperties(
        /** Remetente (From), ex.: noreply@seudominio.com.br */
        String from,
        /** Destino interno onde chegam os pedidos de contato do site */
        String contactTo) {}
