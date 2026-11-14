package com.farmtrack.farmtrack.config;

import com.farmtrack.farmtrack.security.AppUserDetailsService;
import com.farmtrack.farmtrack.security.JwtAuthenticationFilter;
import com.farmtrack.farmtrack.security.JwtService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;

/**
 * Security rules:
 * - The web pages (HTML, CSS, JS) are public.
 * - /api/auth/register, login, forgot-password, reset-password are public.
 * - Listing all farmers and creating farmer records is ADMIN only.
 * - Everything else needs a valid JWT. Ownership (my crops only) is checked in the services.
 * - Stateless: no HTTP session, every request carries its own token.
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http, JwtService jwtService) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable) // not needed: we use tokens, not cookies
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        // The HTML/CSS/JS pages in src/main/resources/static are public.
                        // The data they show comes from /api/** which needs the token.
                        .requestMatchers(HttpMethod.GET, "/", "/*.html", "/css/**", "/js/**", "/favicon.ico").permitAll()
                        .requestMatchers(HttpMethod.POST,
                                "/api/auth/register",
                                "/api/auth/login",
                                "/api/auth/forgot-password",
                                "/api/auth/reset-password").permitAll()
                        .requestMatchers("/error").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/farmers").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.POST, "/api/farmers").hasRole("ADMIN")
                        .anyRequest().authenticated())
                .exceptionHandling(ex -> ex
                        .authenticationEntryPoint((request, response, e) ->
                                writeError(request, response, 401, "Unauthorized", "Please log in to continue"))
                        .accessDeniedHandler((request, response, e) ->
                                writeError(request, response, 403, "Forbidden",
                                        "You are not allowed to access this resource")))
                .addFilterBefore(new JwtAuthenticationFilter(jwtService), UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    /** BCrypt hashes passwords with a random salt; the same password gives a different hash each time. */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /** Checks phone/email + password at login using our users table. */
    @Bean
    public AuthenticationManager authenticationManager(AppUserDetailsService userDetailsService,
                                                       PasswordEncoder passwordEncoder) {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider(userDetailsService);
        provider.setPasswordEncoder(passwordEncoder);
        return new ProviderManager(provider);
    }

    /** Same JSON shape as GlobalExceptionHandler, for errors raised before a controller is reached. */
    private static void writeError(HttpServletRequest request, HttpServletResponse response, int status,
                                   String error, String message) throws IOException {
        response.setStatus(status);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        String json = "{\"timestamp\":\"" + LocalDateTime.now() + "\","
                + "\"status\":" + status + ","
                + "\"error\":\"" + error + "\","
                + "\"message\":\"" + message + "\","
                + "\"path\":\"" + escape(request.getRequestURI()) + "\"}";
        response.getWriter().write(json);
    }

    private static String escape(String value) {
        return value == null ? "" : value.replace("\\", "\\\\").replace("\"", "\\\"");
    }
}
