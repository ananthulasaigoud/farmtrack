package com.farmtrack.farmtrack.service;

import com.farmtrack.farmtrack.config.GuestAccount;
import com.farmtrack.farmtrack.dto.*;
import com.farmtrack.farmtrack.entity.Farmer;
import com.farmtrack.farmtrack.entity.Role;
import com.farmtrack.farmtrack.entity.User;
import com.farmtrack.farmtrack.exception.BadRequestException;
import com.farmtrack.farmtrack.exception.DuplicateResourceException;
import com.farmtrack.farmtrack.exception.ResourceNotFoundException;
import com.farmtrack.farmtrack.repository.FarmerRepository;
import com.farmtrack.farmtrack.repository.UserRepository;
import com.farmtrack.farmtrack.security.AppUserDetailsService;
import com.farmtrack.farmtrack.security.CurrentUserService;
import com.farmtrack.farmtrack.security.JwtService;
import com.farmtrack.farmtrack.security.SecurityUser;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.util.HexFormat;
import java.util.Locale;
import java.util.Optional;
import java.util.UUID;

@Service
public class AuthService {

    private final FarmerRepository farmerRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final AppUserDetailsService userDetailsService;
    private final CurrentUserService currentUser;
    private final GuestAccount guestAccount;
    private final long resetTokenMinutes;
    private final boolean exposeResetToken;

    public AuthService(FarmerRepository farmerRepository, UserRepository userRepository,
                       PasswordEncoder passwordEncoder, AuthenticationManager authenticationManager,
                       JwtService jwtService, AppUserDetailsService userDetailsService,
                       CurrentUserService currentUser, GuestAccount guestAccount,
                       @Value("${app.auth.reset-token-minutes:15}") long resetTokenMinutes,
                       @Value("${app.auth.expose-reset-token:false}") boolean exposeResetToken) {
        this.farmerRepository = farmerRepository;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.authenticationManager = authenticationManager;
        this.jwtService = jwtService;
        this.userDetailsService = userDetailsService;
        this.currentUser = currentUser;
        this.guestAccount = guestAccount;
        this.resetTokenMinutes = resetTokenMinutes;
        this.exposeResetToken = exposeResetToken;
    }

    /**
     * Creates the farmer and the login account, then logs them in.
     * If an admin already created a farmer record with this phone and it has no login yet,
     * the new account is linked to that existing record.
     */
    @Transactional
    public AuthResponse register(RegisterRequest request) {
        String email = normalizeEmail(request.email());
        if (email != null && userRepository.existsByEmailIgnoreCase(email)) {
            throw new DuplicateResourceException("Email " + email + " is already registered");
        }

        Optional<Farmer> existingFarmer = farmerRepository.findFirstByPhoneOrderByIdAsc(request.phone());
        Farmer farmer;
        if (existingFarmer.isPresent()) {
            farmer = existingFarmer.get();
            if (userRepository.findByFarmerId(farmer.getId()).isPresent()) {
                throw new DuplicateResourceException("Phone number " + request.phone() + " is already registered");
            }
            farmer.setName(request.name().trim());
            farmer.setVillage(request.village().trim());
        } else {
            farmer = farmerRepository.save(new Farmer(request.name().trim(), request.phone(), request.village().trim()));
        }

        User user = new User();
        user.setEmail(email);
        user.setPasswordHash(passwordEncoder.encode(request.password()));
        user.setRole(Role.FARMER);
        user.setFarmer(farmer);
        userRepository.save(user);

        return buildAuthResponse(user);
    }

    /** Throws BadCredentialsException (HTTP 401) when the phone/email or password is wrong. */
    @Transactional(readOnly = true)
    public AuthResponse login(LoginRequest request) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.identifier().trim(), request.password()));
        User user = ((SecurityUser) authentication.getPrincipal()).getUser();
        return buildAuthResponse(user);
    }

    /**
     * Generates a one-time reset token valid for a few minutes. Only its hash is stored.
     * The same message is returned whether or not the account exists, so this endpoint
     * cannot be used to discover which phone numbers are registered.
     */
    @Transactional
    public ForgotPasswordResponse forgotPassword(ForgotPasswordRequest request) {
        String token = null;
        Optional<User> user = userDetailsService.findByIdentifier(request.identifier());
        if (user.isPresent() && !guestAccount.isGuest(user.get())) { // the guest password can't be reset
            token = UUID.randomUUID().toString();
            user.get().setResetTokenHash(sha256(token));
            user.get().setResetTokenExpiry(LocalDateTime.now().plusMinutes(resetTokenMinutes));
            // TODO: send the token by SMS/email here once a provider is connected.
        }
        String message = "If an account exists for this phone/email, a password reset token has been generated. "
                + "It is valid for " + resetTokenMinutes + " minutes.";
        return new ForgotPasswordResponse(message, exposeResetToken ? token : null);
    }

    @Transactional
    public MessageResponse resetPassword(ResetPasswordRequest request) {
        User user = userRepository.findByResetTokenHash(sha256(request.token().trim()))
                .orElseThrow(() -> new BadRequestException("Invalid or expired reset token"));

        if (user.getResetTokenExpiry() == null || user.getResetTokenExpiry().isBefore(LocalDateTime.now())) {
            clearResetToken(user);
            throw new BadRequestException("Invalid or expired reset token");
        }

        user.setPasswordHash(passwordEncoder.encode(request.newPassword()));
        clearResetToken(user);
        return new MessageResponse("Password updated. You can now log in with your new password.");
    }

    @Transactional
    public MessageResponse changePassword(ChangePasswordRequest request) {
        User user = getCurrentUserEntity();
        guestAccount.checkNotGuest(user.getFarmer(), "change its password");
        if (!passwordEncoder.matches(request.currentPassword(), user.getPasswordHash())) {
            throw new BadRequestException("Current password is incorrect");
        }
        user.setPasswordHash(passwordEncoder.encode(request.newPassword()));
        return new MessageResponse("Password changed successfully");
    }

    @Transactional(readOnly = true)
    public UserInfo me() {
        User user = getCurrentUserEntity();
        return UserInfo.from(user, guestAccount.isGuest(user));
    }

    private User getCurrentUserEntity() {
        Long userId = currentUser.get().userId();
        return userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User account no longer exists"));
    }

    private AuthResponse buildAuthResponse(User user) {
        return new AuthResponse(jwtService.generateToken(user), "Bearer", jwtService.getExpirationMs(),
                UserInfo.from(user, guestAccount.isGuest(user)));
    }

    private void clearResetToken(User user) {
        user.setResetTokenHash(null);
        user.setResetTokenExpiry(null);
    }

    private static String normalizeEmail(String email) {
        return (email == null || email.isBlank()) ? null : email.trim().toLowerCase(Locale.ROOT);
    }

    private static String sha256(String value) {
        try {
            byte[] hash = MessageDigest.getInstance("SHA-256").digest(value.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hash);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 not available", e);
        }
    }
}
