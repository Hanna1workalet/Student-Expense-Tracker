package com.aau.se.expensetracker.controller;

import com.aau.se.expensetracker.model.BasicExpense;//imported BasicExpense class 
import com.aau.se.expensetracker.model.Expense;//imported Expense class 
import com.aau.se.expensetracker.model.ExpenseCategory;//imported ExpenseCategory class 
import com.aau.se.expensetracker.service.BudgetService;//imported BudgetService class since the controller depends on the BudgetService
import com.aau.se.expensetracker.service.ExpenseService;//imported ExpenseService class since the controller depends on the ExpenseService
import com.aau.se.expensetracker.util.BudgetExceededException; //imported BudgetExceededException class since the controller depends on the BudgetExceededException class
import com.aau.se.expensetracker.util.DataAccessException; //imported DataAccessException class since the controller depends on the DataAccessException class

import java.time.LocalDate;// to store the date of the expense
import java.util.List;// to store the list of expenses
import java.util.UUID;// to generate unique IDs for expenses

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
     * Adds an expense after validating against budget limit.
     *
     * @param name     expense name
     * @param amount   amount
     * @param date     date
     * @param category category
     * @return the created expense, or null if budget exceeded or data error
     */
    public Expense handleAddExpense(String name, double amount, LocalDate date, ExpenseCategory category) {
        String id = UUID.randomUUID().toString();
        Expense expense = new BasicExpense(id, name, amount, date, category);
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
