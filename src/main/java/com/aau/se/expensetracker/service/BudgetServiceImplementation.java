package com.aau.se.expensetracker.service;

import com.aau.se.expensetracker.model.Budget;
import com.aau.se.expensetracker.model.Expense;
import com.aau.se.expensetracker.model.ExpenseCategory;
import com.aau.se.expensetracker.util.BudgetExceededException;
import com.aau.se.expensetracker.util.DataAccessException;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

/**
 * Default implementation of BudgetService. Uses category budget: total spent per category
 * + new expense must not exceed the category limit. Also provides 20%-left warnings.
 */
public class BudgetServiceImplementation implements BudgetService {

    private static final double WARNING_THRESHOLD = 0.8; // alert when 80% spent (20% left)

    private final Budget budget;
    private final ExpenseService expenseService;

    public BudgetServiceImplementation(Budget budget, ExpenseService expenseService) {
        this.budget = budget;
        this.expenseService = expenseService;
    }

    @Override
    public void checkLimit(Expense expense) throws BudgetExceededException, DataAccessException {
        double limit = budget.getLimit(expense.getCategory());
        double spentInCategory = sumSpentByCategory(expense.getCategory(), expense.getId());
        double totalAfterAdd = spentInCategory + expense.getAmount();
        if (totalAfterAdd > limit) {
            throw new BudgetExceededException(
                    String.format("Adding %.2f would exceed %s budget (limit: %.2f, already spent: %.2f)",
                            expense.getAmount(), expense.getCategory(), limit, spentInCategory));
        }
    }

    @Override
    public List<String> getBudgetWarnings() throws DataAccessException {
        List<Expense> expenses = expenseService.getAllExpenses();
        Map<ExpenseCategory, Double> spentByCategory = new EnumMap<>(ExpenseCategory.class);
        for (ExpenseCategory c : ExpenseCategory.values()) {
            spentByCategory.put(c, 0.0);
        }
        for (Expense e : expenses) {
            spentByCategory.merge(e.getCategory(), e.getAmount(), Double::sum);
        }
        List<String> warnings = new ArrayList<>();
        for (ExpenseCategory c : ExpenseCategory.values()) {
            double limit = budget.getLimit(c);
            double spent = spentByCategory.getOrDefault(c, 0.0);
            if (limit > 0 && spent >= WARNING_THRESHOLD * limit) {
                double percentUsed = limit > 0 ? (spent / limit) * 100 : 0;
                warnings.add(String.format("Only 20%% left: %s budget (%.0f%% used, %.2f of %.2f).",
                        c, percentUsed, spent, limit));
            }
        }
        return warnings;
    }

    private double sumSpentByCategory(ExpenseCategory category, String excludeExpenseId) throws DataAccessException {
        List<Expense> expenses = expenseService.getAllExpenses();
        return expenses.stream()
                .filter(e -> e.getCategory() == category && !e.getId().equals(excludeExpenseId))
                .mapToDouble(Expense::getAmount)
                .sum();
    }
}
