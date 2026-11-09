
package manager;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.Window;

public class AddDonation extends Stage {

    public AddDonation(Window owner) {

        // Popup window settings
        setTitle("Add Donation");
        initOwner(owner);
        initModality(Modality.APPLICATION_MODAL);

        // Donation name
        Label nameLabel = new Label("Donation Name:");
        TextField nameField = new TextField();
        nameField.setPromptText("e.g. Rice, Okra, Money");

        // Donation type
        Label typeLabel = new Label("Donation Type:");

        ComboBox<String> typeBox = new ComboBox<>();
        typeBox.getItems().addAll(
            "Food",
            "Money",
            "Other"
        );
        typeBox.setPromptText("Select donation type");

        // Quantity
        Label quantityLabel = new Label("Quantity:");

        TextField quantityField = new TextField();
        quantityField.setPromptText("Enter quantity");

        // Unit
        Label unitLabel = new Label("Unit:");

        ComboBox<String> unitBox = new ComboBox<>();
        unitBox.getItems().addAll(
            "Kilograms (kg)",
            "Pieces",
            "Liters (L)",
            "Packs",
            "Sacks",
            "Boxes",
            "PHP",
            "Other"
        );
        unitBox.setPromptText("Select unit");

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

        form.add(nameLabel, 0, 0);
        form.add(nameField, 1, 0);

        form.add(typeLabel, 0, 1);
        form.add(typeBox, 1, 1);

        form.add(quantityLabel, 0, 2);
        form.add(quantityField, 1, 2);

        form.add(unitLabel, 0, 3);
        form.add(unitBox, 1, 3);

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

    String name = nameField.getText().trim();
    String type = typeBox.getValue();
    String quantity = quantityField.getText().trim();
    String unit = unitBox.getValue();

    if (name.isEmpty() || type == null ||
        quantity.isEmpty() || unit == null) {

        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setContentText("Please complete all fields.");
        alert.showAndWait();

    } else if (name.contains("|") || quantity.contains("|") || name.contains("\n")) {

        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setContentText("Please do not use the | character.");
        alert.showAndWait();

    } else {

        Donation donation = new Donation(name, type, quantity, unit);

        try {

            DonationFile.saveDonation(donation);

            Alert alert = new Alert(Alert.AlertType.INFORMATION);

            alert.setContentText("Donation saved successfully!");
            alert.showAndWait();
            close();

        } catch (java.io.IOException ex) {

            Alert alert = new Alert(
                Alert.AlertType.ERROR
            );

            alert.setContentText(
                "Unable to save donation: " + ex.getMessage()
            );

            alert.showAndWait();
        }
    }
});


        // Cancel button
        cancelButton.setOnAction(e -> close());

        // Display popup
        Scene scene = new Scene(layout, 450, 350);

        setScene(scene);
    }
}
