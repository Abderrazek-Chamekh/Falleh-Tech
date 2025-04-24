// OffreCardOuvrierController.java
package tn.esprit.controllers.ouvrier;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import tn.esprit.entities.Candidature;
import tn.esprit.entities.OffreEmploi;
import tn.esprit.entities.StatutCandidature;
import tn.esprit.entities.User;
import tn.esprit.services.ServiceCandidature;
import tn.esprit.controllers.MessagingController;
import java.io.IOException;
import java.time.LocalDateTime;

public class OffreCardOuvrierController {

    @FXML private Label lblTitre;
    @FXML private Label lblDescription;
    @FXML private Label lblFullDescription;
    @FXML private Label lblSalaire;
    @FXML private Label lblLocation;
    @FXML private Label lblStatus;
    @FXML private Label lblStartDate;
    @FXML private Label lblEndDate;
    @FXML private Button btnApply;
    @FXML private Button btnContact;
    @FXML private Button btnWithdraw;
    @FXML private VBox cardContainer;
    @FXML private VBox expandableBox;
    @FXML private Button btnExpand;

    private OffreEmploi offre;
    private User currentUser;
    private boolean expanded = false;

    private final ServiceCandidature service = new ServiceCandidature();

    public void setData(OffreEmploi offre, User user) {
        this.offre = offre;
        this.currentUser = user;

        lblTitre.setText(offre.getTitre());
        lblDescription.setText(offre.getDescription());
        lblFullDescription.setText(offre.getDescription());
        lblSalaire.setText(String.valueOf(offre.getSalaire()));
        lblLocation.setText(offre.getLieu());
        lblStartDate.setText(offre.getStartDate().toString());
        lblEndDate.setText(offre.getDateExpiration().toString());
        offre.getIdEmployeur().getId();
        checkAndUpdateCandidatureStatus();

        btnApply.setOnAction(event -> postuler());
        btnWithdraw.setOnAction(event -> retirerCandidature());
        btnContact.setOnAction(e -> openChatPopup());
        btnExpand.setOnAction(e -> toggleExpand());
    }

    private void toggleExpand() {
        expanded = !expanded;
        expandableBox.setManaged(expanded);
        expandableBox.setVisible(expanded);
        btnExpand.setText(expanded ? "🔼" : "🔽");

        if (expanded) {
            cardContainer.getStyleClass().add("expanded");
        } else {
            cardContainer.getStyleClass().remove("expanded");
        }
    }

    private void openChatPopup() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/front/ouvrier_front/chat_popup.fxml"));
            Parent root = loader.load();

            ChatPopupController controller = loader.getController(); // ✅ CORRECT

            User farmer = offre.getIdEmployeur();

            int chatPartnerId = offre.getIdEmployeur().getId();
            controller.setChatSession(currentUser, chatPartnerId);

            Stage stage = new Stage();
            stage.setTitle("Messagerie avec   " + farmer.getNom());
            stage.setScene(new Scene(root));
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void checkAndUpdateCandidatureStatus() {
        cardContainer.getStyleClass().removeAll("status-accepted", "status-rejected", "status-pending", "status-none");
        lblStatus.setText("");

        Candidature existing = service.getCandidatureByUserAndOffre(currentUser.getId(), offre.getId());

        if (existing != null) {
            StatutCandidature status = existing.getStatut();
            btnWithdraw.setVisible(status == StatutCandidature.EN_ATTENTE);

            switch (status) {
                case ACCEPTEE -> {
                    cardContainer.getStyleClass().add("status-accepted");
                    lblStatus.setText("✅ Acceptée");
                    lblStatus.setStyle("-fx-background-color: #27ae60;");
                    btnApply.setDisable(false);
                    btnApply.setText("✅ Marquer comme terminée");
                    btnApply.setOnAction(e -> {
                        service.updateStatut(existing.getId(), StatutCandidature.TERMINEE);
                        checkAndUpdateCandidatureStatus();
                    });
                }
                case TERMINEE -> {
                    cardContainer.getStyleClass().add("status-pending");
                    lblStatus.setText("⏳ En attente de confirmation");
                    lblStatus.setStyle("-fx-background-color: #f39c12;");
                    btnApply.setDisable(true);
                    btnApply.setText("En attente de confirmation...");
                }
                case REFUSEE -> {
                    cardContainer.getStyleClass().add("status-rejected");
                    lblStatus.setText("❌ Refusée");
                    lblStatus.setStyle("-fx-background-color: #c0392b;");
                    btnApply.setDisable(true);
                    btnApply.setText("❌ Rejeté");
                }
                case EN_ATTENTE -> {
                    cardContainer.getStyleClass().add("status-pending");
                    lblStatus.setText("⏳ En attente");
                    lblStatus.setStyle("-fx-background-color: #f39c12;");
                    btnApply.setDisable(true);
                    btnApply.setText("⏳ En attente");
                }
            }
        } else {
            lblStatus.setText("🟢 Disponible");
            lblStatus.setStyle("-fx-background-color: #3498db;");
            cardContainer.getStyleClass().add("status-none");

            btnApply.setDisable(false);
            btnApply.setText("Postuler");
            btnWithdraw.setVisible(false);
        }
    }

    private void postuler() {
        Candidature c = new Candidature();
        c.setStatut(StatutCandidature.EN_ATTENTE);
        c.setDateApplied(LocalDateTime.now());
        c.setOffre(offre);

        LocalDateTime offerStart = offre.getStartDate().atStartOfDay();
        LocalDateTime offerEnd = offre.getDateExpiration().atTime(23, 59);

        boolean hasConflict = service.hasAcceptedOverlap(currentUser.getId(), offerStart, offerEnd);

        if (hasConflict) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Conflit détecté");
            alert.setHeaderText("❌ Impossible de postuler");
            alert.setContentText("Vous avez déjà une offre acceptée durant cette période.");
            alert.showAndWait();
        } else {
            service.ajouter(c, offre.getId(), currentUser.getId(), offerStart, offerEnd);

            Alert success = new Alert(Alert.AlertType.INFORMATION);
            success.setTitle("Candidature envoyée");
            success.setHeaderText("✅ Succès");
            success.setContentText("Votre candidature a bien été envoyée.");
            success.showAndWait();

            checkAndUpdateCandidatureStatus();
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