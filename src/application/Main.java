package application;

import inventory.Inventory;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.stage.Stage;
import ui.InventoryScreen;
import ui.ProductScreen;

// Main window of the Donation Tracker for LGUs and nonprofits.
//
// Owns the shared Inventory object and lays out the modules as tabs:
//   Products | Inventory   -> Milestone 2, working now
//   Donations | History    -> placeholders, filled in steps 2-3
//   Donors                 -> placeholder, filled in step 4
//
// Sign-in is a simple role picker for now; step 4 replaces it with the
// real users.txt login (username + password + stored role).
public class Main extends Application {

    private Inventory inventory;
    private Stage mainStage;

    @Override
    public void start(Stage stage) {
        this.mainStage = stage;
        this.inventory = new Inventory();   // loads products.txt + movements.txt, seeds on first run
        showLogin();
    }

    // Sign in

    private void showLogin() {
        Boolean isManager = chooseRole();
        if (isManager == null) {
            Platform.exit();   // sign-in window was closed
            return;
        }
        buildMainWindow(isManager);
    }

    // LOGIN SCREEN
    // checking username + password against users.txt.
    private Boolean chooseRole() {
        Dialog<Boolean> dialog = new Dialog<>();
        dialog.setTitle("Sign in");
        dialog.setHeaderText("LGU Donation Tracker\n");
        
        if (mainStage.getScene() != null) {
            dialog.initOwner(mainStage);
        }

        ButtonType managerBtn = new ButtonType("Sign in as Manager", ButtonBar.ButtonData.APPLY);
        ButtonType clerkBtn = new ButtonType("Sign in as Clerk", ButtonBar.ButtonData.APPLY);
        dialog.getDialogPane().getButtonTypes().addAll(managerBtn, clerkBtn, ButtonType.CANCEL);

        dialog.setResultConverter(bt -> {
            if (bt == managerBtn) {
                return Boolean.TRUE;
            }
            if (bt == clerkBtn) {
                return Boolean.FALSE;
            }
            return null;
        });

        return dialog.showAndWait().orElse(null);
    }

    // Main window

    private void buildMainWindow(boolean isManager) {
        // The role flag drives what the screens show
        // manager  - can add/edit/archive products, restock, adjust stock
        // clerk    - read-only catalog + inventory, can open movement logs
        ProductScreen productScreen = new ProductScreen(inventory, isManager);
        InventoryScreen inventoryScreen = new InventoryScreen(inventory, isManager);

        Tab productsTab = new Tab("Products", productScreen.getView());
        Tab inventoryTab = new Tab("Inventory", inventoryScreen.getView());

        // Roadmap placeholders - enabled in steps 2-4.
        Tab donationsTab = placeholderTab("Donations", "Step 2: record cash and item donations");
        Tab historyTab = placeholderTab("History", "Step 3: transaction history, releases and void");
        Tab donorsTab = placeholderTab("Donors", "Step 4: donor directory and user accounts");

        TabPane tabs = new TabPane(productsTab, inventoryTab, donationsTab, historyTab, donorsTab);
        tabs.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);

        // Refresh a screen whenever you switch to its tab, so changes made
        // in one tab show up immediately in the other.
        tabs.getSelectionModel().selectedItemProperty().addListener((obs, oldTab, newTab) -> {
            if (newTab == inventoryTab) {
                inventoryScreen.refresh();
            }
            if (newTab == productsTab) {
                productScreen.refresh();
            }
        });

        // top bar: title + who is signed in
        Label title = new Label("LGU Donation Tracker");
        title.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #22303f;");

        Label roleLabel = new Label(isManager ? "Signed in: Manager" : "Signed in: Clerk");
        roleLabel.setStyle("-fx-text-fill: #5a6b7b;");

        Button signOutBtn = new Button("Sign out");
        signOutBtn.setOnAction(e -> showLogin());

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        HBox header = new HBox(16, title, spacer, roleLabel, signOutBtn);
        header.setAlignment(Pos.CENTER_LEFT);
        header.setPadding(new Insets(12, 16, 12, 16));
        header.setStyle("-fx-background-color: white; "
                + "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.08), 4, 0.1, 0, 2);");

        BorderPane root = new BorderPane();
        root.setTop(header);
        root.setCenter(tabs);

        Scene scene = new Scene(root, 1060, 680);
        mainStage.setTitle("LGU Donation Tracker - " + (isManager ? "Manager" : "Clerk"));
        mainStage.setScene(scene);
        mainStage.show();
    }

    private Tab placeholderTab(String title, String hint) {
        Tab tab = new Tab(title);
        tab.setDisable(true);
        tab.setTooltip(new Tooltip(hint));
        return tab;
    }

    public static void main(String[] args) {
        launch(args);
    }
}