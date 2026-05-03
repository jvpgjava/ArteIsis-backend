package com.arteisis.model.entity;

public enum ProductLabel {
    NONE,
    NOVO,
    DESTAQUE;

    public String toApi() {
        return switch (this) {
            case NONE -> null;
            case NOVO -> "Novo";
            case DESTAQUE -> "Destaque";
        };
    }

    public static ProductLabel fromApi(String value) {
        if (value == null || value.isBlank()) {
            return NONE;
        }
        return switch (value.trim()) {
            case "Novo", "NOVO" -> NOVO;
            case "Destaque", "DESTAQUE" -> DESTAQUE;
            default -> NONE;
        };
    }
}
