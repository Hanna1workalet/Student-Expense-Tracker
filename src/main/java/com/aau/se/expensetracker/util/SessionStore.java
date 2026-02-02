package com.aau.se.expensetracker.util;

import com.aau.se.expensetracker.model.Budget;
import com.aau.se.expensetracker.model.ExpenseCategory;
import com.aau.se.expensetracker.model.User;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.EnumMap;
import java.util.Map;
import java.util.Properties;
import java.util.UUID;

/**
 * Persists and loads user session (username + budget) to a properties file.
 * No FXML or auth; just a simple file-based "session".
 */
public class SessionStore {

    private static final String SESSION_FILE = "user_session.properties";
    private static final String KEY_USER_ID = "user.id";
    private static final String KEY_USERNAME = "user.name";
    private static final String KEY_MONTHLY_LIMIT = "budget.monthlyLimit";
    private static final String KEY_CATEGORY_PREFIX = "budget.category.";

    private final Path path;

    public SessionStore() {
        this.path = Path.of(SESSION_FILE).toAbsolutePath();
    }
    public void clear() throws IOException {
        Files.deleteIfExists(Path.of(SESSION_FILE));
    }

    public SessionStore(Path path) {
        this.path = path.toAbsolutePath();
    }

    /**
     * Saves the current user and budget to the session file.
     */
    public void save(User user) throws IOException {
        if (user == null) return;
        Properties p = new Properties();
        p.setProperty(KEY_USER_ID, user.getId());
        p.setProperty(KEY_USERNAME, user.getUsername());
        Budget b = user.getBudget();
        p.setProperty(KEY_MONTHLY_LIMIT, String.valueOf(b.getMonthlyLimit()));
        for (ExpenseCategory c : ExpenseCategory.values()) {
            p.setProperty(KEY_CATEGORY_PREFIX + c.name(), String.valueOf(b.getLimit(c)));
        }
        Path parent = path.getParent();
        if (parent != null && !Files.exists(parent)) {
            Files.createDirectories(parent);
        }
        try (var w = Files.newBufferedWriter(path, StandardCharsets.UTF_8)) {
            p.store(w, "Student Expense Tracker session");
        }
    }

    /**
     * Loads user and budget from session file. Returns null if no session or file missing.
     */
    public User load() {
        if (!Files.exists(path)) return null;
        try {
            Properties p = new Properties();
            try (var r = Files.newBufferedReader(path, StandardCharsets.UTF_8)) {
                p.load(r);
            }
            String id = p.getProperty(KEY_USER_ID);
            String username = p.getProperty(KEY_USERNAME);
            String monthlyStr = p.getProperty(KEY_MONTHLY_LIMIT);
            if (id == null || id.isBlank() || username == null || username.isBlank() || monthlyStr == null || monthlyStr.isBlank()) {
                return null;
            }
            double monthlyLimit = Double.parseDouble(monthlyStr.trim());
            Map<ExpenseCategory, Double> categoryLimit = new EnumMap<>(ExpenseCategory.class);
            for (ExpenseCategory c : ExpenseCategory.values()) {
                String val = p.getProperty(KEY_CATEGORY_PREFIX + c.name());
                if (val != null && !val.isBlank()) {
                    try {
                        categoryLimit.put(c, Double.parseDouble(val.trim()));
                    } catch (NumberFormatException ignored) {
                        categoryLimit.put(c, monthlyLimit);
                    }
                } else {
                    categoryLimit.put(c, monthlyLimit);
                }
            }
            Budget budget = new Budget(monthlyLimit, categoryLimit);
            return new User(id, username, budget);
        } catch (IOException | NumberFormatException e) {
            return null;
        }
    }

    /**
     * Builds a new User from username and budget (e.g. after dialog input).
     */
    public static User createUser(String username, double monthlyLimit, Map<ExpenseCategory, Double> categoryLimits) {
        String id = UUID.randomUUID().toString();
        if (categoryLimits == null || categoryLimits.isEmpty()) {
            EnumMap<ExpenseCategory, Double> all = new EnumMap<>(ExpenseCategory.class);
            for (ExpenseCategory c : ExpenseCategory.values()) {
                all.put(c, monthlyLimit);
            }
            categoryLimits = all;
        }
        Budget budget = new Budget(monthlyLimit, categoryLimits);
        return new User(id, username, budget);
    }
}
