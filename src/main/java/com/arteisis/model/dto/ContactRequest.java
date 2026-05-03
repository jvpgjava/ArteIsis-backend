package com.arteisis.model.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record ContactRequest(
        @NotBlank @Size(max = 255) String name,
        @NotBlank @Email @Size(max = 255) String email,
        @NotBlank
                @Pattern(
                        regexp = "uniformes|camisetas|parcerias|outros",
                        message = "assunto inválido")
                String subject,
        @NotBlank @Size(max = 4000) String message) {}
