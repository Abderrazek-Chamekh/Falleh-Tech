package tn.esprit.controllers.ouvrier;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import tn.esprit.entities.Candidature;
import tn.esprit.entities.OffreEmploi;
import tn.esprit.entities.StatutCandidature;
import tn.esprit.entities.User;
import tn.esprit.services.ServiceCandidature;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.IOException;
import java.time.LocalDateTime;

public class OffreCardOuvrierController {

    @FXML private VBox cardContainer;
    @FXML private Label lblTitre;
    @FXML private Label lblDescription;
    @FXML private Label lblLocation;
    @FXML private Label lblSalaire;
    @FXML private Label lblStatus;
    @FXML private Button btnContact;
    @FXML private Button btnWithdraw;

    private OffreEmploi offre;
    private User currentUser;
    private final ServiceCandidature service = new ServiceCandidature();

    public void setData(OffreEmploi offre, User user) {
        this.offre = offre;
        this.currentUser = user;

        lblTitre.setText("📋 " + offre.getTitre());
        lblDescription.setText(offre.getDescription());
        lblLocation.setText("📍 " + offre.getLieu());
        lblSalaire.setText("💰 " + offre.getSalaire() + " TND");

        checkAndUpdateCandidatureStatus();

        btnContact.setOnAction(e -> openChatPopup());
        btnWithdraw.setOnAction(e -> retirerCandidature());
    }

    private void checkAndUpdateCandidatureStatus() {
        lblStatus.setText("");
        lblStatus.setStyle("");

        Candidature existing = service.getCandidatureByUserAndOffre(currentUser.getId(), offre.getId());

        if (existing != null) {
            StatutCandidature status = existing.getStatut();
            btnWithdraw.setVisible(status == StatutCandidature.EN_ATTENTE);

            switch (status) {
                case ACCEPTEE -> {
                    lblStatus.setText("✅ Acceptée");
                    lblStatus.setStyle("-fx-background-color: #27ae60; -fx-text-fill: white; -fx-padding: 4 10; -fx-background-radius: 10;");
                }
                case TERMINEE -> {
                    lblStatus.setText("⏳ Terminé");
                    lblStatus.setStyle("-fx-background-color: #3498db; -fx-text-fill: white; -fx-padding: 4 10; -fx-background-radius: 10;");
                }
                case REFUSEE -> {
                    lblStatus.setText("❌ Refusée");
                    lblStatus.setStyle("-fx-background-color: #e74c3c; -fx-text-fill: white; -fx-padding: 4 10; -fx-background-radius: 10;");
                }
                case EN_ATTENTE -> {
                    lblStatus.setText("⏳ En attente");
                    lblStatus.setStyle("-fx-background-color: #f39c12; -fx-text-fill: white; -fx-padding: 4 10; -fx-background-radius: 10;");
                }
            }
        } else {
            lblStatus.setText("🟢 Disponible");
            lblStatus.setStyle("-fx-background-color: #2ecc71; -fx-text-fill: white; -fx-padding: 4 10; -fx-background-radius: 10;");
            btnWithdraw.setVisible(false);
        }
    }

    private void openChatPopup() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/front/ouvrier_front/chat_popup.fxml"));
            Parent root = loader.load();

            ChatPopupController controller = loader.getController();
            int chatPartnerId = offre.getIdEmployeur().getId();
            controller.setChatSession(currentUser, chatPartnerId);

            Stage stage = new Stage();
            stage.setTitle("Messagerie");
            stage.setScene(new Scene(root));
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void retirerCandidature() {
        Candidature existing = service.getCandidatureByUserAndOffre(currentUser.getId(), offre.getId());
        if (existing != null) {
            service.supprimer(existing.getId());
        }
        checkAndUpdateCandidatureStatus();
    }
}
