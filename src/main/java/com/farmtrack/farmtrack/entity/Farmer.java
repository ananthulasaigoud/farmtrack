package com.farmtrack.farmtrack.entity;

import jakarta.persistence.*;

import java.util.ArrayList;
import java.util.List;

/**
 * A farmer who owns crops. One farmer -> many crops.
 * Input validation lives in the request DTOs (see dto package), so the entity stays a plain table mapping.
 */
@Entity     // Tells Hibernate/JPA that this class represents a database table.
public class Farmer {

    @Id  // Primary key
    @GeneratedValue(strategy = GenerationType.IDENTITY) // Use MySQL auto-increment for the id
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false, unique = true, length = 10)
    private String phone;

    @Column(nullable = false)
    private String village;

    // Deleting a farmer also deletes their crops (and, through Crop, their expenses and incomes).
    @OneToMany(mappedBy = "farmer", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Crop> crops = new ArrayList<>();

    public Farmer() {
    }

    public Farmer(String name, String phone, String village) {
        this.name = name;
        this.phone = phone;
        this.village = village;
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getVillage() {
        return village;
    }

    public void setVillage(String village) {
        this.village = village;
    }

    public List<Crop> getCrops() {
        return crops;
    }
}
