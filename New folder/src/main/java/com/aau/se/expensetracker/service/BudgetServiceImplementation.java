package com.aau.se.expensetracker.service;
import com.aau.se.expensetracker.model.Budget;
import com.aau.se.expensetracker.model.Expense;
import com.aau.se.expensetracker.model.ExpenseCategory;
import com.aau.se.expensetracker.util.BudgetExceededException;
import com.aau.se.expensetracker.util.DataAccessException;
import java.util.List;
/**
 * Default implementation of BudgetService. Enforces: total spent in category + new expense ≤ limit.
 */
public class BudgetServiceImplementation implements BudgetService {
    private final Budget budget;
    private final ExpenseService expenseService;
    public BudgetServiceImplementation(Budget budget, ExpenseService expenseService) {
        this.budget = budget;
        this.expenseService = expenseService;
    }
    @Override
    public void checkLimit(Expense expense) throws BudgetExceededException {
        double limit = budget.getLimit(expense.getCategory());
        double currentSpent = getSpentInCategory(expense.getCategory());
        double afterAdd = currentSpent + expense.getAmount();
        if (afterAdd > limit) {
            throw new BudgetExceededException(
                    String.format("Over budget: adding %.2f would exceed limit %.2f for category %s (spent: %.2f)",
                            expense.getAmount(), limit, expense.getCategory(), currentSpent));
        }
    }
    @Override
    public double getRemainingPercent(ExpenseCategory category) {
        double limit = budget.getLimit(category);
        if (limit <= 0) return 100.0;
        double spent = getSpentInCategory(category);
        double remaining = limit - spent;
        if (remaining <= 0) return 0.0;
        return (remaining / limit) * 100.0;
    }
    @Override
    public double getMonthlyLimit() {
        return budget.getMonthlyLimit();
    }
    @Override
    public double getLimit(ExpenseCategory category) {
        return budget.getLimit(category);
    }
    private double getSpentInCategory(ExpenseCategory category) {
        try {
            List<Expense> all = expenseService.getAllExpenses();
            return all.stream()
                    .filter(e -> e.getCategory() == category)
                    .mapToDouble(Expense::getAmount)
                    .sum();
        } catch (DataAccessException e) {
            return 0.0;
        }
    }
}
