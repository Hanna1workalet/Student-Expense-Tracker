package com.aau.se.expensetracker.service;

import com.aau.se.expensetracker.model.Expense;
import com.aau.se.expensetracker.util.BudgetExceededException;

public interface BudgetService {

    void checkLimit(Expense expense) throws BudgetExceededException;
}
