package com.aau.se.expensetracker.util;

/**
 * Thrown when an expense would exceed the budget limit.
 */
public class BudgetExceededException extends Exception {

    public BudgetExceededException(String message) {
        super(message);
    }

    public BudgetExceededException(String message, Throwable cause) {
        super(message, cause);
    }
}
