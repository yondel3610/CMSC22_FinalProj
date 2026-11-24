package manager;

import java.util.Locale;

import inventory.Inventory;
import inventory.Product;
import inventory.StockMovement;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.Window;
import javafx.util.StringConverter;
import system.DonationSystem;
import users.User;

public class AddDonation extends Stage {

    private static final String TYPE_ITEM = "Item";
    private static final String TYPE_MONEY = "Money";

    public AddDonation(Window owner) {

        // The shared catalog + stock, so item donations can add to stock
        Inventory inventory = DonationSystem.getInstance().getInventory();

        // Popup window settings
        setTitle("Add Donation");
        initOwner(owner);
        initModality(Modality.APPLICATION_MODAL);

        // Donor
        Label donorLabel = new Label("Donor Name:");
        TextField donorField = new TextField();
        donorField.setPromptText("e.g. Juan Dela Cruz");

        CheckBox anonymousBox = new CheckBox("Anonymous donor");
        // when anonymous is ticked, the name field is locked and cleared
        donorField.disableProperty().bind(anonymousBox.selectedProperty());
        anonymousBox.selectedProperty().addListener((obs, was, isNow) -> {
            if (isNow) {
                donorField.clear();
            }
        });

        // Donor contact (at least one of phone or email is required)
        Label phoneLabel = new Label("Phone Number:");
        TextField phoneField = new TextField();
        phoneField.setPromptText("e.g. 0917 123 4567");

        Label emailLabel = new Label("Email:");
        TextField emailField = new TextField();
        emailField.setPromptText("e.g. juan@email.com");

        Label contactHint = new Label("Provide at least one: phone number or email.");
        contactHint.setStyle("-fx-text-fill: #5a6b7b; -fx-font-size: 11px;");

        // an anonymous donor has no contact info, so lock and clear those too
        phoneField.disableProperty().bind(anonymousBox.selectedProperty());
        emailField.disableProperty().bind(anonymousBox.selectedProperty());
        contactHint.visibleProperty().bind(anonymousBox.selectedProperty().not());
        anonymousBox.selectedProperty().addListener((obs, was, isNow) -> {
            if (isNow) {
                phoneField.clear();
                emailField.clear();
            }
        });

        // Donation type: an item (adds to stock) or money (counted separately)
        Label typeLabel = new Label("Donation Type:");

        ComboBox<String> typeBox = new ComboBox<>();
        typeBox.getItems().addAll(TYPE_ITEM, TYPE_MONEY);
        typeBox.setPromptText("Select donation type");

        // Product (item donations only): must be a product in the catalog,
        // so the donation can add to that product's stock
        Label productLabel = new Label("Product:");
        ComboBox<Product> productBox = new ComboBox<>();
        for (Product p : inventory.getProducts()) {
            if (p.isActive()) {
                productBox.getItems().add(p);
            }
        }
        productBox.setConverter(new StringConverter<Product>() {
            @Override
            public String toString(Product p) {
                return p == null ? "" : p.getName() + " (" + p.getUnit() + ")";
            }

            @Override
            public Product fromString(String s) {
                return null;
            }
        });
        productBox.setPromptText(productBox.getItems().isEmpty()
                ? "No products yet - ask the manager to add one"
                : "Select product");

        // Quantity (items) or amount (money)
        Label quantityLabel = new Label("Quantity:");
        TextField quantityField = new TextField();
        quantityField.setPromptText("Enter quantity");

        // Unit is not typed: it comes from the product, or is PHP for money
        Label unitLabel = new Label("Unit:");
        Label unitValue = new Label(Donation.UNKNOWN);

        // show the right rows for the chosen type
        Runnable updateTypeRows = () -> {
            boolean isMoney = TYPE_MONEY.equals(typeBox.getValue());
            setShown(!isMoney, productLabel, productBox);
            quantityLabel.setText(isMoney ? "Amount (PHP):" : "Quantity:");
            quantityField.setPromptText(isMoney ? "e.g. 500.00" : "Enter quantity");
            if (isMoney) {
                unitValue.setText("PHP");
            } else {
                Product chosen = productBox.getValue();
                unitValue.setText(chosen == null ? Donation.UNKNOWN : chosen.getUnit());
            }
        };
        typeBox.valueProperty().addListener((obs, was, isNow) -> updateTypeRows.run());
        productBox.valueProperty().addListener((obs, was, isNow) -> updateTypeRows.run());

        // Buttons
        Button saveButton = new Button("Save Donation");
        Button cancelButton = new Button("Cancel");

        // Button layout
        GridPane buttonLayout = new GridPane();
        buttonLayout.setHgap(15);
        buttonLayout.add(saveButton, 0, 0);
        buttonLayout.add(cancelButton, 1, 0);

        // Form layout
        GridPane form = new GridPane();

        form.setHgap(15);
        form.setVgap(15);

        form.add(donorLabel, 0, 0);
        form.add(donorField, 1, 0);
        form.add(anonymousBox, 1, 1);

        form.add(phoneLabel, 0, 2);
        form.add(phoneField, 1, 2);

        form.add(emailLabel, 0, 3);
        form.add(emailField, 1, 3);
        form.add(contactHint, 1, 4);

        form.add(typeLabel, 0, 5);
        form.add(typeBox, 1, 5);

        form.add(productLabel, 0, 6);
        form.add(productBox, 1, 6);

        form.add(quantityLabel, 0, 7);
        form.add(quantityField, 1, 7);

        form.add(unitLabel, 0, 8);
        form.add(unitValue, 1, 8);

        // Main layout
        VBox layout = new VBox(20);

        layout.setPadding(new Insets(25));
        layout.setAlignment(Pos.CENTER);

        layout.getChildren().addAll(
            new Label("Donation Information"),
            form,
            buttonLayout
        );

        // Save button
        saveButton.setOnAction(e -> {

            boolean anonymous = anonymousBox.isSelected();
            String donor = donorField.getText().trim();
            String phone = phoneField.getText().trim();
            String email = emailField.getText().trim();
            String type = typeBox.getValue();
            Product product = productBox.getValue();
            String quantity = quantityField.getText().trim();

            boolean isMoney = TYPE_MONEY.equals(type);

            if ((!anonymous && donor.isEmpty()) || type == null || quantity.isEmpty()
                    || (!isMoney && product == null)) {

                showAlert(Alert.AlertType.WARNING,
                        "Please complete all fields (or tick Anonymous donor).");

            } else if (!anonymous && phone.isEmpty() && email.isEmpty()) {

                showAlert(Alert.AlertType.WARNING,
                        "We need at least one way to reach the donor.\n"
                        + "Please enter a phone number or an email (or both).");

            } else if (!anonymous && !phone.isEmpty() && !isValidPhone(phone)) {

                showAlert(Alert.AlertType.WARNING,
                        "That phone number doesn't look right.\n"
                        + "Use 7 to 15 digits, e.g. 09171234567 or +63 917 123 4567.");

            } else if (!anonymous && !email.isEmpty() && !isValidEmail(email)) {

                showAlert(Alert.AlertType.WARNING,
                        "That email doesn't look right. Example: juan@email.com");

            } else if (donor.contains("|") || donor.contains("\n")) {

                showAlert(Alert.AlertType.WARNING, "Please do not use the | character.");

            } else {

                // Quantity: a whole number for items, a peso amount for money
                int itemQty = 0;
                double amount = 0;
                try {
                    if (isMoney) {
                        amount = Double.parseDouble(quantity.replace(",", ""));
                        if (Double.isNaN(amount) || Double.isInfinite(amount)
                                || amount <= 0 || amount > 1_000_000_000) {
                            throw new NumberFormatException();
                        }
                    } else {
                        itemQty = Integer.parseInt(quantity);
                        if (itemQty <= 0) {
                            throw new NumberFormatException();
                        }
                    }
                } catch (NumberFormatException ex) {
                    showAlert(Alert.AlertType.WARNING, isMoney
                            ? "Amount must be a number greater than 0."
                            : "Quantity must be a whole number greater than 0.");
                    return;
                }

                // who is logged in right now gets saved as the one who recorded it
                User current = DonationSystem.getInstance().getCurrentUser();
                String recordedBy = current == null ? Donation.UNKNOWN : current.getUsername();

                String savedName = isMoney ? "Money" : product.getName();
                String savedUnit = isMoney ? "PHP" : product.getUnit();
                String savedQty = isMoney
                        ? String.format(Locale.US, "%.2f", amount)
                        : String.valueOf(itemQty);

                Donation donation = Donation.createNew(
                        anonymous ? Donation.ANONYMOUS : donor,
                        phone, email,
                        savedName, isMoney ? TYPE_MONEY : TYPE_ITEM,
                        savedQty, savedUnit, recordedBy);

                try {
                    // save the donation first; stock is only changed once it is saved
                    DonationFile.saveDonation(donation);

                    String message = "Donation saved successfully!";
                    if (!isMoney) {
                        inventory.recordMovement(product.getId(), itemQty,
                                StockMovement.DONATION, "", recordedBy);
                        message += "\n" + product.getName() + " stock is now "
                                + inventory.stockOf(product.getId()) + " " + product.getUnit() + ".";
                    }
                    showAlert(Alert.AlertType.INFORMATION, message);
                    close();

                } catch (java.io.IOException ex) {
                    showAlert(Alert.AlertType.ERROR,
                            "Unable to save donation: " + ex.getMessage());
                }
            }
        });

        // Cancel button
        cancelButton.setOnAction(e -> close());

        // start with the item rows showing
        updateTypeRows.run();

        // Display popup
        Scene scene = new Scene(layout, 500, 620);

        setScene(scene);
    }

    // shows or hides nodes, and stops hidden ones from taking up space
    private static void setShown(boolean shown, Node... nodes) {
        for (Node n : nodes) {
            n.setVisible(shown);
            n.setManaged(shown);
        }
    }

    // digits only after removing spaces, dashes and brackets; optional leading +
    private static boolean isValidPhone(String phone) {
        String cleaned = phone.replaceAll("[\\s\\-()]", "");
        return cleaned.matches("\\+?\\d{7,15}");
    }

    // simple check: something@something.something, no spaces or |
    private static boolean isValidEmail(String email) {
        return email.matches("[^@\\s|]+@[^@\\s|]+\\.[^@\\s|]+");
    }

    private static void showAlert(Alert.AlertType type, String message) {
        Alert alert = new Alert(type);
        alert.setContentText(message);
        alert.showAndWait();
    }
}