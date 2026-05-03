package com.arteisis.model.dto;

import java.util.UUID;

public record PortfolioItemResponse(UUID id, String title, String imageUrl, int sortOrder) {}
