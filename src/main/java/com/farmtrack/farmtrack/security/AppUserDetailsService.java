package com.farmtrack.farmtrack.security;

import com.farmtrack.farmtrack.entity.User;
import com.farmtrack.farmtrack.repository.UserRepository;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

/** Finds a user by phone number or email (anything containing "@" is treated as an email). */
@Service
public class AppUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    public AppUserDetailsService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public Optional<User> findByIdentifier(String identifier) {
        if (identifier == null || identifier.isBlank()) {
            return Optional.empty();
        }
        String value = identifier.trim();
        return value.contains("@")
                ? userRepository.findByEmailIgnoreCase(value)
                : userRepository.findByFarmerPhone(value);
    }

    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String identifier) throws UsernameNotFoundException {
        return findByIdentifier(identifier)
                .map(user -> new SecurityUser(user, identifier))
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));
    }
}
