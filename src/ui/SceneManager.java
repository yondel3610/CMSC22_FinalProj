package ui;

import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import system.DonationSystem;
import users.Clerk;
import users.Manager;
import users.User;

// Handles screen switching while the program is running:
// login screen -> role dashboard -> module screens -> back / logout.
public final class SceneManager {

    private static Scene scene;

    private SceneManager() {}

    public static void init(Stage stage, String stylesheet) {
        scene = new Scene(new StackPane());
        scene.getStylesheets().add(stylesheet);

        stage.setTitle("Donation Management System");
        stage.setScene(scene);
        stage.setMinWidth(900);
        stage.setMinHeight(650);
        stage.setMaximized(true); // full-screen size, still a normal window
    }

    public static void show(Parent root) {
        scene.setRoot(root);
    }

    public static void showLogin() {
        show(new LoginScreen());
    }

    // Opens the right dashboard for the user's role.
    public static void showDashboard(User user) {
        switch (user.getRole()) {
            case MANAGER -> show(new ManagerDashboard((Manager) user));
            case CLERK -> show(new ClerkDashboard((Clerk) user, null));
        }
    }

    // The green bar at the top of every screen after login: app name on the
    // left, who is signed in and one action button (Log out / Back) on the right.
    public static HBox createAppBar(String actionText, Runnable action) {
        Label title = new Label("Donation Management System");
        title.getStyleClass().add("app-title");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        HBox bar = new HBox(16, title, spacer);
        bar.getStyleClass().add("app-bar");
        bar.setAlignment(Pos.CENTER_LEFT);

        User current = DonationSystem.getInstance().getCurrentUser();
        if (current != null) {
            Label who = new Label(current.getFullName()
                    + " (" + current.getRole().getDisplayName() + ")");
            who.getStyleClass().add("app-user");
            bar.getChildren().add(who);
        }

        Button button = new Button(actionText);
        button.getStyleClass().add("app-bar-button");
        button.setOnAction(e -> action.run());
        bar.getChildren().add(button);

        return bar;
    }

    // Shows a module screen (Products / Inventory / Donors) under the app bar,
    // whose Back button returns to the current user's dashboard.
    public static void showModule(Region content) {
        BorderPane root = new BorderPane();
        root.getStyleClass().add("screen");

        root.setTop(createAppBar("Back to dashboard", () -> {
            User current = DonationSystem.getInstance().getCurrentUser();
            if (current != null) {
                showDashboard(current);
            } else {
                showLogin();
            }
        }));
        root.setCenter(content);
        show(root);
    }

    public static void logout() {
        DonationSystem.getInstance().logout();
        showLogin();
    }
}