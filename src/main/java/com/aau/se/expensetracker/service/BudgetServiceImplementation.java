package com.aau.se.expensetracker.service;

import com.aau.se.expensetracker.model.Budget;
import com.aau.se.expensetracker.model.Expense;
import com.aau.se.expensetracker.util.BudgetExceededException;

/**
 * Default implementation of BudgetService using a Budget instance.
 */
public class BudgetServiceImplementation implements BudgetService {

    private final Budget budget;

    public BudgetServiceImplementation(Budget budget) {
        this.budget = budget;
    }

    @Override
    public void checkLimit(Expense expense) throws BudgetExceededException {
        double limit = budget.getLimit(expense.getCategory());
        if (expense.getAmount() > limit) {
            throw new BudgetExceededException(
                    String.format("Expense amount %.2f exceeds limit %.2f for category %s",
                            expense.getAmount(), limit, expense.getCategory()));
        }
    }
}
