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

        Label subtitle = new Label("Log in with your staff account.");
        subtitle.getStyleClass().add("login-subtitle");

        TextField usernameField = new TextField();
        VBox usernameGroup = field("Username", usernameField);

        PasswordField passwordField = new PasswordField();
        VBox passwordGroup = field("Password", passwordField);

        Label errorLabel = new Label();
        errorLabel.getStyleClass().add("error-label");
        errorLabel.setWrapText(true);
        errorLabel.setVisible(false);
        errorLabel.setManaged(false);

        Button loginButton = new Button("Log in");
        loginButton.getStyleClass().addAll("primary", "login-button");
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

        VBox header = new VBox(4, title, subtitle);

        VBox card = new VBox(20, header, usernameGroup, passwordGroup, errorLabel, loginButton);
        card.getStyleClass().add("login-card");
        card.setMaxWidth(380);
        card.setMaxHeight(Region.USE_PREF_SIZE);

        getChildren().add(card); // StackPane centers the card

        Platform.runLater(usernameField::requestFocus);
    }

    // A small label above its input, so the field is named even after typing.
    private static VBox field(String labelText, Region input) {
        Label label = new Label(labelText);
        label.getStyleClass().add("field-label");
        return new VBox(6, label, input);
    }

    private static void showError(Label label, String message) {
        label.setText(message);
        label.setVisible(true);
        label.setManaged(true);
    }
}