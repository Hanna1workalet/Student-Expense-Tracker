package com.aau.se.expensetracker.util;

/**
 * Thrown when a data access operation fails (e.g. file I/O).
 */
public class DataAccessException extends Exception {

    public DataAccessException(String message) {
        super(message);
    }

    public DataAccessException(String message, Throwable cause) {
        super(message, cause);
    }
}
