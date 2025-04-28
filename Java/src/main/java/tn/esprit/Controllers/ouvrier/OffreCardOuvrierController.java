package tn.esprit.Controllers.ouvrier;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import tn.esprit.entities.Candidature;
import tn.esprit.entities.OffreEmploi;
import tn.esprit.entities.StatutCandidature;
import tn.esprit.entities.User;
import tn.esprit.services.ServiceCandidature;

import java.time.LocalDateTime;

public class OffreCardOuvrierController {

    @FXML private Label lblTitre;
    @FXML private Label lblDescription;
    @FXML private Label lblSalaire;
    @FXML private Label lblLocation;
    @FXML private Button btnApply;
    @FXML private Button btnContact;
    @FXML private VBox cardContainer;
    @FXML private Button btnWithdraw;

    private OffreEmploi offre;
    private User currentUser;

    private final ServiceCandidature service = new ServiceCandidature();

    public void setData(OffreEmploi offre, User user) {
        this.offre = offre;
        this.currentUser = user;

        lblTitre.setText(offre.getTitre());
        lblDescription.setText(offre.getDescription());
        lblSalaire.setText(String.valueOf(offre.getSalaire()));
        lblLocation.setText(offre.getLieu());

        checkAndUpdateCandidatureStatus();
    }

    @FXML
    private void initialize() {
        btnApply.setOnAction(event -> postuler());
    }

    private void checkAndUpdateCandidatureStatus() {
        // Clean up previous style classes
        cardContainer.getStyleClass().removeAll("status-accepted", "status-rejected", "status-pending");

        Candidature existing = service.getCandidatureByUserAndOffre(currentUser.getId(), offre.getId());

        if (existing != null) {
            StatutCandidature status = existing.getStatut();

            // Update Apply button state and label
            btnApply.setDisable(true);
            btnWithdraw.setVisible(status == StatutCandidature.EN_ATTENTE); // show only if EN_ATTENTE

            // Set style class based on status
            switch (status) {
                case ACCEPTE -> {
                    cardContainer.getStyleClass().add("status-accepted");
                    btnApply.setText("✅ Accepté");
                }
                case REJETE -> {
                    cardContainer.getStyleClass().add("status-rejected");
                    btnApply.setText("❌ Rejeté");
                }
                case EN_ATTENTE -> {
                    cardContainer.getStyleClass().add("status-pending");
                    btnApply.setText("⏳ En attente");
                }
            }

            // Withdraw logic
            btnWithdraw.setOnAction(e -> {
                service.supprimer(existing.getId());
                btnApply.setDisable(false);
                btnApply.setText("Postuler");
                btnWithdraw.setVisible(false);
                cardContainer.getStyleClass().removeAll("status-accepted", "status-rejected", "status-pending");
            });
        }
    }

    private void postuler() {
        Candidature c = new Candidature();
        c.setStatut(StatutCandidature.EN_ATTENTE);
        c.setDateApplied(LocalDateTime.now());

        service.ajouter(c, offre.getId(), currentUser.getId());

        btnApply.setDisable(true);
        btnApply.setText("⏳ En attente");
        checkAndUpdateCandidatureStatus();
    }
}
