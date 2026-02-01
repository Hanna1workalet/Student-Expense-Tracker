package com.aau.se.expensetracker.model;

import java.time.LocalDate;

/**
 * Abstract base class for an expense entity.
 * Follows SOLID: Single Responsibility (expense data only), Open/Closed (extend via subclasses).
 */
public abstract class Expense {

    private final String id;
    private final double amount;
    private final LocalDate date;
    private final ExpenseCategory category;

    /**
     * Protected constructor for use by subclasses.
     *
     * @param id       unique identifier
     * @param amount   monetary value
     * @param date     date of the expense
     * @param category category of the expense
     */
    protected Expense(String id, double amount, LocalDate date, ExpenseCategory category) {
        this.id = id;
        this.amount = amount;
        this.date = date;
        this.category = category;
    }

    public double getAmount() {
        return amount;
    }

    public ExpenseCategory getCategory() {
        return category;
    }

    public String getId() {
        return id;
    }

    public LocalDate getDate() {
        return date;
    }
}
