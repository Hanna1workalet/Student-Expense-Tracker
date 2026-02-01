package com.aau.se.expensetracker.service;

import com.aau.se.expensetracker.model.Expense;
import com.aau.se.expensetracker.util.BudgetExceededException;
import com.aau.se.expensetracker.util.DataAccessException;

import java.util.List;

/**
 * Contract for budget limit checks (Dependency Inversion).
 */
public interface BudgetService {

    /**
     * Verifies that adding this expense would not exceed the category budget
     * (existing spent in category + this expense &lt;= category limit).
     *
     * @param expense the expense to check
     * @throws BudgetExceededException if adding would exceed the category limit
     */
    void checkLimit(Expense expense) throws BudgetExceededException, DataAccessException;

    /**
     * Returns warning messages for categories where 80% or more of the budget has been spent
     * (20% or less remaining).
     *
     * @return list of warning strings, empty if none
     */
    List<String> getBudgetWarnings() throws DataAccessException;
}
