package com.arteisis.model.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import java.util.List;

public record PublicOrderRequest(
        @NotBlank @Size(max = 255) String customerName,
        @NotBlank @Email @Size(max = 255) String customerEmail,
        @NotBlank @Size(max = 64) String customerPhone,
        @NotEmpty @Valid List<OrderLineRequest> lines) {}
