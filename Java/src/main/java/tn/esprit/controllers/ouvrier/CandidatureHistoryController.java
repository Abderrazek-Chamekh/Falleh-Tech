package tn.esprit.controllers.ouvrier;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import tn.esprit.entities.Candidature;
import tn.esprit.entities.OffreEmploi;
import tn.esprit.entities.User;
import tn.esprit.services.ServiceCandidature;

import java.util.List;

public class CandidatureHistoryController {

    @FXML
    private VBox historyContainer;
    @FXML
    private VBox candidatureListContainer;

    private final ServiceCandidature service = new ServiceCandidature();
    private User currentUser;


    public void setCurrentUser(User user) {
        this.currentUser = user;
        loadHistory(); // <- Will now work because historyContainer is correctly loaded
    }


    private void loadHistory() {
        List<Candidature> candidatures = service.getByUserId(currentUser.getId());
        historyContainer.getChildren().clear();

        for (Candidature c : candidatures) {
            OffreEmploi offre = c.getOffre();

            Label title = new Label("📌 " + offre.getTitre());
            Label date = new Label("📅 Date: " + c.getDateApplied().toLocalDate());
            Label status = new Label("⏳ Statut: " + c.getStatut());

            String bgColor = switch (c.getStatut()) {
                case ACCEPTEE -> "#c8e6c9";
                case REFUSEE -> "#ffcdd2";
                case EN_ATTENTE -> "#fff9c4";
                default -> "#eeeeee";
            };

            VBox card = new VBox(title, date, status);
            card.setStyle("-fx-background-color: " + bgColor + "; -fx-padding: 10; -fx-border-radius: 8; -fx-background-radius: 8;");
            historyContainer.getChildren().add(card);
        }
    }


}
