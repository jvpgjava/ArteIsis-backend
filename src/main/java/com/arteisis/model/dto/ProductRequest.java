package com.arteisis.model.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.util.Set;

public record ProductRequest(
        @NotBlank @Size(max = 255) String name,
        @NotNull @DecimalMin("0.0") BigDecimal unitPrice,
        @NotBlank @Size(max = 100) String category,
        @NotNull @Min(0) Integer stock,
        @Size(max = 1024) String imageUrl,
        String label,
        String availability,
        @NotNull Boolean active,
        Set<@Size(max = 8) String> sizes) {}
