package com.aau.se.expensetracker.service;
import com.aau.se.expensetracker.model.Expense;
import com.aau.se.expensetracker.model.ExpenseCategory;
import com.aau.se.expensetracker.util.BudgetExceededException;
/**
 * Contract for budget limit checks (Dependency Inversion).
 */
public interface BudgetService {
    /**
     * Verifies that adding this expense would not exceed the budget limit for its category.
     * Enforces: total spent in category + new expense ≤ category limit.
     *
     * @param expense the expense to check
     * @throws BudgetExceededException if the expense would exceed the limit
     */
    void checkLimit(Expense expense) throws BudgetExceededException;
    /**
     * Returns the remaining budget percentage for the given category (0–100).
     * (limit - spent) / limit * 100. Returns 100 if limit is 0.
     */
    double getRemainingPercent(ExpenseCategory category);
    /** Returns the overall monthly budget limit. */
    double getMonthlyLimit();
    /** Returns the budget limit for the given category. */
    double getLimit(ExpenseCategory category);
}
