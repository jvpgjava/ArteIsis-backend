package com.arteisis.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "arteisis.mail")
public record ContactMailProperties(
        /** Remetente (From), ex.: noreply@dominio.pt */
        String from,
        /** Destino interno onde chegam os pedidos de contacto */
        String contactTo) {}
