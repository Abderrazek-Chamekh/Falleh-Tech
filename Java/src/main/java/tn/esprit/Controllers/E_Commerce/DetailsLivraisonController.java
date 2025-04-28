package tn.esprit.Controllers.E_Commerce;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.Button;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.scene.Scene;
import javafx.scene.Parent;
import javafx.fxml.FXMLLoader;
import tn.esprit.entities.Livraison;

import java.io.IOException;

public class DetailsLivraisonController {

    @FXML private Label statutLabel;
    @FXML private Label transporteurLabel;
    @FXML private Label numTelTransporteurLabel;
    @FXML private Label dateLivraisonLabel;
    @FXML private Button retourButton;
    private Livraison livraison;

    public void setLivraison(Livraison livraison) {
        this.livraison = livraison;
        afficherInfosLivraison();
    }

    private void afficherInfosLivraison() {
        statutLabel.setText(livraison.getStatut());
        transporteurLabel.setText(livraison.getTransporteur());
        numTelTransporteurLabel.setText(livraison.getNumTelTransporteur());
        dateLivraisonLabel.setText(livraison.getDateLivraison() != null ? livraison.getDateLivraison().toString() : "Non définie");
    }


    @FXML
    private void closeWindow(ActionEvent event) {
        Stage stage = (Stage) retourButton.getScene().getWindow();
        stage.close(); // Close the window when "Retour" button is clicked
    }
}
