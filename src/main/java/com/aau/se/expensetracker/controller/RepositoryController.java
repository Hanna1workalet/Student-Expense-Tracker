package com.aau.se.expensetracker.controller;

import com.aau.se.expensetracker.model.Expense;
import com.aau.se.expensetracker.model.ExpenseCategory;
import com.aau.se.expensetracker.service.ExpenseService;
import com.aau.se.expensetracker.util.DataAccessException;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;

/**
 * Handles summary and reporting (Single Responsibility).
 */
public class RepositoryController {

    private final ExpenseService expenseService;

    public RepositoryController(ExpenseService expenseService) {
        this.expenseService = expenseService;
    }

    /**
     * Generates a summary of expenses: total spent and per-category totals.
     */
    public ExpenseSummary generateSummary() {
        try {
            List<Expense> expenses = expenseService.getAllExpenses();
            double total = 0;
            Map<ExpenseCategory, Double> byCategory = new EnumMap<>(ExpenseCategory.class);
            for (ExpenseCategory c : ExpenseCategory.values()) {
                byCategory.put(c, 0.0);
            }
            for (Expense e : expenses) {
                total += e.getAmount();
                byCategory.merge(e.getCategory(), e.getAmount(), Double::sum);
            }
            return new ExpenseSummary(total, byCategory, expenses.size());
        } catch (DataAccessException e) {
            throw new RuntimeException("Failed to generate summary: " + e.getMessage(), e);
        }
    }

    /**
     * DTO for expense summary.
     */
    public static final class ExpenseSummary {
        private final double totalSpent;
        private final Map<ExpenseCategory, Double> byCategory;
        private final int expenseCount;

        public ExpenseSummary(double totalSpent, Map<ExpenseCategory, Double> byCategory, int expenseCount) {
            this.totalSpent = totalSpent;
            this.byCategory = byCategory != null ? new EnumMap<>(byCategory) : new EnumMap<>(ExpenseCategory.class);
            this.expenseCount = expenseCount;
        }

        public double getTotalSpent() {
            return totalSpent;
        }

        public Map<ExpenseCategory, Double> getByCategory() {
            return byCategory;
        }

        public int getExpenseCount() {
            return expenseCount;
        }
    }
}
