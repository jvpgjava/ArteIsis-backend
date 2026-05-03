package com.arteisis.model.entity;

public enum OrderStatus {
    PENDENTE,
    PRODUCAO,
    CONCLUIDO;

    public String toApi() {
        return switch (this) {
            case PENDENTE -> "Pendente";
            case PRODUCAO -> "Produção";
            case CONCLUIDO -> "Concluído";
        };
    }

    public static OrderStatus fromApi(String value) {
        if (value == null || value.isBlank()) {
            return PENDENTE;
        }
        String v = value.trim();
        return switch (v) {
            case "Pendente", "PENDENTE" -> PENDENTE;
            case "Produção", "PRODUCAO" -> PRODUCAO;
            case "Concluído", "CONCLUIDO" -> CONCLUIDO;
            default -> throw new IllegalArgumentException("status");
        };
    }
}
