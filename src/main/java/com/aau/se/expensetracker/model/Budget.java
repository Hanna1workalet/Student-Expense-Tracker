package com.aau.se.expensetracker.model;

import java.util.Collections;
import java.util.EnumMap;
import java.util.Map;

/**
 * Holds monthly and per-category spending limits.
 */
public class Budget {

    private final double monthlyLimit;
    private final Map<ExpenseCategory, Double> categoryLimit;

    public Budget(double monthlyLimit, Map<ExpenseCategory, Double> categoryLimit) {
        this.monthlyLimit = monthlyLimit;
        this.categoryLimit = categoryLimit != null
                ? new EnumMap<>(categoryLimit)
                : new EnumMap<>(ExpenseCategory.class);
    }

    public double getMonthlyLimit() {
        return monthlyLimit;
    }

    public Map<ExpenseCategory, Double> getCategoryLimit() {
        return Collections.unmodifiableMap(categoryLimit);
    }

    /**
     * Returns the limit for the given category, or monthly limit if no category limit is set.
     */
    public double getLimit(ExpenseCategory category) {
        return categoryLimit.getOrDefault(category, monthlyLimit);
    }
}
