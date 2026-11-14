package com.farmtrack.farmtrack.entity;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDate;

/** A sale of harvested produce. totalAmount = quantity x pricePerUnit (calculated by the service). */
@Entity
public class Income {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal quantity;

    /** Unit of the quantity, e.g. quintal, kg, tonne. */
    @Column(length = 20)
    private String unit;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal pricePerUnit;

    @Column(nullable = false, precision = 14, scale = 2)
    private BigDecimal totalAmount;

    @Column(nullable = false)
    private LocalDate saleDate;

    @Column(length = 500)
    private String description;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "crop_id", nullable = false)
    private Crop crop;

    public Income() {
    }

    public Long getId() {
        return id;
    }

    public BigDecimal getQuantity() {
        return quantity;
    }

    public void setQuantity(BigDecimal quantity) {
        this.quantity = quantity;
    }

    public String getUnit() {
        return unit;
    }

    public void setUnit(String unit) {
        this.unit = unit;
    }

    public BigDecimal getPricePerUnit() {
        return pricePerUnit;
    }

    public void setPricePerUnit(BigDecimal pricePerUnit) {
        this.pricePerUnit = pricePerUnit;
    }

    public BigDecimal getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(BigDecimal totalAmount) {
        this.totalAmount = totalAmount;
    }

    public LocalDate getSaleDate() {
        return saleDate;
    }

    public void setSaleDate(LocalDate saleDate) {
        this.saleDate = saleDate;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Crop getCrop() {
        return crop;
    }

    public void setCrop(Crop crop) {
        this.crop = crop;
    }
}
