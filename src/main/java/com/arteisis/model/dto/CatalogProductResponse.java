package com.arteisis.model.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.math.BigDecimal;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record CatalogProductResponse(
        UUID id,
        String name,
        String category,
        BigDecimal price,
        String image,
        String label,
        String availability,
        Set<String> sizes,
        Set<String> availableSizes,
        List<ColorVariantDto> colors) {}
