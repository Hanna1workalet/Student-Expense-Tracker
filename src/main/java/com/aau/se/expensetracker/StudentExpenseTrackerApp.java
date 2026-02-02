package com.aau.se.expensetracker;

import com.aau.se.expensetracker.controller.ExpenseController;
import com.aau.se.expensetracker.controller.RepositoryController;
import com.aau.se.expensetracker.model.User;
import com.aau.se.expensetracker.repository.ExpenseRepository;
import com.aau.se.expensetracker.repository.FileExpenseRepository;
import com.aau.se.expensetracker.service.BudgetService;
import com.aau.se.expensetracker.service.BudgetServiceImplementation;
import com.aau.se.expensetracker.service.ExpenseService;
import com.aau.se.expensetracker.service.ExpenseServiceImplementation;
import com.aau.se.expensetracker.util.SessionStore;
import com.aau.se.expensetracker.view.MainView;
import com.aau.se.expensetracker.model.ExpenseCategory;
import com.sun.tools.javac.Main;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.TextInputDialog;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.EnumMap;
import java.util.Map;
import java.util.Optional;
import javafx.scene.control.Button;
import javafx.application.Platform;

/**
 * JavaFX application entry point. Wires model, repository, services, controllers and views.
 * Shows TextInputDialog for user name and budget when no session; expense screen only visible after user exists.
 */
public class StudentExpenseTrackerApp extends Application {

    private static final String EXPENSE_FILE = "expenses.txt";

    @Override
    public void start(Stage stage) {
        ExpenseRepository repository = new FileExpenseRepository(EXPENSE_FILE);
        ExpenseService expenseService = new ExpenseServiceImplementation(repository);
        SessionStore sessionStore = new SessionStore();

        User user = sessionStore.load();
        if (user == null) {
            user = showUserAndBudgetDialogs();
            if (user == null) {
                stage.close();
                return;
            }
            try {
                sessionStore.save(user);
            } catch (IOException e) {
                Alert a = new Alert(Alert.AlertType.ERROR);
                a.setContentText("Could not save session: " + e.getMessage());
                a.showAndWait();
                stage.close();
                return;
            }
        }

        BudgetService budgetService = new BudgetServiceImplementation(user.getBudget(), expenseService);
        ExpenseController expenseController = new ExpenseController(expenseService, budgetService);
        RepositoryController repositoryController = new RepositoryController(expenseService, budgetService);

        MainView mainView = new MainView(expenseController, repositoryController, stage);
        Button logoutBtn = new Button("Logout");

        logoutBtn.setOnAction(e -> {
            try {
                sessionStore.clear(); // destroy session
            } catch (IOException ex) {
                Alert a = new Alert(Alert.AlertType.ERROR);
                a.setContentText("Could not logout: " + ex.getMessage());
                a.showAndWait();
                return;
            }
            stage.close(); // closes the app cleanly
        });


        BorderPane root = mainView.build();
        root.setTop(logoutBtn);

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
     * Shows TextInputDialog for name, overall budget, then per-category budget limits.
     * Validates that sum of category limits equals overall limit. Account is saved to session in start().
     */
    private User showUserAndBudgetDialogs() {
        TextInputDialog nameDialog = new TextInputDialog();
        nameDialog.setTitle("Welcome");
        nameDialog.setHeaderText("Create your account");
        nameDialog.setContentText("Enter your name:");
        Optional<String> nameOpt = nameDialog.showAndWait();
        if (nameOpt.isEmpty() || nameOpt.get().isBlank()) return null;
        String username = nameOpt.get().trim();

        TextInputDialog budgetDialog = new TextInputDialog("1000");
        budgetDialog.setTitle("Budget");
        budgetDialog.setHeaderText("Set your overall monthly budget");
        budgetDialog.setContentText("Overall monthly budget amount:");
        Optional<String> budgetOpt = budgetDialog.showAndWait();
        if (budgetOpt.isEmpty() || budgetOpt.get().isBlank()) return null;
        double monthlyLimit;
        try {
            monthlyLimit = Double.parseDouble(budgetOpt.get().trim());
            if (monthlyLimit <= 0) return null;
        } catch (NumberFormatException e) {
            return null;
        }

        Map<ExpenseCategory, Double> categoryLimits = showCategoryBudgetDialogs(monthlyLimit);
        if (categoryLimits == null) return null;

        return SessionStore.createUser(username, monthlyLimit, categoryLimits);
    }

    /**
     * Pop-up for each category asking for that category's budget limit.
     * Repeats until sum of category limits equals overall limit (or user cancels).
     */
    private Map<ExpenseCategory, Double> showCategoryBudgetDialogs(double overallLimit) {
        final double tolerance = 0.01;
        while (true) {
            EnumMap<ExpenseCategory, Double> limits = new EnumMap<>(ExpenseCategory.class);
            for (ExpenseCategory cat : ExpenseCategory.values()) {
                TextInputDialog d = new TextInputDialog("0");
                d.setTitle("Category budget");
                d.setHeaderText("Budget limit for " + cat.name());
                d.setContentText("Amount for " + cat.name() + ":");
                Optional<String> opt = d.showAndWait();
                if (opt.isEmpty()) return null;
                try {
                    double val = Double.parseDouble(opt.get().trim());
                    if (val < 0) return null;
                    limits.put(cat, val);
                } catch (NumberFormatException e) {
                    return null;
                }
            }
            double sum = limits.values().stream().mapToDouble(Double::doubleValue).sum();
            if (Math.abs(sum - overallLimit) <= tolerance) {
                return limits;
            }
            Alert a = new Alert(Alert.AlertType.WARNING);
            a.setTitle("Budget mismatch");
            a.setHeaderText("Category total must equal overall budget");
            a.setContentText(String.format("Sum of category limits (%.2f) must equal overall limit (%.2f). Please enter the category limits again.", sum, overallLimit));
            a.showAndWait();

        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}
