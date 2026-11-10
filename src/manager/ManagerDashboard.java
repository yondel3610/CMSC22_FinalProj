
package manager;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;

public class ManagerDashboard extends BorderPane {

    public ManagerDashboard() {

        // Main background
        this.setStyle("-fx-background-color: white;");

        // Welcome home
        Label welcomeLabel = new Label("Welcome back, Admin");
        welcomeLabel.setStyle(
            "-fx-font-size: 64px;" +
            "-fx-font-family: Arial;" +
            "-fx-text-fill: black;"
        );

        VBox header = new VBox(welcomeLabel);
        header.setAlignment(Pos.CENTER);
        header.setPadding(new Insets(70, 0, 0, 0));

        // Buttons
        Button addDonationButton = new Button("Add Donation");
        Button viewDonationsButton = new Button("View Donations");
        Button clerkDashboardButton = new Button("View clerk\ndashboard");
        Button logoutButton = new Button("Log out");

        addDonationButton.setOnAction(e -> {

            AddDonation donationWindow = new AddDonation(
                this.getScene().getWindow()
            );

            donationWindow.showAndWait();

        });
        

        viewDonationsButton.setOnAction(e -> {

        	ViewDonations donationsWindow =
        			new ViewDonations(this.getScene().getWindow());

        	donationsWindow.showAndWait();

        });

        
        // Button styling
        styleButton(addDonationButton);
        styleButton(viewDonationsButton);
        styleButton(clerkDashboardButton);
        styleButton(logoutButton);

        // Button layout
        GridPane buttonGrid = new GridPane();

        buttonGrid.setAlignment(Pos.CENTER);
        buttonGrid.setHgap(150);
        buttonGrid.setVgap(100);

        buttonGrid.add(addDonationButton, 0, 0);
        buttonGrid.add(viewDonationsButton, 1, 0);
        buttonGrid.add(clerkDashboardButton, 0, 1);
        buttonGrid.add(logoutButton, 1, 1);

        // Main layout
        VBox mainLayout = new VBox(80);
        mainLayout.setAlignment(Pos.CENTER);
        mainLayout.getChildren().addAll(header, buttonGrid);

        this.setCenter(mainLayout);
    }

    // Reusable button styling
    private void styleButton(Button button) {

        button.setPrefSize(335, 140);

        button.setStyle(
            "-fx-background-color: #C9D2DC;" +
            "-fx-text-fill: black;" +
            "-fx-font-size: 25px;" +
            "-fx-font-family: Arial;" +
            "-fx-background-radius: 0;" +
            "-fx-cursor: hand;"
        );
    }
    
    
}
