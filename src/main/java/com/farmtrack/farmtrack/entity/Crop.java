package com.farmtrack.farmtrack.entity;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * A crop grown by a farmer. One crop -> many expenses and many incomes (sales).
 */
@Entity
public class Crop {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String cropName;

    private String landDetails;
    private String season;
    private LocalDate sowingDate;
    private LocalDate expectedHarvestDate;

    @Column(length = 1000)
    private String additionalNotes;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "farmer_id", nullable = false)
    private Farmer farmer;

    @OneToMany(mappedBy = "crop", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Expense> expenses = new ArrayList<>();

    @OneToMany(mappedBy = "crop", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Income> incomes = new ArrayList<>();

    public Crop() {
    }

    /** Sum of all expense amounts recorded for this crop. */
    public BigDecimal getTotalExpenses() {
        return expenses.stream()
                .map(Expense::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    /** Sum of all sales recorded for this crop. */
    public BigDecimal getTotalIncome() {
        return incomes.stream()
                .map(Income::getTotalAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    /** profit = totalIncome - totalExpenses (negative means loss). */
    public BigDecimal getProfit() {
        return getTotalIncome().subtract(getTotalExpenses());
    }

    public Long getId() {
        return id;
    }

    public Farmer getFarmer() {
        return farmer;
    }

    public void setFarmer(Farmer farmer) {
        this.farmer = farmer;
    }

    public String getCropName() {
        return cropName;
    }

    public void setCropName(String cropName) {
        this.cropName = cropName;
    }

    public String getLandDetails() {
        return landDetails;
    }

    public void setLandDetails(String landDetails) {
        this.landDetails = landDetails;
    }

    public String getSeason() {
        return season;
    }

    public void setSeason(String season) {
        this.season = season;
    }

    public LocalDate getSowingDate() {
        return sowingDate;
    }

    public void setSowingDate(LocalDate sowingDate) {
        this.sowingDate = sowingDate;
    }

    public LocalDate getExpectedHarvestDate() {
        return expectedHarvestDate;
    }

    public void setExpectedHarvestDate(LocalDate expectedHarvestDate) {
        this.expectedHarvestDate = expectedHarvestDate;
    }

    public String getAdditionalNotes() {
        return additionalNotes;
    }

    public void setAdditionalNotes(String additionalNotes) {
        this.additionalNotes = additionalNotes;
    }

    public List<Expense> getExpenses() {
        return expenses;
    }

    public List<Income> getIncomes() {
        return incomes;
    }
}
