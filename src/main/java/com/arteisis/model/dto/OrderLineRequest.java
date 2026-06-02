package com.arteisis.model.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.util.UUID;

public record OrderLineRequest(
        UUID productId,
        @NotBlank @Size(max = 512) String description,
        @NotNull @Min(1) Integer quantity,
        @NotNull @DecimalMin("0.0") BigDecimal unitPrice,
        @Size(max = 32) String selectedColor) {}
