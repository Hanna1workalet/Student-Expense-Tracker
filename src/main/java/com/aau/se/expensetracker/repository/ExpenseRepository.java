package com.aau.se.expensetracker.repository;

import com.aau.se.expensetracker.model.Expense;
import com.aau.se.expensetracker.util.DataAccessException;

import java.util.List;

/**
 * Data access contract for expenses (Dependency Inversion: depend on abstraction).
 */
public interface ExpenseRepository {

    void save(Expense expense) throws DataAccessException;

    List<Expense> loadAll() throws DataAccessException;

    void delete(String expenseId) throws DataAccessException;
}
