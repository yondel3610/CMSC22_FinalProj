//package application;
//	
//import javafx.application.Application;
//import javafx.stage.Stage;
//import javafx.scene.Scene;
//import javafx.scene.layout.BorderPane;
//
//
//public class Main extends Application {
//	@Override
//	public void start(Stage primaryStage) {
//		try {
//			BorderPane root = new BorderPane();
//			Scene scene = new Scene(root,400,400);
//			scene.getStylesheets().add(getClass().getResource("application.css").toExternalForm());
//			primaryStage.setScene(scene);
//			primaryStage.show();
//		} catch(Exception e) {
//			e.printStackTrace();
//		}
//	}
//	
//	public static void main(String[] args) {
//		launch(args);
//	}
//}



package application;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;

import manager.ManagerDashboard;

public class Main extends Application {

    @Override
    public void start(Stage stage) {

        ManagerDashboard dashboard = new ManagerDashboard();

        Scene scene = new Scene(dashboard, 1000, 700);

        stage.setTitle("Donation Management System");
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
