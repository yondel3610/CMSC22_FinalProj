package ui;

import java.util.List;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import users.User;

/**
 * Shared layout for every dashboard: welcome header, exit button and a
 * grid of menu buttons. Subclasses only decide which buttons to show.
 */
public abstract class Dashboard extends BorderPane {

    protected final User user;

    /**
     * @param backAction if null, the top-right button is "Log out";
     *                   otherwise it is "Back" and runs this action
     */
    protected Dashboard(User user, Runnable backAction) {
        this.user = user;
        getStyleClass().add("dashboard");

        // Top bar
        Button exitButton = (backAction == null)
                ? createExitButton("Log out", SceneManager::logout)
                : createExitButton("Back", backAction);

        HBox topBar = new HBox(exitButton);
        topBar.setAlignment(Pos.CENTER_RIGHT);
        topBar.setPadding(new Insets(20, 30, 0, 30));
        setTop(topBar);

        // Header
        Label welcome = new Label("Welcome back, " + user.getFullName());
        welcome.getStyleClass().add("welcome-label");

        Label role = new Label(user.getRole().getDisplayName() + " Dashboard");
        role.getStyleClass().add("role-label");

        VBox header = new VBox(5, welcome, role);
        header.setAlignment(Pos.CENTER);

        // Menu buttons (defined by the subclass)
        FlowPane buttonPane = new FlowPane(60, 50);
        buttonPane.setAlignment(Pos.CENTER);
        buttonPane.setMaxWidth(1000);
        buttonPane.getChildren().addAll(createButtons());

        VBox content = new VBox(60, header, buttonPane);
        content.setAlignment(Pos.CENTER);
        setCenter(content);
    }

    /** Each dashboard lists its own menu buttons. */
    protected abstract List<Button> createButtons();

    protected Button createMenuButton(String text, Runnable action) {
        Button button = new Button(text);
        button.getStyleClass().add("dashboard-button");
        button.setPrefSize(335, 140);
        button.setOnAction(e -> action.run());
        return button;
    }

    private Button createExitButton(String text, Runnable action) {
        Button button = new Button(text);
        button.getStyleClass().add("exit-button");
        button.setOnAction(e -> action.run());
        return button;
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