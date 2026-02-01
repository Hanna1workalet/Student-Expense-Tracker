package com.aau.se.expensetracker.model;

import java.time.LocalDate;

/**
 * Concrete implementation of Expense for expense entries.
 */
public class BasicExpense extends Expense {

    public BasicExpense(String id, double amount, LocalDate date, ExpenseCategory category) {
        super(id, amount, date, category);
    }
}
