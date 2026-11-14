package com.farmtrack.farmtrack.config;

import com.farmtrack.farmtrack.entity.Role;
import com.farmtrack.farmtrack.entity.User;
import com.farmtrack.farmtrack.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.Locale;

/**
 * Creates an ADMIN login at startup when app.admin.email and app.admin.password are set
 * (e.g. through the ADMIN_EMAIL and ADMIN_PASSWORD environment variables).
 */
@Component
public class AdminInitializer implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(AdminInitializer.class);

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final String adminEmail;
    private final String adminPassword;

    public AdminInitializer(UserRepository userRepository, PasswordEncoder passwordEncoder,
                            @Value("${app.admin.email:}") String adminEmail,
                            @Value("${app.admin.password:}") String adminPassword) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.adminEmail = adminEmail;
        this.adminPassword = adminPassword;
    }

    @Override
    public void run(ApplicationArguments args) {
        if (adminEmail.isBlank() || adminPassword.isBlank()) {
            return;
        }
        String email = adminEmail.trim().toLowerCase(Locale.ROOT);
        if (userRepository.existsByEmailIgnoreCase(email)) {
            return;
        }
        User admin = new User();
        admin.setEmail(email);
        admin.setPasswordHash(passwordEncoder.encode(adminPassword));
        admin.setRole(Role.ADMIN);
        userRepository.save(admin);
        log.info("Admin account created for {}", email);
    }
}
