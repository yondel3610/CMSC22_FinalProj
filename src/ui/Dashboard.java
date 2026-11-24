package ui;

import java.io.IOException;              
import java.util.List;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;     
import javafx.scene.layout.Priority;      
import javafx.scene.layout.VBox;
import manager.DonationFile;              
import system.DonationSystem;              
import users.User;

/**
 * Shared layout for every dashboard: app bar, welcome header and a list of
 * actions. Subclasses only decide which actions to show.
 */
public abstract class Dashboard extends BorderPane {

    protected final User user;
    private final Label productValueLabel = new Label("0.00");
    private final Label monetaryLabel = new Label("0.00");

    /**
     * @param backAction if null, the top-right button is "Log out"; 
     * otherwise it is "Back" and runs this action
     */
    protected Dashboard(User user, Runnable backAction) {
        this.user = user;
        getStyleClass().add("dashboard");

        // Top bar
        setTop(backAction == null
                ? SceneManager.createAppBar("Log out", SceneManager::logout)
                : SceneManager.createAppBar("Back", backAction));

        // Header
        Label welcome = new Label("Welcome back, " + user.getFullName());
        welcome.getStyleClass().add("page-title");

        Label role = new Label(user.getRole().getDisplayName() + " dashboard");
        role.getStyleClass().add("page-sub");

        VBox header = new VBox(4, welcome, role);
        header.setPadding(new Insets(0, 0, 24, 0));

        // Actions (defined by the subclass), one button each
        VBox actions = new VBox(12);                       // 12 = gap between buttons
        actions.getStyleClass().add("action-list");
        actions.getChildren().addAll(createButtons());
        HBox.setHgrow(actions, Priority.ALWAYS);

        // Summary cards, one on top of the other
        VBox stats = new VBox(12,
                createStatCard("Total product value (PHP)", productValueLabel, false),
                createStatCard("Monetary donations received (PHP)", monetaryLabel, true));
        stats.setMinWidth(260);
        stats.setPrefWidth(280);
        stats.setMaxWidth(280);
        stats.setAlignment(Pos.TOP_CENTER);

        HBox body = new HBox(24, actions, stats);          // buttons left, cards right
        body.setAlignment(Pos.TOP_LEFT);

        VBox content = new VBox(header, body);             // was: new VBox(header, actions)
        content.setMaxWidth(900);                          // was: 680
        content.setAlignment(Pos.TOP_LEFT);

        BorderPane.setAlignment(content, Pos.TOP_CENTER);
        BorderPane.setMargin(content, new Insets(48, 24, 24, 24));
        setCenter(content);

        // Re-read the figures every time this dashboard becomes the visible
        // screen (login, Back from a module, Back from the clerk view).
        sceneProperty().addListener((obs, oldScene, newScene) -> {
            if (newScene != null) {
                refreshStats();
            }
        });
        refreshStats();
    }
    

    /** Each dashboard lists its own menu buttons. */
    protected abstract List<Button> createButtons();
    
 // One summary card: a small caption on top, the big number below.
    private VBox createStatCard(String caption, Label value, boolean money) {
        value.getStyleClass().add("stat-value");
        if (money) {
            value.getStyleClass().add("stat-value-money");
        }

        Label captionLabel = new Label(caption);
        captionLabel.getStyleClass().add("stat-caption");
        captionLabel.setWrapText(true);

        VBox card = new VBox(4, captionLabel, value);
        card.getStyleClass().add("stat-card");
        card.setMaxWidth(Double.MAX_VALUE);
        return card;
    }

    // Recomputes both cards from the live data.
    protected void refreshStats() {
        double productValue = DonationSystem.getInstance().getInventory().totalProductValue();
        productValueLabel.setText(String.format("%,.2f", productValue));

        double monetary;
        try {
            monetary = DonationFile.totalMonetary();
        } catch (IOException e) {
            monetary = 0;
        }
        monetaryLabel.setText(String.format("%,.2f", monetary));
    }

    // One action row: a title and a one-line description of what it does.
    protected Button createMenuButton(String text, Runnable action) {
        String title = text.replace("\n", " ");

        Label titleLabel = new Label(title);
        titleLabel.getStyleClass().add("action-title");

        VBox box = new VBox(2, titleLabel);
        box.setAlignment(Pos.CENTER_LEFT);

        String description = describe(title);
        if (description != null) {
            Label descLabel = new Label(description);
            descLabel.getStyleClass().add("action-desc");
            box.getChildren().add(descLabel);
        }

        Button button = new Button();
        button.setGraphic(box);
        button.setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
        button.setAccessibleText(title);
        button.getStyleClass().add("action-row");
        button.setMaxWidth(Double.MAX_VALUE);
        button.setOnAction(e -> {
            action.run();
            // dialogs (Record Donation) block until closed, so the totals
            // are refreshed right after they return
            refreshStats();
        });
        return button;
    }

    // What each action does, in plain words. Wording follows what this role can do.
    private String describe(String title) {
        boolean canEdit = user.canManageProducts();
        return switch (title) {
            case "Record Donation" -> "Log an item or cash donation from a donor.";
            case "Donation History" -> "Every donation, who gave it and who recorded it.";
            case "Products" -> canEdit
                    ? "Add, edit and archive the items you accept."
                    : "Browse the items we accept and what each is worth.";
            case "Inventory" -> "Current stock, restocking and physical counts.";
            case "Check Inventory" -> "Current stock and each product's movement history.";
            case "View Clerk Dashboard" -> "See the dashboard the way a clerk sees it.";
            default -> null;
        };
    }

    protected void showInfo(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.initOwner(getScene().getWindow());
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    protected void showComingSoon(String feature) {
        showInfo(feature + " is coming soon.");
    }
}