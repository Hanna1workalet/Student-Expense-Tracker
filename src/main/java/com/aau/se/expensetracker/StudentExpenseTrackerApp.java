package com.aau.se.expensetracker;

import com.aau.se.expensetracker.controller.ExpenseController;
import com.aau.se.expensetracker.controller.RepositoryController;
import com.aau.se.expensetracker.model.Budget;
import com.aau.se.expensetracker.model.ExpenseCategory;
import com.aau.se.expensetracker.model.User;
import com.aau.se.expensetracker.repository.ExpenseRepository;
import com.aau.se.expensetracker.repository.FileExpenseRepository;
import com.aau.se.expensetracker.service.BudgetService;
import com.aau.se.expensetracker.service.BudgetServiceImplementation;
import com.aau.se.expensetracker.service.ExpenseService;
import com.aau.se.expensetracker.service.ExpenseServiceImplementation;
import com.aau.se.expensetracker.session.AppSession;
import com.aau.se.expensetracker.view.MainView;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.TextInputDialog;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;

import java.util.EnumMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

/**
 * JavaFX application entry point. Wires model, repository, services, controllers and views.
 * Shows a TextInputDialog to create user and budget before the expense screen is visible.
 */
public class StudentExpenseTrackerApp extends Application {

    private static final String EXPENSE_FILE = "expenses.txt";

    @Override
    public void start(Stage stage) {
        // --- Create user and budget via built-in TextInputDialog (no FXML, no auth) ---
        User user = promptUserAndBudget();
        if (user == null) {
            Platform.exit();
            return;
        }
        AppSession.setCurrentUser(user);
        Budget budget = user.getBudget();

        // Repository (file-based)
        ExpenseRepository repository = new FileExpenseRepository(EXPENSE_FILE);

        // Services (use the budget from the session user; BudgetService needs ExpenseService for category totals)
        ExpenseService expenseService = new ExpenseServiceImplementation(repository);
        BudgetService budgetService = new BudgetServiceImplementation(budget, expenseService);

        // Controllers
        ExpenseController expenseController = new ExpenseController(expenseService, budgetService);
        RepositoryController repositoryController = new RepositoryController(expenseService);

        // View (only built after user exists — expense screen was not visible before this)
        MainView mainView = new MainView(expenseController, repositoryController, stage);
        BorderPane root = mainView.build();

        Scene scene = new Scene(root, 720, 520);
        try {
            var css = getClass().getResource("/app.css");
            if (css != null) {
                scene.getStylesheets().add(css.toExternalForm());
            }
        } catch (Exception ignored) {
            // CSS optional
        }
        stage.setTitle("Student Expense Tracker – " + user.getUsername());
        stage.setScene(scene);
        stage.setMinWidth(600);
        stage.setMinHeight(400);
        stage.show();
    }

    /**
     * Shows TextInputDialogs: username, overall monthly budget, then per-category budgets.
     * Overall and each category are treated separately so each budget can be set independently.
     * Creates and returns a User with an associated Budget. Returns null if cancelled or invalid.
     */
    private User promptUserAndBudget() {
        TextInputDialog nameDialog = new TextInputDialog();
        nameDialog.setTitle("Welcome");
        nameDialog.setHeaderText("Create your profile");
        nameDialog.setContentText("Enter your username:");
        nameDialog.getEditor().setPromptText("e.g. Student1");

        Optional<String> nameResult = nameDialog.showAndWait();
        if (nameResult.isEmpty() || nameResult.get().isBlank()) {
            return null;
        }
        String username = nameResult.get().trim();

        TextInputDialog overallDialog = new TextInputDialog("1000");
        overallDialog.setTitle("Overall budget");
        overallDialog.setHeaderText("Set your overall monthly budget");
        overallDialog.setContentText("Overall monthly budget amount:");
        overallDialog.getEditor().setPromptText("e.g. 1000");

        Optional<String> overallResult = overallDialog.showAndWait();
        if (overallResult.isEmpty()) {
            return null;
        }
        double monthlyLimit;
        try {
            monthlyLimit = Double.parseDouble(overallResult.get().trim());
            if (monthlyLimit <= 0 || !Double.isFinite(monthlyLimit)) {
                showInvalidBudgetAlert("Please enter a positive number for your overall monthly budget.");
                return null;
            }
        } catch (NumberFormatException e) {
            showInvalidBudgetAlert("Please enter a positive number for your overall monthly budget.");
            return null;
        }

        ExpenseCategory[] categories = ExpenseCategory.values();
        String defaultCategoryBudgets = "200,200,100,100,50,50";
        TextInputDialog categoryDialog = new TextInputDialog(defaultCategoryBudgets);
        categoryDialog.setTitle("Category budgets");
        categoryDialog.setHeaderText("Set budget per category (treated separately)");
        categoryDialog.setContentText(
                "Enter budget for each category, comma-separated (order: FOOD, ACADEMIC, TRANSPORT, RECREATIONAL, OCCASIONAL, HYGIENE). Leave empty to use overall for all.");
        categoryDialog.getEditor().setPromptText("e.g. 200,200,100,100,50,50");

        Optional<String> categoryResult = categoryDialog.showAndWait();
        if (categoryResult.isEmpty()) {
            return null;
        }

        Map<ExpenseCategory, Double> categoryLimits = new EnumMap<>(ExpenseCategory.class);
        String input = categoryResult.get().trim();
        if (input.isEmpty()) {
            for (ExpenseCategory c : categories) {
                categoryLimits.put(c, monthlyLimit);
            }
        } else {
            String[] parts = input.split(",");
            for (int i = 0; i < categories.length; i++) {
                double limit;
                if (i < parts.length) {
                    try {
                        limit = Double.parseDouble(parts[i].trim());
                        if (limit <= 0 || !Double.isFinite(limit)) {
                            limit = monthlyLimit;
                        }
                    } catch (NumberFormatException e) {
                        limit = monthlyLimit;
                    }
                } else {
                    limit = monthlyLimit;
                }
                categoryLimits.put(categories[i], limit);
            }
        }

        Budget budget = new Budget(monthlyLimit, categoryLimits);
        String userId = UUID.randomUUID().toString();
        return new User(userId, username, budget);
    }

    private void showInvalidBudgetAlert(String message) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle("Invalid budget");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
