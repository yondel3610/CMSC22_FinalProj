package application;

import javafx.application.Application;
import javafx.stage.Stage;
import system.DonationSystem;
import ui.SceneManager;


public class Main extends Application {

    @Override
    public void start(Stage stage) {
        // The shared product catalog + stock ledger. Loads products.txt and
        // movements.txt once; every screen reads from this same object.
        DonationSystem.getInstance().getInventory();

        String css = getClass().getResource("application.css").toExternalForm();
        SceneManager.init(stage, css);
        SceneManager.showLogin();
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}