package com.investhub.api.dto.response;

public record AuthTokenResponse(String accessToken, String tokenType, long expiresIn) {}
