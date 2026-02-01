package com.aau.se.expensetracker.service;

import com.aau.se.expensetracker.model.Expense;
import com.aau.se.expensetracker.util.BudgetExceededException;

/**
 * Contract for budget limit checks (Dependency Inversion).
 */
public interface BudgetService {

    /**
     * Verifies that adding this expense would not exceed the budget limit for its category.
     *
     * @param expense the expense to check
     * @throws BudgetExceededException if the expense would exceed the limit
     */
    void checkLimit(Expense expense) throws BudgetExceededException;
}
