package com.aau.se.expensetracker.view;

import com.aau.se.expensetracker.controller.RepositoryController;
import com.aau.se.expensetracker.model.ExpenseCategory;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Separator;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;

import java.util.Map;

/**
 * JavaFX UI for expense summary (totals and by category).
 */
public class SummaryView {

    private final RepositoryController repositoryController;
    private Label totalLabel;
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

        countLabel = new Label();
        main.getChildren().add(countLabel);
        main.getChildren().add(new Separator());

        Label byCatLabel = new Label("By category:");
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
        totalLabel.setText(String.format("Total spent: %.2f", summary.getTotalSpent()));
        countLabel.setText(String.format("Number of expenses: %d", summary.getExpenseCount()));
        gridPane.getChildren().clear();
        int row = 0;
        for (Map.Entry<ExpenseCategory, Double> entry : summary.getByCategory().entrySet()) {
            gridPane.add(new Label(entry.getKey().name() + ":"), 0, row);
            gridPane.add(new Label(String.format("%.2f", entry.getValue())), 1, row);
            row++;
        }
    }
}
