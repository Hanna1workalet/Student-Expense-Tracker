package com.aau.se.expensetracker.model;

/**
 * Represents a user of the Student Expense Tracker.
 */
public class User {

    private final String id;
    private final String username;
    private final Budget budget;

    public User(String id, String username, Budget budget) {
        this.id = id;
        this.username = username;
        this.budget = budget;
    }

    public String getId() {
        return id;
    }

    public String getUsername() {
        return username;
    }

    public Budget getBudget() {
        return budget;
    }
}
