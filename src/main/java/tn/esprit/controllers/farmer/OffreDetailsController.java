package tn.esprit.controllers.farmer;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.layout.StackPane;
import tn.esprit.entities.OffreEmploi;

import java.io.IOException;

public class OffreDetailsController {

    @FXML private Label titreLabel;
    @FXML private Label lieuLabel;
    @FXML private Label periodeLabel;
    @FXML private TextArea descriptionText;

    private OffreEmploi currentOffre;
    private StackPane contentPaneRef;

    // Called by MesOffresController to set data and container
    public void setOffre(OffreEmploi offre, StackPane contentPane) {
        this.currentOffre = offre;
        this.contentPaneRef = contentPane;

        // Fill UI with offer details
        titreLabel.setText("🧑‍🌾 " + offre.getTitre());
        lieuLabel.setText("📍 " + offre.getLieu());
        periodeLabel.setText("📅 " + offre.getStartDate() + " - " + offre.getDateExpiration());
        descriptionText.setText(offre.getDescription());
    }

    @FXML
    private void goBack() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/front/farmer_front/MesOffresView.fxml"));
            Parent view = loader.load();

            // Pass back the pane so MesOffres can still control transitions
            MesOffresController.setContentPaneRef(contentPaneRef);
            contentPaneRef.getChildren().setAll(view);
        } catch (IOException e) {
            System.err.println("❌ Failed to go back to MesOffresView.fxml");
            e.printStackTrace();
        }
    }

    @FXML
    private void goToCandidatures() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/front/farmer_front/CandidaturesView.fxml"));
            Parent view = loader.load();

            CandidaturesController ctrl = loader.getController();
            ctrl.setOffre(currentOffre, contentPaneRef); // Set selected offer and parent pane

            contentPaneRef.getChildren().setAll(view);
        } catch (IOException e) {
            System.err.println("❌ Failed to load CandidaturesView.fxml");
            e.printStackTrace();
        }
    }
}
