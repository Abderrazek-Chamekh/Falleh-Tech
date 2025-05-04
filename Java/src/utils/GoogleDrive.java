package googledrive.utils;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class GoogleDrive extends Application {

    @Override
    public void start(Stage stage) throws Exception {
        // ✅ Use Parent to avoid ClassCastException
        Parent root = FXMLLoader.load(getClass().getResource("/fxml/landing.fxml"));

        Scene scene = new Scene(root);

        scene.getStylesheets().add(getClass().getResource("/styles/main.css").toExternalForm());
        scene.getStylesheets().add(getClass().getResource("/styles/dashboard.css").toExternalForm());
        scene.getStylesheets().add(getClass().getResource("/styles/offre.css").toExternalForm());

        scene.getStylesheets().add(getClass().getResource("/styles/popup.css").toExternalForm());

        scene.getStylesheets().add(getClass().getResource("/styles/sidebar.css").toExternalForm());
        stage.setTitle("FallehTech Platform");
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        launch();
    }
}
