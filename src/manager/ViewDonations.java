package manager;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;

import javafx.collections.FXCollections;
import javafx.scene.Scene;
import javafx.geometry.Insets;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.Window;

public class ViewDonations extends Stage {

    public ViewDonations(Window owner) {

        setTitle("Donation History");

        initOwner(owner);
        initModality(Modality.APPLICATION_MODAL);

        TableView<Donation> table = new TableView<>();
        table.setPlaceholder(new Label("No donations recorded yet."));

        // Add columns to table
        table.getColumns().addAll(
            column("Date", "date", 150),
            column("Donor", "donor", 160),
            column("Phone", "phone", 120),
            column("Email", "email", 170),
            column("Donation Name", "name", 150),
            column("Type", "type", 90),
            column("Quantity", "quantity", 90),
            column("Unit", "unit", 110),
            column("Recorded By", "recordedBy", 110)
        );

        // Footer: cash donations are counted here, separate from product stock value
        Label moneyTotal = new Label("Total monetary donations: PHP 0.00");
        moneyTotal.setStyle("-fx-font-weight: bold; -fx-text-fill: #6a1b9a;");
        moneyTotal.setPadding(new Insets(10, 12, 10, 12));

        // Load saved donations (newest first)
        try {

            ArrayList<Donation> donations = DonationFile.loadDonations();
            Collections.reverse(donations);
            table.setItems(FXCollections.observableArrayList(donations));

            double money = 0;
            for (Donation d : donations) {
                if (d.isMonetary()) {
                    money += d.getAmount();
                }
            }
            moneyTotal.setText(String.format("Total monetary donations: PHP %,.2f", money));

        } catch (IOException ex) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setContentText("Unable to load donations: " + ex.getMessage());
            alert.showAndWait();
        }

        // Layout of whole thing
        VBox layout = new VBox(table, moneyTotal);
        VBox.setVgrow(table, Priority.ALWAYS);
        Scene scene = new Scene(layout, 1180, 450);
        setScene(scene);
    }

    // Builds one text column that reads a Donation property by name.
    private static TableColumn<Donation, String> column(String title, String property, double width) {
        TableColumn<Donation, String> col = new TableColumn<>(title);
        col.setCellValueFactory(new PropertyValueFactory<>(property));
        col.setPrefWidth(width);
        return col;
    }
}