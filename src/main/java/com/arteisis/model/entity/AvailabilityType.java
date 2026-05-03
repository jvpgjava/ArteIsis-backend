package com.arteisis.model.entity;

public enum AvailabilityType {
    DISPONIVEL,
    SOB_ENCOMENDA;

    public String toApi() {
        return switch (this) {
            case DISPONIVEL -> "Disponível";
            case SOB_ENCOMENDA -> "Sob encomenda";
        };
    }

    public static AvailabilityType fromApi(String value) {
        if (value == null || value.isBlank()) {
            return DISPONIVEL;
        }
        String v = value.trim();
        return switch (v) {
            case "Disponível", "DISPONIVEL", "IN_STOCK" -> DISPONIVEL;
            case "Sob encomenda", "SOB_ENCOMENDA", "MADE_TO_ORDER" -> SOB_ENCOMENDA;
            default -> DISPONIVEL;
        };
    }
}
