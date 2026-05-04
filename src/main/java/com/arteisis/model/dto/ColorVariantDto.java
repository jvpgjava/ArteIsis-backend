package com.arteisis.model.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ColorVariantDto(
        @NotBlank @Size(max = 32) String hex,
        @Size(max = 1024) String imageUrl,
        boolean available) {}
