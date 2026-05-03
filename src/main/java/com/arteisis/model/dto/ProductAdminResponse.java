package com.arteisis.model.dto;

import java.math.BigDecimal;
import java.util.Set;
import java.util.UUID;

public record ProductAdminResponse(
        UUID id,
        String name,
        BigDecimal unitPrice,
        String category,
        int stock,
        String imageUrl,
        String label,
        String availability,
        boolean active,
        Set<String> sizes) {}
