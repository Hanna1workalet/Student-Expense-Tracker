package com.aau.se.expensetracker.view;

import com.aau.se.expensetracker.controller.RepositoryController;
import com.aau.se.expensetracker.model.ExpenseCategory;
import com.aau.se.expensetracker.service.BudgetService;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Separator;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;

import java.util.Map;

/**
 * JavaFX UI for expense summary: totals, by category, budget limit and amount left (overall and per category).
 */
public class SummaryView {

    private final RepositoryController repositoryController;
    private Label totalLabel;
    private Label overallBudgetLabel;
    private Label countLabel;
    private GridPane gridPane;

    public SummaryView(RepositoryController repositoryController) {
        this.repositoryController = repositoryController;
    }

    public VBox build() {
        VBox main = new VBox(12);
        main.setPadding(new Insets(12));

        totalLabel = new Label();
        totalLabel.setFont(Font.font("Segoe UI", 18));
        main.getChildren().add(totalLabel);

        overallBudgetLabel = new Label();
        overallBudgetLabel.setFont(Font.font("Segoe UI", 14));
        main.getChildren().add(overallBudgetLabel);

        countLabel = new Label();
        main.getChildren().add(countLabel);
        main.getChildren().add(new Separator());

        Label byCatLabel = new Label("By category (limit | spent | left):");
        byCatLabel.setFont(Font.font("Segoe UI", 14));
        main.getChildren().add(byCatLabel);

        gridPane = new GridPane();
        gridPane.setHgap(16);
        gridPane.setVgap(6);
        main.getChildren().add(gridPane);

        Button refreshBtn = new Button("Refresh summary");
        refreshBtn.setOnAction(e -> updateSummary());

        main.getChildren().add(new Separator());
        main.getChildren().add(refreshBtn);

        updateSummary();
        return main;
    }

    private void updateSummary() {
        RepositoryController.ExpenseSummary summary = repositoryController.generateSummary();
        BudgetService budget = summary.getBudgetService();
        double totalSpent = summary.getTotalSpent();
        double totalLimit = budget.getMonthlyLimit();
        double totalLeft = Math.max(0, totalLimit - totalSpent);

        totalLabel.setText(String.format("Total spent: %.2f", totalSpent));
        overallBudgetLabel.setText(String.format("Overall budget: limit %.2f  |  spent %.2f  |  left %.2f", totalLimit, totalSpent, totalLeft));
        overallBudgetLabel.getStyleClass().remove("budget-warning");
        if (totalLimit > 0 && (totalLeft / totalLimit) < 0.21) {
            overallBudgetLabel.getStyleClass().add("budget-warning");
        }
        countLabel.setText(String.format("Number of expenses: %d", summary.getExpenseCount()));
        gridPane.getChildren().clear();
        int row = 0;
        for (ExpenseCategory cat : ExpenseCategory.values()) {
            double spent = summary.getByCategory().getOrDefault(cat, 0.0);
            double limit = budget.getLimit(cat);
            double left = Math.max(0, limit - spent);
            Label catLabel = new Label(cat.name() + ":");
            Label valueLabel = new Label(String.format("limit %.2f  |  spent %.2f  |  left %.2f", limit, spent, left));
            valueLabel.getStyleClass().remove("budget-warning");
            if (limit > 0 && (left / limit) < 0.21) {
                valueLabel.getStyleClass().add("budget-warning");
            }
            gridPane.add(catLabel, 0, row);
            gridPane.add(valueLabel, 1, row);
            row++;
        }
    }
}