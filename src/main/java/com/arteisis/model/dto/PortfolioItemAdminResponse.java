package com.arteisis.model.dto;

import java.util.UUID;

public record PortfolioItemAdminResponse(UUID id, String title, String imageUrl, int sortOrder, boolean active) {}
