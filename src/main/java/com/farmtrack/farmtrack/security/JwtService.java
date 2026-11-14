package com.farmtrack.farmtrack.security;

import com.farmtrack.farmtrack.entity.Role;
import com.farmtrack.farmtrack.entity.User;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtBuilder;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.util.Date;

/**
 * Creates and validates JWTs.
 * Token payload: sub = user id, role = FARMER/ADMIN, farmerId = linked farmer (farmers only).
 * The token is signed with HMAC-SHA so the client cannot change it without the signature failing.
 */
@Service
public class JwtService {

    private final SecretKey signingKey;
    private final long expirationMs;

    public JwtService(@Value("${app.jwt.secret}") String base64Secret,
                      @Value("${app.jwt.expiration-ms}") long expirationMs) {
        this.signingKey = Keys.hmacShaKeyFor(Decoders.BASE64.decode(base64Secret));
        this.expirationMs = expirationMs;
    }

    public String generateToken(User user) {
        Date now = new Date();
        JwtBuilder builder = Jwts.builder()
                .subject(String.valueOf(user.getId()))
                .claim("role", user.getRole().name())
                .issuedAt(now)
                .expiration(new Date(now.getTime() + expirationMs));
        if (user.getFarmer() != null) {
            builder.claim("farmerId", user.getFarmer().getId());
        }
        return builder.signWith(signingKey).compact();
    }

    /**
     * Verifies the signature and expiry and returns the user stored in the token.
     *
     * @throws JwtException             if the token is invalid or expired
     * @throws IllegalArgumentException if the token content is malformed
     */
    public AuthUser parseToken(String token) {
        Claims claims = Jwts.parser()
                .verifyWith(signingKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();

        Long userId = Long.valueOf(claims.getSubject());
        Number farmerId = claims.get("farmerId", Number.class);
        Role role = Role.valueOf(claims.get("role", String.class));
        return new AuthUser(userId, farmerId != null ? farmerId.longValue() : null, role);
    }

    public long getExpirationMs() {
        return expirationMs;
    }
}
