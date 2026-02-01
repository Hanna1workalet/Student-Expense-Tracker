package com.aau.se.expensetracker.view;

import com.aau.se.expensetracker.controller.ExpenseController;
import com.aau.se.expensetracker.model.Expense;
import com.aau.se.expensetracker.model.ExpenseCategory;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import java.time.LocalDate;
import java.util.List;

/**
 * JavaFX UI for adding and listing expenses.
 */
public class ExpenseView {

    private final ExpenseController expenseController;
    private final ObservableList<ExpenseTableItem> tableItems = FXCollections.observableArrayList();

    public ExpenseView(ExpenseController expenseController) {
        this.expenseController = expenseController;
    }

    public VBox build() {
        VBox main = new VBox(16);
        main.setPadding(new Insets(12));

        GridPane form = new GridPane();
        form.setHgap(10);
        form.setVgap(8);

        Spinner<Double> amountSpinner = new Spinner<>(0.0, 1_000_000.0, 0.0, 0.5);
        amountSpinner.setEditable(true);
        amountSpinner.setPrefWidth(120);

        DatePicker datePicker = new DatePicker(LocalDate.now());
        datePicker.setPrefWidth(140);

        ComboBox<ExpenseCategory> categoryCombo = new ComboBox<>(
                FXCollections.observableArrayList(ExpenseCategory.values()));
        categoryCombo.getSelectionModel().selectFirst();
        categoryCombo.setPrefWidth(140);

        form.add(new Label("Amount:"), 0, 0);
        form.add(amountSpinner, 1, 0);
        form.add(new Label("Date:"), 0, 1);
        form.add(datePicker, 1, 1);
        form.add(new Label("Category:"), 0, 2);
        form.add(categoryCombo, 1, 2);

        Button addBtn = new Button("Add Expense");
        addBtn.setDefaultButton(true);
        addBtn.setOnAction(e -> {
            double amount = amountSpinner.getValue() != null ? amountSpinner.getValue() : 0;
            if (amount <= 0) {
                showAlert(Alert.AlertType.WARNING, "Invalid amount", "Amount must be greater than 0.");
                return;
            }
            LocalDate date = datePicker.getValue() != null ? datePicker.getValue() : LocalDate.now();
            ExpenseCategory cat = categoryCombo.getValue() != null ? categoryCombo.getValue() : ExpenseCategory.FOOD;
            try {
                Expense added = expenseController.handleAddExpense(amount, date, cat);
                if (added != null) {
                    tableItems.add(new ExpenseTableItem(added));
                    amountSpinner.getValueFactory().setValue(0.0);
                    datePicker.setValue(LocalDate.now());
                    showBudgetWarningsIfAny();
                }
            } catch (RuntimeException ex) {
                showAlert(Alert.AlertType.ERROR, "Error", ex.getMessage());
            }
        });

        HBox formRow = new HBox(10);
        formRow.getChildren().addAll(form, addBtn);
        formRow.setAlignment(javafx.geometry.Pos.CENTER_LEFT);

        TableView<ExpenseTableItem> table = new TableView<>(tableItems);
        table.setPlaceholder(new Label("No expenses yet. Add one above."));
        table.setPrefHeight(280);

        TableColumn<ExpenseTableItem, Number> amountCol = new TableColumn<>("Amount");
        amountCol.setCellValueFactory(c -> c.getValue().amountProperty());
        amountCol.setPrefWidth(120);

        TableColumn<ExpenseTableItem, String> dateCol = new TableColumn<>("Date");
        dateCol.setCellValueFactory(c -> c.getValue().dateProperty());
        dateCol.setPrefWidth(120);

        TableColumn<ExpenseTableItem, String> categoryCol = new TableColumn<>("Category");
        categoryCol.setCellValueFactory(c -> c.getValue().categoryProperty());
        categoryCol.setPrefWidth(140);

        TableColumn<ExpenseTableItem, Void> deleteCol = new TableColumn<>("");
        deleteCol.setPrefWidth(80);
        deleteCol.setCellFactory(tc -> {
            Button cellBtn = new Button("Delete");
            cellBtn.getStyleClass().add("delete-btn");
            TableCell<ExpenseTableItem, Void> cell = new TableCell<>() {
                @Override
                protected void updateItem(Void item, boolean empty) {
                    super.updateItem(item, empty);
                    setGraphic(empty ? null : cellBtn);
                }
            };
            cellBtn.setOnAction(ev -> {
                ExpenseTableItem row = cell.getTableRow().getItem();
                if (row != null) {
                    expenseController.handleDeleteExpense(row.getId());
                    tableItems.remove(row);
                }
            });
            return cell;
        });

        table.getColumns().addAll(amountCol, dateCol, categoryCol, deleteCol);

        refreshTable();

        main.getChildren().addAll(new Label("Add new expense"), formRow, new Separator(), new Label("Your expenses"), table);
        return main;
    }

    private void refreshTable() {
        tableItems.clear();
        List<Expense> all = expenseController.getAllExpenses();
        for (Expense e : all) {
            tableItems.add(new ExpenseTableItem(e));
        }
    }

    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert a = new Alert(type);
        a.setTitle(title);
        a.setHeaderText(null);
        a.setContentText(message);
        a.showAndWait();
    }

    private void showBudgetWarningsIfAny() {
        try {
            List<String> warnings = expenseController.getBudgetWarnings();
            if (!warnings.isEmpty()) {
                String message = String.join("\n", warnings);
                showAlert(Alert.AlertType.WARNING, "Budget warning – 20% left", message);
            }
        } catch (RuntimeException ignored) {
            // optional: show error
        }
    }

    /**
     * Table row model for TableView (amount, date, category only).
     */
    public static class ExpenseTableItem {
        private final String id;
        private final double amount;
        private final String date;
        private final String category;

        public ExpenseTableItem(Expense e) {
            this.id = e.getId();
            this.amount = e.getAmount();
            this.date = e.getDate().toString();
            this.category = e.getCategory().name();
        }

        public String getId() {
            return id;
        }

        public javafx.beans.property.SimpleDoubleProperty amountProperty() {
            return new javafx.beans.property.SimpleDoubleProperty(amount);
        }

        public javafx.beans.property.SimpleStringProperty dateProperty() {
            return new javafx.beans.property.SimpleStringProperty(date);
        }

        public javafx.beans.property.SimpleStringProperty categoryProperty() {
            return new javafx.beans.property.SimpleStringProperty(category);
        }
    }
}
