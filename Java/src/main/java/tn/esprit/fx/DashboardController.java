package tn.esprit.fx;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;

public class DashboardController {

    @FXML private Button homeBtn;
    @FXML private Button offreBtn;
    @FXML private BorderPane rootBorderPane;
    @FXML private Label welcomeLabel;

    @FXML
    public void initialize() {
        offreBtn.setOnAction(event -> loadOffreView());
        homeBtn.setOnAction(event -> rootBorderPane.setCenter(welcomeLabel)); // Return to welcome label
    }

    private void loadOffreView() {
        try {
            Parent offreContent = FXMLLoader.load(getClass().getResource("/views/offre_view.fxml"));
            rootBorderPane.setCenter(offreContent);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
