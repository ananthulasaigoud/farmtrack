package com.farmtrack.farmtrack.dto;

/** Returned after register/login. The frontend stores the token and sends it as "Authorization: Bearer TOKEN". */
public record AuthResponse(String token, String tokenType, long expiresInMs, UserInfo user) {
}
