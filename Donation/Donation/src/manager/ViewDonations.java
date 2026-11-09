
package manager;

import java.io.IOException;
import java.util.ArrayList;

import javafx.collections.FXCollections;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.Window;

public class ViewDonations extends Stage {

	public ViewDonations(Window owner) {

        setTitle("View Donations");

        initOwner(owner);
        initModality(Modality.APPLICATION_MODAL);

        TableView<Donation> table = new TableView<>();
        TableColumn<Donation, String> nameColumn = new TableColumn<>("Donation Name");
        nameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
        TableColumn<Donation, String> typeColumn = new TableColumn<>("Donation Type");
        typeColumn.setCellValueFactory(new PropertyValueFactory<>("type"));
        TableColumn<Donation, String> quantityColumn = new TableColumn<>("Quantity");
        quantityColumn.setCellValueFactory(new PropertyValueFactory<>("quantity"));
        TableColumn<Donation, String> unitColumn = new TableColumn<>("Unit");

        unitColumn.setCellValueFactory(new PropertyValueFactory<>("unit"));

        // Add columns to table
        table.getColumns().addAll(
            nameColumn,
            typeColumn,
            quantityColumn,
            unitColumn
        );
        
//        table.setColumnResizePolicy(
//        	    TableView.CONSTRAINED_RESIZE_POLICY
//        	);
        
        nameColumn.setPrefWidth(250);
        typeColumn.setPrefWidth(150);
        quantityColumn.setPrefWidth(150);
        unitColumn.setPrefWidth(150);

        // Load saved donations
        try {

            ArrayList<Donation> donations = DonationFile.loadDonations();
            table.setItems(FXCollections.observableArrayList(donations));

        } catch (IOException ex) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setContentText("Unable to load donations: " + ex.getMessage() );
            alert.showAndWait();
        }

        // Layout of whole thing
        VBox layout = new VBox(table);
        Scene scene = new Scene(layout, 700, 400);
        setScene(scene);
    }
}
