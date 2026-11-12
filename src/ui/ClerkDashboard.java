package ui;

import java.util.List;

import javafx.scene.control.Button;
import manager.AddDonation;
import manager.ViewDonations;
import users.Clerk;

public class ClerkDashboard extends Dashboard {

    /**
     * @param backAction null when the clerk is logged in themselves (shows
     *                   "Log out"); set when a manager is viewing (shows "Back")
     */
    public ClerkDashboard(Clerk clerk, Runnable backAction) {
        super(clerk, backAction);
    }

    @Override
    protected List<Button> createButtons() {
        return List.of(
            createMenuButton("Record Donation",
                () -> new AddDonation(getScene().getWindow()).showAndWait()),
            createMenuButton("View Donations",
                () -> new ViewDonations(getScene().getWindow()).showAndWait()),
            // TODO (Inventory member): replace this placeholder
            createMenuButton("Check Inventory", () -> showComingSoon("Inventory"))
        );
    }
}