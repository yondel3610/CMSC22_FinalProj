package ui;

import inventory.Inventory;
import inventory.Product;
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
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;

// The Product Catalog screen
// Manager - can add, edit, archive and restore products
// Clerk - read-only (the buttons are not shown)
// just ask claude or glm, i can't be bothered to document this  
public class ProductScreen {

    private final Inventory inventory;
    private final boolean isManager;

    private final BorderPane view = new BorderPane();
    private final TableView<Product> table = new TableView<>();
    private final TextField searchField = new TextField();

    private Button editBtn;
    private Button archiveBtn;

    public ProductScreen(Inventory inventory, boolean isManager) {
        this.inventory = inventory;
        this.isManager = isManager;
        buildView();
        refresh();
    }

    public Region getView() {
        return view;
    }

    // Layout
    private void buildView() {
        view.setStyle("-fx-background-color: #f4f6f8;");

        Label title = new Label("Product Catalog");
        title.setStyle("-fx-font-size: 20px; -fx-font-weight: bold; -fx-text-fill: #22303f;");

        searchField.setPromptText("Search product name or description...");
        searchField.setPrefWidth(320);
        searchField.textProperty().addListener((obs, oldVal, newVal) -> refresh());

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        HBox top = new HBox(16, title, spacer, searchField);
        top.setAlignment(Pos.CENTER_LEFT);
        top.setPadding(new Insets(16, 16, 12, 16));

        buildTable();
        BorderPane.setMargin(table, new Insets(0, 16, 0, 16));
        view.setTop(top);
        view.setCenter(table);

        if (isManager) {
            Button addBtn = new Button("Add Product");
            addBtn.setStyle("-fx-background-color: #2e7d32; -fx-text-fill: white;");
            addBtn.setOnAction(e -> openAddDialog());

            editBtn = new Button("Edit");
            editBtn.setOnAction(e -> openEditDialog());

            archiveBtn = new Button("Archive");
            archiveBtn.setOnAction(e -> toggleArchive());

            table.getSelectionModel().selectedItemProperty()
                    .addListener((obs, oldVal, sel) -> updateButtons());

            HBox buttons = new HBox(10, addBtn, editBtn, archiveBtn);
            buttons.setPadding(new Insets(12, 16, 16, 16));
            view.setBottom(buttons);
        } else {
            Label note = new Label("Read-only: only managers can change the product catalog.");
            note.setStyle("-fx-font-size: 11px; -fx-text-fill: #777777;");
            HBox bottom = new HBox(note);
            bottom.setPadding(new Insets(12, 16, 16, 16));
            view.setBottom(bottom);
        }
    }

    private void buildTable() {
        table.setPlaceholder(new Label("No products yet. Click \"Add Product\" to create your first one."));

        // archived products are shown greyed out instead of deleted
        table.setRowFactory(tv -> new TableRow<Product>() {
            @Override
            protected void updateItem(Product p, boolean empty) {
                super.updateItem(p, empty);
                if (p == null || empty) {
                    setStyle("");
                } else if (!p.isActive()) {
                    setStyle("-fx-opacity: 0.55;");
                } else {
                    setStyle("");
                }
            }
        });

        TableColumn<Product, String> idCol = new TableColumn<>("ID");
        idCol.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getId()));
        idCol.setPrefWidth(70);

        TableColumn<Product, String> nameCol = new TableColumn<>("Product Name");
        nameCol.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getName()));
        nameCol.setPrefWidth(210);

        TableColumn<Product, String> catCol = new TableColumn<>("Category");
        catCol.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getCategory()));
        catCol.setPrefWidth(110);

        TableColumn<Product, String> unitCol = new TableColumn<>("Unit");
        unitCol.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getUnit()));
        unitCol.setPrefWidth(70);

        TableColumn<Product, String> valueCol = new TableColumn<>("Unit Value (PHP)");
        valueCol.setCellValueFactory(d -> new SimpleStringProperty(
                String.format("%,.2f", d.getValue().getUnitValue())));
        valueCol.setPrefWidth(120);
        valueCol.setStyle("-fx-alignment: CENTER-RIGHT;");

        TableColumn<Product, Number> reorderCol = new TableColumn<>("Reorder Level");
        reorderCol.setCellValueFactory(d -> new SimpleIntegerProperty(d.getValue().getReorderLevel()));
        reorderCol.setPrefWidth(100);
        reorderCol.setStyle("-fx-alignment: CENTER-RIGHT;");

        TableColumn<Product, String> statusCol = new TableColumn<>("Status");
        statusCol.setCellValueFactory(d -> new SimpleStringProperty(
                d.getValue().isActive() ? "Active" : "Archived"));
        statusCol.setPrefWidth(90);

        TableColumn<Product, String> descCol = new TableColumn<>("Description");
        descCol.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getDescription()));
        descCol.setPrefWidth(250);

        table.getColumns().addAll(idCol, nameCol, catCol, unitCol, valueCol, reorderCol, statusCol, descCol);
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
    }
    
    // Data
    public void refresh() {
        String q = searchField.getText() == null ? "" : searchField.getText().trim().toLowerCase();
        ObservableList<Product> items = FXCollections.observableArrayList();
        for (Product p : inventory.getProducts()) {
            if (q.isEmpty()
                    || p.getName().toLowerCase().contains(q)
                    || p.getDescription().toLowerCase().contains(q)) {
                items.add(p);
            }
        }
        table.setItems(items);
        if (isManager) {
            updateButtons();
        }
    }

    private void updateButtons() {
        Product p = table.getSelectionModel().getSelectedItem();
        editBtn.setDisable(p == null);
        archiveBtn.setDisable(p == null);
        archiveBtn.setText(p != null && !p.isActive() ? "Restore" : "Archive");
    }

    // Add / edit / archive
    private void openAddDialog() {
        ProductForm form = showProductForm("Add Product", null);
        if (form == null) {
            return;
        }
        try {
            inventory.addProduct(form.name, form.description, form.category, form.unit,
                    form.unitValue, form.reorderLevel, form.initialQty, "manager");
            refresh();
        } catch (IllegalArgumentException ex) {
            showError(ex.getMessage());
        }
    }

    private void openEditDialog() {
        Product p = table.getSelectionModel().getSelectedItem();
        if (p == null) {
            return;
        }
        ProductForm form = showProductForm("Edit Product - " + p.getName(), p);
        if (form == null) {
            return;
        }
        try {
            inventory.updateProduct(p, form.name, form.description, form.category, form.unit,
                    form.unitValue, form.reorderLevel);
            refresh();
        } catch (IllegalArgumentException ex) {
            showError(ex.getMessage());
        }
    }

    private void toggleArchive() {
        Product p = table.getSelectionModel().getSelectedItem();
        if (p == null) {
            return;
        }
        boolean archiving = p.isActive();
        String msg = archiving
                ? "Archive \"" + p.getName() + "\"? It will stay in the list but can no longer receive donations, and its stock is frozen."
                : "Restore \"" + p.getName() + "\" back to the active catalog?";
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION, msg, ButtonType.YES, ButtonType.CANCEL);
        confirm.setHeaderText(null);
        confirm.showAndWait().ifPresent(bt -> {
            if (bt == ButtonType.YES) {
                inventory.setArchived(p, archiving);
                refresh();
            }
        });
    }

    // Shared add/edit form. Returns the entered values, or null if cancelled.
    // All validation happens before the dialog closes.
    private ProductForm showProductForm(String title, Product existing) {
        Dialog<ProductForm> dialog = new Dialog<>();
        dialog.setTitle(title);
        dialog.setHeaderText(existing == null
                ? "Fill in the details of the new product."
                : "Update the details of \"" + existing.getName()
                    + "\". Stock is managed separately (Restock / Adjust), so it is not editable here.");

        TextField nameField = new TextField(existing == null ? "" : existing.getName());
        TextArea descField = new TextArea(existing == null ? "" : existing.getDescription());
        descField.setPrefRowCount(2);

        ComboBox<String> catBox = new ComboBox<>(FXCollections.observableArrayList(Product.CATEGORIES));
        catBox.setValue(existing == null ? "Other" : existing.getCategory());

        ComboBox<String> unitBox = new ComboBox<>(FXCollections.observableArrayList(Product.UNITS));
        unitBox.setValue(existing == null ? "pack" : existing.getUnit());

        TextField valueField = new TextField(existing == null ? "0.00" : String.valueOf(existing.getUnitValue()));
        TextField reorderField = new TextField(existing == null ? "10" : String.valueOf(existing.getReorderLevel()));
        TextField initialField = new TextField("0");

        nameField.setPrefColumnCount(26);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(6));
        int r = 0;
        grid.add(new Label("Product name:*"), 0, r);
        grid.add(nameField, 1, r++);
        grid.add(new Label("Description:"), 0, r);
        grid.add(descField, 1, r++);
        grid.add(new Label("Category:"), 0, r);
        grid.add(catBox, 1, r++);
        grid.add(new Label("Unit:"), 0, r);
        grid.add(unitBox, 1, r++);
        grid.add(new Label("Estimated unit value (PHP):*"), 0, r);
        grid.add(valueField, 1, r++);
        grid.add(new Label("Reorder level:*"), 0, r);
        grid.add(reorderField, 1, r++);
        if (existing == null) {
            grid.add(new Label("Starting stock:"), 0, r);
            grid.add(initialField, 1, r);
        }

        dialog.getDialogPane().setContent(grid);

        ButtonType saveType = new ButtonType("Save", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveType, ButtonType.CANCEL);

        Button saveBtn = (Button) dialog.getDialogPane().lookupButton(saveType);
        saveBtn.addEventFilter(ActionEvent.ACTION, e -> {
            String error = validateForm(nameField, descField, valueField, reorderField,
                    existing == null ? initialField : null, existing);
            if (error != null) {
                e.consume();   // keep the dialog open
                showError(error);
            }
        });

        dialog.setResultConverter(bt -> {
            if (bt == saveType) {
                return readForm(nameField, descField, catBox, unitBox, valueField,
                        reorderField, existing == null ? initialField : null);
            }
            return null;
        });

        return dialog.showAndWait().orElse(null);
    }

    private ProductForm readForm(TextField nameField, TextArea descField, ComboBox<String> catBox,
                                 ComboBox<String> unitBox, TextField valueField,
                                 TextField reorderField, TextField initialField) {
        ProductForm f = new ProductForm();
        f.name = nameField.getText().trim();
        f.description = descField.getText().trim();
        f.category = catBox.getValue();
        f.unit = unitBox.getValue();
        f.unitValue = Double.parseDouble(valueField.getText().trim());
        f.reorderLevel = Integer.parseInt(reorderField.getText().trim());
        f.initialQty = initialField == null ? 0 : Integer.parseInt(initialField.getText().trim());
        return f;
    }

    private String validateForm(TextField nameField, TextArea descField, TextField valueField,
                                TextField reorderField, TextField initialField, Product existing) {
        String name = nameField.getText().trim();
        if (name.isEmpty()) {
            return "Product name is required.";
        }
        if (name.contains("|") || descField.getText().contains("|")) {
            return "The \"|\" character is not allowed in product details.";
        }
        if (inventory.nameExists(name, existing == null ? "" : existing.getId())) {
            return "A product named \"" + name + "\" already exists. Product names must be unique.";
        }
        try {
            if (Double.parseDouble(valueField.getText().trim()) < 0) {
                return "Unit value cannot be negative.";
            }
        } catch (NumberFormatException e) {
            return "Unit value must be a number, e.g. 45.50";
        }
        try {
            if (Integer.parseInt(reorderField.getText().trim()) < 0) {
                return "Reorder level cannot be negative.";
            }
        } catch (NumberFormatException e) {
            return "Reorder level must be a whole number.";
        }
        if (initialField != null) {
            try {
                if (Integer.parseInt(initialField.getText().trim()) < 0) {
                    return "Starting stock cannot be negative.";
                }
            } catch (NumberFormatException e) {
                return "Starting stock must be a whole number.";
            }
        }
        return null;
    }

    private static class ProductForm {
        String name;
        String description;
        String category;
        String unit;
        double unitValue;
        int reorderLevel;
        int initialQty;
    }

    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR, message, ButtonType.OK);
        alert.setHeaderText(null);
        alert.showAndWait();
    }
}