package com.farmtrack.farmtrack.repository;

import com.farmtrack.farmtrack.entity.Role;
import com.farmtrack.farmtrack.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByEmailIgnoreCase(String email);

    Optional<User> findByFarmerPhone(String phone);

    Optional<User> findByFarmerId(Long farmerId);

    Optional<User> findByResetTokenHash(String resetTokenHash);

    boolean existsByEmailIgnoreCase(String email);

    boolean existsByRole(Role role);
}
