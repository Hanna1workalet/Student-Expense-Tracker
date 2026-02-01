package com.aau.se.expensetracker.service;

import com.aau.se.expensetracker.model.Expense;
import com.aau.se.expensetracker.repository.ExpenseRepository;
import com.aau.se.expensetracker.util.DataAccessException;

import java.util.List;

/**
 * Default implementation of ExpenseService (Single Responsibility: expense CRUD).
 */
public class ExpenseServiceImplementation implements ExpenseService {

    private final ExpenseRepository repository;

    public ExpenseServiceImplementation(ExpenseRepository repository) {
        this.repository = repository;
    }

    @Override
    public void addExpense(Expense expense) throws DataAccessException {
        repository.save(expense);
    }

    @Override
    public void removeExpense(String expenseId) throws DataAccessException {
        repository.delete(expenseId);
    }

    @Override
    public List<Expense> getAllExpenses() throws DataAccessException {
        return repository.loadAll();
    }
}
