package com.farmtrack.farmtrack.security;

import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

/**
 * Runs once per request. If the request has "Authorization: Bearer TOKEN" and the token is valid,
 * it marks the request as logged in. Otherwise the request continues as anonymous and
 * Spring Security answers 401 for protected endpoints.
 *
 * Not annotated with @Component on purpose: it is added only to the security filter chain
 * (see SecurityConfig), so Spring Boot does not register it a second time as a servlet filter.
 */
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final String BEARER_PREFIX = "Bearer ";

    private final JwtService jwtService;

    public JwtAuthenticationFilter(JwtService jwtService) {
        this.jwtService = jwtService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        String header = request.getHeader(HttpHeaders.AUTHORIZATION);

        if (header != null && header.startsWith(BEARER_PREFIX)) {
            String token = header.substring(BEARER_PREFIX.length()).trim();
            try {
                AuthUser authUser = jwtService.parseToken(token);
                var authorities = List.of(new SimpleGrantedAuthority("ROLE_" + authUser.role().name()));
                var authentication = new UsernamePasswordAuthenticationToken(authUser, null, authorities);

                SecurityContext context = SecurityContextHolder.createEmptyContext();
                context.setAuthentication(authentication);
                SecurityContextHolder.setContext(context);
            } catch (JwtException | IllegalArgumentException ex) {
                // Invalid or expired token: stay anonymous
                SecurityContextHolder.clearContext();
            }
        }

        filterChain.doFilter(request, response);
    }
}
