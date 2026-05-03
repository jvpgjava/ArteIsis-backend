package com.arteisis.model.dto;

public record TokenResponse(String accessToken, String tokenType, long expiresInSeconds) {}
