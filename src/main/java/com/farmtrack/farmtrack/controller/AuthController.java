package com.farmtrack.farmtrack.controller;

import com.farmtrack.farmtrack.dto.*;
import com.farmtrack.farmtrack.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(authService.register(request));
    }

    @PostMapping("/login")
    public AuthResponse login(@Valid @RequestBody LoginRequest request) {
        return authService.login(request);
    }

    @PostMapping("/forgot-password")
    public ForgotPasswordResponse forgotPassword(@Valid @RequestBody ForgotPasswordRequest request) {
        return authService.forgotPassword(request);
    }

    @PostMapping("/reset-password")
    public MessageResponse resetPassword(@Valid @RequestBody ResetPasswordRequest request) {
        return authService.resetPassword(request);
    }

    @PostMapping("/change-password")
    public MessageResponse changePassword(@Valid @RequestBody ChangePasswordRequest request) {
        return authService.changePassword(request);
    }

    /** The logged-in user's details (used by the frontend after a page refresh). */
    @GetMapping("/me")
    public UserInfo me() {
        return authService.me();
    }

    /**
     * JWTs are stateless, so logout happens on the client by deleting the token.
     * This endpoint exists so the frontend has a single place to call.
     */
    @PostMapping("/logout")
    public MessageResponse logout() {
        return new MessageResponse("Logged out");
    }
}
