package com.farmtrack.farmtrack.entity;

/** Types of farming expenses. Stored as text in the database (e.g. "SEEDS"). */
public enum ExpenseCategory {
    SEEDS,
    FERTILIZER,
    LABOUR,
    PESTICIDE,
    IRRIGATION,
    MACHINERY,
    TRANSPORT,
    OTHER
}
