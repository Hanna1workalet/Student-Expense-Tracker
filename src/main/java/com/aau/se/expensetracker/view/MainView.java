package com.aau.se.expensetracker.view;

import com.aau.se.expensetracker.controller.ExpenseController;
import com.aau.se.expensetracker.controller.RepositoryController;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.StackPane;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.stage.Stage;

/**
 * Main JavaFX view: tabbed interface with Expense view and Summary.
 */
public class MainView {

    private final ExpenseController expenseController;
    private final RepositoryController repositoryController;
    private final Stage stage;

    public MainView(ExpenseController expenseController,
                    RepositoryController repositoryController,
                    Stage stage) {
        this.expenseController = expenseController;
        this.repositoryController = repositoryController;
        this.stage = stage;
    }

    public BorderPane build() {
        BorderPane root = new BorderPane();
        root.setPadding(new Insets(12));
        root.getStyleClass().add("root");

        Text title = new Text("Student Expense Tracker");
        title.setFont(Font.font("Segoe UI", 24));
        StackPane top = new StackPane(title);
        top.setAlignment(Pos.CENTER);
        top.setPadding(new Insets(0, 0, 12, 0));
        root.setTop(top);

        TabPane tabs = new TabPane();
        Tab expensesTab = new Tab("Expenses", new ExpenseView(expenseController).build());
        expensesTab.setClosable(false);
        Tab summaryTab = new Tab("Summary", new SummaryView(repositoryController).build());
        summaryTab.setClosable(false);
        tabs.getTabs().addAll(expensesTab, summaryTab);
        root.setCenter(tabs);

        return root;
    }
}