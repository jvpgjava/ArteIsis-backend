package com.arteisis.model.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import java.util.List;

public record PublicOrderRequest(
        @NotEmpty @Valid List<OrderLineRequest> lines) {}
