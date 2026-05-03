package com.arteisis.model.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public record OrderResponse(
        UUID id,
        UUID customerId,
        String customerName,
        String productSummary,
        LocalDate date,
        String status,
        BigDecimal total) {}
