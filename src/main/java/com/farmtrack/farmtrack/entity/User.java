package com.farmtrack.farmtrack.entity;

import jakarta.persistence.*;

import java.time.LocalDateTime;

/**
 * Login account. Kept separate from Farmer so that authentication data (password hash, role,
 * reset tokens) never travels with normal farmer records.
 *
 * A FARMER user logs in with the phone number of the linked farmer, or with the optional email.
 * An ADMIN user has no farmer and logs in with email.
 */
@Entity
@Table(name = "users") // "user" is a reserved word in many databases
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true)
    private String email;

    /** BCrypt hash. The plain password is never stored. */
    @Column(name = "password_hash", nullable = false)
    private String passwordHash;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Role role;

    @OneToOne
    @JoinColumn(name = "farmer_id", unique = true)
    private Farmer farmer;

    /** SHA-256 hash of the active password reset token (null when none). */
    private String resetTokenHash;
    private LocalDateTime resetTokenExpiry;

    public User() {
    }

    public Long getId() {
        return id;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
    }

    public Role getRole() {
        return role;
    }

    public void setRole(Role role) {
        this.role = role;
    }

    public Farmer getFarmer() {
        return farmer;
    }

    public void setFarmer(Farmer farmer) {
        this.farmer = farmer;
    }

    public String getResetTokenHash() {
        return resetTokenHash;
    }

    public void setResetTokenHash(String resetTokenHash) {
        this.resetTokenHash = resetTokenHash;
    }

    public LocalDateTime getResetTokenExpiry() {
        return resetTokenExpiry;
    }

    public void setResetTokenExpiry(LocalDateTime resetTokenExpiry) {
        this.resetTokenExpiry = resetTokenExpiry;
    }
}
