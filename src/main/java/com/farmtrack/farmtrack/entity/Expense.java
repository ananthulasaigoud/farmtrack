package com.farmtrack.farmtrack.entity;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDate;

/** Money spent on a crop (seeds, fertilizer, labour ...). Many expenses -> one crop. */
@Entity
public class Expense {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private ExpenseCategory category;

    // BigDecimal is used for money because double gives rounding errors.
    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal amount;

    @Column(nullable = false)
    private LocalDate expenseDate;

    @Column(length = 500)
    private String description;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "crop_id", nullable = false)
    private Crop crop;

    public Expense() {
    }

    public Long getId() {
        return id;
    }

    public ExpenseCategory getCategory() {
        return category;
    }

    public void setCategory(ExpenseCategory category) {
        this.category = category;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public LocalDate getExpenseDate() {
        return expenseDate;
    }

    public void setExpenseDate(LocalDate expenseDate) {
        this.expenseDate = expenseDate;
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
