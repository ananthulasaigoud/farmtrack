package com.farmtrack.farmtrack.security;

import com.farmtrack.farmtrack.entity.User;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;

/** Adapts our User entity to Spring Security's UserDetails (used only while checking the password at login). */
public class SecurityUser implements UserDetails {

    private final User user;
    private final String loginIdentifier;

    public SecurityUser(User user, String loginIdentifier) {
        this.user = user;
        this.loginIdentifier = loginIdentifier;
    }

    public User getUser() {
        return user;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority("ROLE_" + user.getRole().name()));
    }

    @Override
    public String getPassword() {
        return user.getPasswordHash();
    }

    @Override
    public String getUsername() {
        return loginIdentifier;
    }
}
