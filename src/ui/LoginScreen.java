package ui;

import java.util.Optional;
import javafx.application.Platform;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import system.DonationSystem;
import users.User;

public class LoginScreen extends StackPane {

    public LoginScreen() {
        getStyleClass().add("login-root");

        Label title = new Label("Donation Management System");
        title.getStyleClass().add("login-title");

        TextField usernameField = new TextField();
        usernameField.setPromptText("Username");

        PasswordField passwordField = new PasswordField();
        passwordField.setPromptText("Password");

        Label errorLabel = new Label();
        errorLabel.getStyleClass().add("error-label");
        errorLabel.setVisible(false);
        errorLabel.setManaged(false);

        Button loginButton = new Button("Log in");
        loginButton.getStyleClass().add("login-button");
        loginButton.setMaxWidth(Double.MAX_VALUE);
        loginButton.setDefaultButton(true); // Enter key triggers login

        loginButton.setOnAction(e -> {
            String username = usernameField.getText().trim();
            String password = passwordField.getText();

            if (username.isEmpty() || password.isEmpty()) {
                showError(errorLabel, "Please enter your username and password.");
                return;
            }

            Optional<User> user = DonationSystem.getInstance().login(username, password);

            if (user.isPresent()) {
                SceneManager.showDashboard(user.get());
            } else {
                showError(errorLabel, "Invalid username or password.");
                passwordField.clear();
                passwordField.requestFocus();
            }
        });

        VBox card = new VBox(15, title, usernameField,
                passwordField, errorLabel, loginButton);
        card.getStyleClass().add("login-card");
        card.setMaxWidth(420);
        card.setMaxHeight(Region.USE_PREF_SIZE);

        getChildren().add(card); // StackPane centers the card

        Platform.runLater(usernameField::requestFocus);
    }

    private static void showError(Label label, String message) {
        label.setText(message);
        label.setVisible(true);
        label.setManaged(true);
    }
}