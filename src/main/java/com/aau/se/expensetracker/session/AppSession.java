package com.aau.se.expensetracker.session;

import com.aau.se.expensetracker.model.User;

/**
 * Holds the current user and budget for this app session (in-memory).
 * The expense screen is only shown after a user is created via the startup dialog.
 */
public final class AppSession {

    private static User currentUser;

    private AppSession() {
    }

    public static void setCurrentUser(User user) {
        currentUser = user;
    }

    public static User getCurrentUser() {
        return currentUser;
    }

    public static boolean hasUser() {
        return currentUser != null;
    }
}
