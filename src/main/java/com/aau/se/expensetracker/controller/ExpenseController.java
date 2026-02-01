package com.aau.se.expensetracker.controller;

import com.aau.se.expensetracker.model.BasicExpense;
import com.aau.se.expensetracker.model.Expense;
import com.aau.se.expensetracker.model.ExpenseCategory;
import com.aau.se.expensetracker.service.BudgetService;
import com.aau.se.expensetracker.service.ExpenseService;
import com.aau.se.expensetracker.util.BudgetExceededException;
import com.aau.se.expensetracker.util.DataAccessException;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;


/**
 * Handles user actions for adding and deleting expenses (Single Responsibility).
 */
public class ExpenseController {

    private final ExpenseService expenseService;//to add and delete expenses
    private final BudgetService budgetService;//to check the budget limit
    //constructor to initialize the expenseService and budgetService
    public ExpenseController(ExpenseService expenseService, BudgetService budgetService) {
        this.expenseService = expenseService;
        this.budgetService = budgetService;
    }

    /**
     * Adds an expense after validating against category budget (total spent in category + this amount).
     *
     * @param amount   amount
     * @param date     date
     * @param category category
     * @return the created expense
     */
    public Expense handleAddExpense(double amount, LocalDate date, ExpenseCategory category) {
        String id = UUID.randomUUID().toString();
        Expense expense = new BasicExpense(id, amount, date, category);
        try {
            budgetService.checkLimit(expense);
            expenseService.addExpense(expense);
            return expense;
        } catch (BudgetExceededException e) {
            throw new RuntimeException("Budget exceeded: " + e.getMessage(), e);
        } catch (DataAccessException e) {
            throw new RuntimeException("Failed to save expense: " + e.getMessage(), e);
        }
    }

    /**
     * Returns warnings for categories where 20% or less of the budget is left (80%+ spent).
     */
    public List<String> getBudgetWarnings() {
        try {
            return budgetService.getBudgetWarnings();
        } catch (DataAccessException e) {
            throw new RuntimeException("Failed to get budget warnings: " + e.getMessage(), e);
        }
    }

    /**
     * Removes an expense by id.
     */
    public void handleDeleteExpense(String expenseId) {
        try {
            expenseService.removeExpense(expenseId);
        } catch (DataAccessException e) {
            throw new RuntimeException("Failed to delete expense: " + e.getMessage(), e);
        }
    }

    public List<Expense> getAllExpenses() {
        try {
            return expenseService.getAllExpenses();
        } catch (DataAccessException e) {
            throw new RuntimeException("Failed to load expenses: " + e.getMessage(), e);
        }
    }
}
