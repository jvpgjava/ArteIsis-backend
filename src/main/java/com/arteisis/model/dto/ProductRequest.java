package com.arteisis.model.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.util.List;
import java.util.Set;

public record ProductRequest(
        @NotBlank @Size(max = 255) String name,
        @NotNull
                @DecimalMin(
                        value = "0.0",
                        inclusive = false,
                        message = "O valor unitário precisa ser maior que zero.")
                BigDecimal unitPrice,
        @NotBlank @Size(max = 100) String category,
        @NotNull
                @Min(
                        value = 1,
                        message = "O estoque precisa ser de pelo menos 1 unidade (estoque zero não é permitido).")
                Integer stock,
        @Size(max = 1024) String imageUrl,
        String label,
        String availability,
        @NotNull Boolean active,
        Set<@Size(max = 8) String> sizes,
        Set<@Size(max = 8) String> availableSizes,
        List<ColorVariantDto> colorVariants) {

    public ProductRequest {
        if (sizes == null) {
            sizes = Set.of();
        }
        if (availableSizes == null) {
            availableSizes = Set.of();
        }
        if (colorVariants == null) {
            colorVariants = List.of();
        }
    }
}
