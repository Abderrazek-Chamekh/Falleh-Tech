package tn.esprit.fx;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class DashboardApp extends Application {

    @Override
    public void start(Stage stage) throws Exception {

        FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/main_layout.fxml"));
        Parent root = loader.load();

        Scene scene = new Scene(root);
        scene.getStylesheets().add(getClass().getResource("/styles/main.css").toExternalForm());
        scene.getStylesheets().add(getClass().getResource("/styles/dashboard.css").toExternalForm());
        scene.getStylesheets().add(getClass().getResource("/styles/offre.css").toExternalForm());
        scene.getStylesheets().add(getClass().getResource("/styles/sidebar.css").toExternalForm());
        stage.setTitle("Dashboard");
        stage.setScene(scene);
        stage.show();
    }

   /* @Override
    public void start(Stage stage) {
        try {
            TabPane tabPane = new TabPane();

            // === Offre Tab ===
            FXMLLoader offreLoader = new FXMLLoader(getClass().getResource("/views/offre_view.fxml"));
            Parent offreRoot = offreLoader.load();
            Tab offreTab = new Tab("Offres de Travail", offreRoot);
            offreTab.setClosable(false);

            // === Candidature Tab (keep using CandidaturePane for now, or convert to FXML later)
            Tab candidatureTab = new Tab("Candidatures", new CandidaturePane());
            candidatureTab.setClosable(false);

            tabPane.getTabs().addAll(offreTab, candidatureTab);

            Scene scene = new Scene(tabPane, 900, 600);
            stage.setTitle("Tableau de Bord - JavaFX");
            stage.setScene(scene);
            stage.show();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }*/

    public static void main(String[] args) {
        launch();
    }
}
