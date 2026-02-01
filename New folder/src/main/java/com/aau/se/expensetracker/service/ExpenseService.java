package com.aau.se.expensetracker.service;

import com.aau.se.expensetracker.model.Expense;
import com.aau.se.expensetracker.util.DataAccessException;

import java.util.List;

/**
 * Contract for expense operations (Dependency Inversion).
 */
public interface ExpenseService {

    void addExpense(Expense expense) throws DataAccessException;

    void removeExpense(String expenseId) throws DataAccessException;

    List<Expense> getAllExpenses() throws DataAccessException;
}
