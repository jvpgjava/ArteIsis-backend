package com.arteisis.model.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record PortfolioItemRequest(
        @NotBlank @Size(max = 255) String title,
        @NotBlank @Size(max = 1024) String imageUrl,
        boolean active) {}
