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
import tn.esprit.entities.Commande;

import java.io.IOException;

public class DetailsCommandeController {

    @FXML private Label dateCreationLabel;
    @FXML private Label utilisateurLabel;
    @FXML private Label totalLabel;
    @FXML private Label statutLabel;
    @FXML private Label adresseLivraisonLabel;
    @FXML private Label modePaiementLabel;
    @FXML private Label datePaiementLabel;
    @FXML private Label statutPaiementLabel;
    @FXML private Button retourButton;
    private Commande commande;

    public void setCommande(Commande commande) {
        this.commande = commande;
        afficherInfosCommande();
    }

    private void afficherInfosCommande() {
        dateCreationLabel.setText(String.valueOf(commande.getDateCreation()));
        utilisateurLabel.setText(commande.getUser().getName()); // ou autre champ du User
        totalLabel.setText(String.valueOf(commande.getTotal()));
        statutLabel.setText(commande.getStatus());
        adresseLivraisonLabel.setText(commande.getAdresseLivraison());
        modePaiementLabel.setText(commande.getModePaiement());
        datePaiementLabel.setText(String.valueOf(commande.getDatePaiement()));
        statutPaiementLabel.setText(commande.getStatusPaiement());
    }

    @FXML
    private void modifierCommande(ActionEvent event) {
        System.out.println("Bouton Modifier la commande cliqué.");

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/Commande/ModifierCommande.fxml"));
            Parent root = loader.load();

            ModifierCommandeController controller = loader.getController();
            controller.setCommande(commande); // Transfert des données

            Stage stage = new Stage();
            stage.setTitle("Modifier Commande");
            stage.setScene(new Scene(root));

            // 🔄 Refresh when the window is closed
            stage.setOnHidden(e -> afficherInfosCommande());

            stage.show(); // Non-blocking window

        } catch (IOException e) {
            System.err.println("❌ Erreur lors du chargement de la fenêtre de modification : " + e.getMessage());
            e.printStackTrace();
        }
    }



}
