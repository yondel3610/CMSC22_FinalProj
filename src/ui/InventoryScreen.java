package ui;

import java.util.List;

import inventory.Inventory;
import inventory.Product;
import inventory.StockMovement;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

// The Inventory screen (summary cards, filters, stock table with status badges)
// Restock + Adjust (manager only) 
// everyone can open the movement log of a product
public class InventoryScreen {

    private final Inventory inventory;
    private final boolean isManager;

    private final BorderPane view = new BorderPane();
    private final TableView<Product> table = new TableView<>();

    private TextField searchField;
    private ComboBox<String> categoryFilter;
    private ComboBox<String> statusFilter;

    private Label totalLabel;
    private Label lowLabel;
    private Label outLabel;
    private Label valueLabel;

    private Button restockBtn;
    private Button adjustBtn;
    private Button logBtn;

    public InventoryScreen(Inventory inventory, boolean isManager) {
        this.inventory = inventory;
        this.isManager = isManager;
        buildView();
        refresh();
    }

    public Region getView() {
        return view;
    }

    // Layouy
    private void buildView() {
        view.setStyle("-fx-background-color: #f4f6f8;");

        // summary cards
        totalLabel = new Label("0");
        lowLabel = new Label("0");
        outLabel = new Label("0");
        valueLabel = new Label("0.00");

        HBox cards = new HBox(12,
                card(totalLabel, "Active products", "#1a56a0"),
                card(lowLabel, "Low stock", "#b07d00"),
                card(outLabel, "Out of stock", "#c62828"),
                card(valueLabel, "Total estimated value (PHP)", "#2e7d32"));
        cards.setPadding(new Insets(16, 16, 12, 16));

        // filters
        searchField = new TextField();
        searchField.setPromptText("Search product...");
        searchField.setPrefWidth(260);
        searchField.textProperty().addListener((obs, oldVal, newVal) -> refresh());

        ObservableList<String> categories = FXCollections.observableArrayList("All Categories");
        categories.addAll(Product.CATEGORIES);
        categoryFilter = new ComboBox<>(categories);
        categoryFilter.setValue("All Categories");

        statusFilter = new ComboBox<>(FXCollections.observableArrayList(
                "All Statuses", Inventory.STATUS_OK, Inventory.STATUS_LOW, Inventory.STATUS_OUT));
        statusFilter.setValue("All Statuses");

        categoryFilter.valueProperty().addListener((obs, oldVal, newVal) -> refresh());
        statusFilter.valueProperty().addListener((obs, oldVal, newVal) -> refresh());

        HBox filters = new HBox(10,
                new Label("Search:"), searchField,
                new Label("Category:"), categoryFilter,
                new Label("Status:"), statusFilter);
        filters.setAlignment(Pos.CENTER_LEFT);
        filters.setPadding(new Insets(0, 16, 12, 16));

        VBox topArea = new VBox(cards, filters);
        view.setTop(topArea);

        buildTable();
        BorderPane.setMargin(table, new Insets(0, 16, 0, 16));
        view.setCenter(table);

        // buttons 
        logBtn = new Button("View Movement Log");
        logBtn.setDisable(true);
        logBtn.setOnAction(e -> openMovementLog());

        HBox buttons;
        if (isManager) {
            restockBtn = new Button("Restock");
            restockBtn.setDisable(true);
            restockBtn.setStyle("-fx-background-color: #1a56a0; -fx-text-fill: white;");
            restockBtn.setOnAction(e -> openRestockDialog());

            adjustBtn = new Button("Adjust Stock");
            adjustBtn.setDisable(true);
            adjustBtn.setOnAction(e -> openAdjustDialog());

            buttons = new HBox(10, restockBtn, adjustBtn, logBtn);
        } else {
            buttons = new HBox(10, logBtn);
        }

        table.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, sel) -> {
            boolean none = sel == null;
            logBtn.setDisable(none);
            if (isManager) {
                restockBtn.setDisable(none);
                adjustBtn.setDisable(none);
            }
        });

        buttons.setPadding(new Insets(12, 16, 16, 16));
        view.setBottom(buttons);
    }

    private VBox card(Label value, String caption, String color) {
        value.setStyle("-fx-font-size: 22px; -fx-font-weight: bold; -fx-text-fill: " + color + ";");
        Label cap = new Label(caption);
        cap.setStyle("-fx-font-size: 12px; -fx-text-fill: #5a6b7b;");
        VBox box = new VBox(4, value, cap);
        box.setAlignment(Pos.CENTER_LEFT);
        
        // asked claude for help in this vbox, sana di na tanungin ni maam 
        box.setStyle("-fx-background-color: white; -fx-background-radius: 8; "
                + "-fx-padding: 12 18 12 18; "
                + "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.10), 5, 0.1, 0, 2);");
        HBox.setHgrow(box, Priority.ALWAYS);
        box.setMaxWidth(Double.MAX_VALUE);
        return box;
    }

    private void buildTable() {
        table.setPlaceholder(new Label("No products match the current filters."));

        TableColumn<Product, String> nameCol = new TableColumn<>("Product");
        nameCol.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getName()));
        nameCol.setPrefWidth(220);

        TableColumn<Product, String> catCol = new TableColumn<>("Category");
        catCol.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getCategory()));
        catCol.setPrefWidth(110);

        TableColumn<Product, Number> stockCol = new TableColumn<>("Stock");
        stockCol.setCellValueFactory(d ->
                new SimpleIntegerProperty(inventory.stockOf(d.getValue().getId())));
        stockCol.setPrefWidth(80);
        stockCol.setStyle("-fx-alignment: CENTER-RIGHT;");

        TableColumn<Product, String> unitCol = new TableColumn<>("Unit");
        unitCol.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getUnit()));
        unitCol.setPrefWidth(70);

        TableColumn<Product, String> statusCol = new TableColumn<>("Status");
        statusCol.setCellValueFactory(d -> new SimpleStringProperty(inventory.statusOf(d.getValue())));
        statusCol.setPrefWidth(100);
        statusCol.setCellFactory(col -> new TableCell<Product, String>() {
            @Override
            protected void updateItem(String status, boolean empty) {
                super.updateItem(status, empty);
                if (empty || status == null) {
                    setGraphic(null);
                    setText(null);
                } else {
                    String color;
                    if (Inventory.STATUS_OK.equals(status)) {
                        color = "#2e7d32";   // green
                    } else if (Inventory.STATUS_LOW.equals(status)) {
                        color = "#f9a825";   // amber
                    } else {
                        color = "#c62828";   // red
                    }
                    Label badge = new Label(status);
                    badge.setStyle("-fx-background-color: " + color + "; -fx-text-fill: white; "
                            + "-fx-padding: 3 10 3 10; -fx-background-radius: 12; "
                            + "-fx-font-size: 11px; -fx-font-weight: bold;");
                    setGraphic(badge);
                    setText(null);
                    setAlignment(Pos.CENTER);
                }
            }
        });

        TableColumn<Product, String> unitValueCol = new TableColumn<>("Unit Value (PHP)");
        unitValueCol.setCellValueFactory(d -> new SimpleStringProperty(
                String.format("%,.2f", d.getValue().getUnitValue())));
        unitValueCol.setPrefWidth(120);
        unitValueCol.setStyle("-fx-alignment: CENTER-RIGHT;");

        TableColumn<Product, String> totalCol = new TableColumn<>("Total Value (PHP)");
        totalCol.setCellValueFactory(d -> new SimpleStringProperty(
                String.format("%,.2f",
                        inventory.stockOf(d.getValue().getId()) * d.getValue().getUnitValue())));
        totalCol.setPrefWidth(130);
        totalCol.setStyle("-fx-alignment: CENTER-RIGHT;");

        table.getColumns().addAll(nameCol, catCol, stockCol, unitCol, statusCol, unitValueCol, totalCol);
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
    }
    
    // Data
    public void refresh() {
        ObservableList<Product> items = FXCollections.observableArrayList();
        int total = 0, low = 0, out = 0;
        double totalValue = 0;

        String q = searchField.getText() == null ? "" : searchField.getText().trim().toLowerCase();
        String cat = categoryFilter.getValue();
        String st = statusFilter.getValue();

        for (Product p : inventory.getProducts()) {
            if (!p.isActive()) {
                continue;   // archived products live in the Products screen only
            }
            total++;
            String status = inventory.statusOf(p);
            if (status.equals(Inventory.STATUS_LOW)) {
                low++;
            }
            if (status.equals(Inventory.STATUS_OUT)) {
                out++;
            }
            totalValue += inventory.stockOf(p.getId()) * p.getUnitValue();

            boolean show = true;
            if (!q.isEmpty() && !p.getName().toLowerCase().contains(q)
                    && !p.getDescription().toLowerCase().contains(q)) {
                show = false;
            }
            if (cat != null && !cat.equals("All Categories") && !p.getCategory().equals(cat)) {
                show = false;
            }
            if (st != null && !st.equals("All Statuses") && !status.equals(st)) {
                show = false;
            }
            if (show) {
                items.add(p);
            }
        }

        table.setItems(items);
        totalLabel.setText(String.valueOf(total));
        lowLabel.setText(String.valueOf(low));
        outLabel.setText(String.valueOf(out));
        valueLabel.setText(String.format("%,.2f", totalValue));
    }

    // Restock / Adjust / Movement log
    private void openRestockDialog() {
        Product p = table.getSelectionModel().getSelectedItem();
        if (p == null) {
            return;
        }

        Dialog<Integer> dialog = new Dialog<>();
        dialog.setTitle("Restock");
        dialog.setHeaderText("Restock \"" + p.getName() + "\"\nCurrent stock: "
                + inventory.stockOf(p.getId()) + " " + p.getUnit());

        TextField qtyField = new TextField();
        qtyField.setPromptText("e.g. 25");

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(6));
        grid.add(new Label("Quantity to add (" + p.getUnit() + "):*"), 0, 0);
        grid.add(qtyField, 1, 0);
        dialog.getDialogPane().setContent(grid);

        ButtonType addType = new ButtonType("Add Stock", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(addType, ButtonType.CANCEL);

        Button addBtn = (Button) dialog.getDialogPane().lookupButton(addType);
        addBtn.addEventFilter(ActionEvent.ACTION, e -> {
            try {
                if (Integer.parseInt(qtyField.getText().trim()) <= 0) {
                    throw new NumberFormatException();
                }
            } catch (NumberFormatException ex) {
                e.consume();
                showError("Quantity must be a whole number greater than 0.");
            }
        });

        dialog.setResultConverter(bt -> bt == addType
                ? Integer.valueOf(qtyField.getText().trim())
                : null);

        dialog.showAndWait().ifPresent(qty -> {
            try {
                inventory.restock(p, qty, "manager");
                refresh();
            } catch (IllegalArgumentException ex) {
                showError(ex.getMessage());
            }
        });
    }

    private void openAdjustDialog() {
        Product p = table.getSelectionModel().getSelectedItem();
        if (p == null) {
            return;
        }

        Dialog<Integer> dialog = new Dialog<>();
        dialog.setTitle("Adjust Stock");
        dialog.setHeaderText("Physical count for \"" + p.getName() + "\"\n"
                + "System stock: " + inventory.stockOf(p.getId()) + " " + p.getUnit()
                + "\nEnter the quantity you actually counted; the difference is recorded as a correction.");

        TextField countField = new TextField();
        countField.setPromptText("e.g. 18");

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(6));
        grid.add(new Label("Counted stock (" + p.getUnit() + "):*"), 0, 0);
        grid.add(countField, 1, 0);
        dialog.getDialogPane().setContent(grid);

        ButtonType okType = new ButtonType("Save Correction", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(okType, ButtonType.CANCEL);

        Button okBtn = (Button) dialog.getDialogPane().lookupButton(okType);
        okBtn.addEventFilter(ActionEvent.ACTION, e -> {
            try {
                if (Integer.parseInt(countField.getText().trim()) < 0) {
                    throw new NumberFormatException();
                }
            } catch (NumberFormatException ex) {
                e.consume();
                showError("Counted stock must be a whole number of 0 or more.");
            }
        });

        dialog.setResultConverter(bt -> bt == okType
                ? Integer.valueOf(countField.getText().trim())
                : null);

        dialog.showAndWait().ifPresent(counted -> {
            try {
                inventory.adjustStock(p, counted, "manager");
                refresh();
            } catch (IllegalArgumentException ex) {
                showError(ex.getMessage());
            }
        });
    }

    private void openMovementLog() {
        Product p = table.getSelectionModel().getSelectedItem();
        if (p == null) {
            return;
        }

        // oldest first, with a running balance — reads like a ledger
        List<StockMovement> moves = inventory.movementsFor(p.getId());
        ObservableList<LogRow> rows = FXCollections.observableArrayList();
        int running = 0;
        for (StockMovement m : moves) {
            running += m.getQtyChange();
            rows.add(new LogRow(m, running));
        }

        TableView<LogRow> logTable = new TableView<>(rows);
        logTable.setPlaceholder(new Label("No stock movements recorded for this product yet."));

        TableColumn<LogRow, String> dateCol = new TableColumn<>("Date & Time");
        dateCol.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getDateTimeText()));
        dateCol.setPrefWidth(150);

        TableColumn<LogRow, String> changeCol = new TableColumn<>("Change");
        changeCol.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getChangeText()));
        changeCol.setPrefWidth(70);
        changeCol.setCellFactory(col -> new TableCell<LogRow, String>() {
            @Override
            protected void updateItem(String v, boolean empty) {
                super.updateItem(v, empty);
                if (empty || v == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(v);
                    setStyle("-fx-alignment: CENTER-RIGHT; -fx-font-weight: bold; -fx-text-fill: "
                            + (v.startsWith("-") ? "#c62828;" : "#2e7d32;"));
                }
            }
        });

        TableColumn<LogRow, Number> balanceCol = new TableColumn<>("Balance");
        balanceCol.setCellValueFactory(d -> new SimpleIntegerProperty(d.getValue().getBalance()));
        balanceCol.setPrefWidth(80);
        balanceCol.setStyle("-fx-alignment: CENTER-RIGHT;");

        TableColumn<LogRow, String> reasonCol = new TableColumn<>("Reason");
        reasonCol.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getReasonLabel()));
        reasonCol.setPrefWidth(130);

        TableColumn<LogRow, String> refCol = new TableColumn<>("Reference");
        refCol.setCellValueFactory(d -> new SimpleStringProperty(
                d.getValue().getRefId() == null || d.getValue().getRefId().isEmpty()
                        ? "-" : d.getValue().getRefId()));
        refCol.setPrefWidth(90);

        TableColumn<LogRow, String> byCol = new TableColumn<>("By");
        byCol.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getByUser()));
        byCol.setPrefWidth(90);

        logTable.getColumns().addAll(dateCol, changeCol, balanceCol, reasonCol, refCol, byCol);
        logTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        logTable.setPrefSize(660, 380);

        Dialog<Void> dialog = new Dialog<>();
        dialog.setTitle("Movement Log - " + p.getName());
        dialog.setHeaderText("Stock history of \"" + p.getName() + "\", oldest first. Current stock: "
                + running + " " + p.getUnit());
        dialog.getDialogPane().setContent(logTable);
        dialog.getDialogPane().getButtonTypes().add(ButtonType.CLOSE);
        dialog.setResizable(true);
        dialog.showAndWait();
    }

    // One row of the movement log: the movement plus the running balance.
    private static class LogRow {
        private final StockMovement m;
        private final int balance;

        LogRow(StockMovement m, int balance) {
            this.m = m;
            this.balance = balance;
        }

        String getDateTimeText() { return m.getDateTimeText(); }
        String getChangeText() { return m.getChangeText(); }
        int getBalance() { return balance; }
        String getReasonLabel() { return m.getReasonLabel(); }
        String getRefId() { return m.getRefId(); }
        String getByUser() { return m.getByUser(); }
    }

    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR, message, ButtonType.OK);
        alert.setHeaderText(null);
        alert.showAndWait();
    }
}