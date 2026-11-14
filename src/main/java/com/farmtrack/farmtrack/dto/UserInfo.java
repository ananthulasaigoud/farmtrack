package com.farmtrack.farmtrack.dto;

import com.farmtrack.farmtrack.entity.Farmer;
import com.farmtrack.farmtrack.entity.User;

/** guest = true for the shared demo account (the pages show a notice and disable account changes). */
public record UserInfo(Long userId, Long farmerId, String name, String phone, String village,
                       String email, String role, boolean guest) {

    public static UserInfo from(User user, boolean guest) {
        Farmer farmer = user.getFarmer();
        return new UserInfo(
                user.getId(),
                farmer != null ? farmer.getId() : null,
                farmer != null ? farmer.getName() : "Administrator",
                farmer != null ? farmer.getPhone() : null,
                farmer != null ? farmer.getVillage() : null,
                user.getEmail(),
                user.getRole().name(),
                guest);
    }
}
