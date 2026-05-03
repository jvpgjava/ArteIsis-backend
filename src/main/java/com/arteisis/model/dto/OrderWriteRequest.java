package com.arteisis.model.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public record OrderWriteRequest(
        @NotNull UUID customerId,
        String status,
        @NotNull LocalDate orderDate,
        @NotEmpty @Valid List<OrderLineRequest> lines) {}
