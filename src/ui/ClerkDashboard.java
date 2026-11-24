package ui;

import java.util.List;

import javafx.scene.control.Button;
import manager.AddDonation;
import manager.ViewDonations;
import system.DonationSystem;
import users.Clerk;

public class ClerkDashboard extends Dashboard {


	// @param backAction null when the clerk is logged in themselves
	//	(shows "Log out"); set when a manager is viewing (shows "Back")
    public ClerkDashboard(Clerk clerk, Runnable backAction) {
        super(clerk, backAction);
    }

    @Override
    protected List<Button> createButtons() {
        return List.of(
            createMenuButton("Record Donation",
                () -> new AddDonation(getScene().getWindow()).showAndWait()),
            createMenuButton("Donation History",
                () -> new ViewDonations(getScene().getWindow()).showAndWait()),
            
            // Read-only: canManageProducts() is false for the clerk, so the
            // catalog and inventory open without the edit/restock buttons
            createMenuButton("Products", this::openProducts),
            createMenuButton("Check Inventory", this::openInventory)
        );
    }

    private void openProducts() {
        SceneManager.showModule(new ProductScreen(
                DonationSystem.getInstance().getInventory(),
                user.canManageProducts()).getView());
    }

    private void openInventory() {
        SceneManager.showModule(new InventoryScreen(
                DonationSystem.getInstance().getInventory(),
                user.canManageProducts()).getView());
    }
}