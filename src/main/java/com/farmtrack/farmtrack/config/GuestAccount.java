package com.farmtrack.farmtrack.config;

import com.farmtrack.farmtrack.entity.Farmer;
import com.farmtrack.farmtrack.entity.User;
import com.farmtrack.farmtrack.exception.BadRequestException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * Settings and rules for the shared guest (demo) account.
 * Anyone can log in as guest to try FarmTrack, so the guest cannot change the
 * account's password or details, or delete it. Crops, expenses and sales can be changed freely.
 */
@Component
public class GuestAccount {

    private final boolean enabled;
    private final String phone;
    private final String password;

    public GuestAccount(@Value("${app.guest.enabled:false}") boolean enabled,
                        @Value("${app.guest.phone:9999999999}") String phone,
                        @Value("${app.guest.password:guest123}") String password) {
        this.enabled = enabled;
        this.phone = phone;
        this.password = password;
    }

    public boolean isGuest(Farmer farmer) {
        return enabled && farmer != null && phone.equals(farmer.getPhone());
    }

    public boolean isGuest(User user) {
        return user != null && isGuest(user.getFarmer());
    }

    /** Throws 400 with a clear message if this is the guest account. */
    public void checkNotGuest(Farmer farmer, String action) {
        if (isGuest(farmer)) {
            throw new BadRequestException("The guest account is shared by everyone, so you can't " + action
                    + ". Register your own free account to do this.");
        }
    }

    public boolean isEnabled() {
        return enabled;
    }

    public String getPhone() {
        return phone;
    }

    public String getPassword() {
        return password;
    }
}
