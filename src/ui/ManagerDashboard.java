package ui;

import java.util.List;

import javafx.scene.control.Button;
import javafx.scene.control.ChoiceDialog;
import manager.AddDonation;
import manager.ViewDonations;
import system.DonationSystem;
import users.Clerk;
import users.Manager;

public class ManagerDashboard extends Dashboard {

    public ManagerDashboard(Manager manager) {
        super(manager, null);
    }

    @Override
    protected List<Button> createButtons() {
        return List.of(
            createMenuButton("Add Donation",
                () -> new AddDonation(getScene().getWindow()).showAndWait()),
            createMenuButton("View Donations",
                () -> new ViewDonations(getScene().getWindow()).showAndWait()),
            // TODO (Inventory / Product members): replace placeholders
            createMenuButton("Inventory", () -> showComingSoon("Inventory")),
            createMenuButton("Products", () -> showComingSoon("Product Management")),
            createMenuButton("View Clerk\nDashboard", this::openClerkDashboard)
        );
    }

    // gets the manager pick a clerk and see that clerk's dashboard
    private void openClerkDashboard() {
        List<Clerk> clerks = DonationSystem.getInstance().getClerks();

        if (clerks.isEmpty()) {
            showInfo("There are no clerk accounts yet.");
            return;
        }

        ChoiceDialog<Clerk> dialog = new ChoiceDialog<>(clerks.get(0), clerks);
        dialog.initOwner(getScene().getWindow());
        dialog.setTitle("Clerk Dashboards");
        dialog.setHeaderText("Select a clerk");
        dialog.setContentText("Clerk:");

        dialog.showAndWait().ifPresent(clerk ->
            SceneManager.show(new ClerkDashboard(clerk, () -> SceneManager.show(this)))
        );
    }
}