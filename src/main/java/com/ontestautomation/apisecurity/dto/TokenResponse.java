package com.ontestautomation.apisecurity.dto;

public record TokenResponse(String accessToken, String tokenType, long expiresIn) {}
