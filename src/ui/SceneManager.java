package ui;

import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import system.DonationSystem;
import users.Clerk;
import users.Manager;
import users.User;


// to handle screen switching when the program is in use
// 
public final class SceneManager {
	private static Scene scene;
	
	private SceneManager(){}
	
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

	/** Opens the right dashboard for the user's role. */
	public static void showDashboard(User user) {
		switch (user.getRole()) {
			case MANAGER -> show(new ManagerDashboard((Manager) user));
	        case CLERK -> show(new ClerkDashboard((Clerk) user, null));
		}
	}

	public static void logout() {
		DonationSystem.getInstance().logout();
	    showLogin();
	}
}






















