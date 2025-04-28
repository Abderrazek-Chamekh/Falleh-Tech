package tn.esprit.Controllers.front.farmer;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import tn.esprit.entities.Candidature;
import tn.esprit.entities.OffreEmploi;
import tn.esprit.entities.StatutCandidature;
import tn.esprit.entities.User;
import tn.esprit.services.ServiceCandidature;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class CandidaturesController {

    @FXML
    private VBox candidaturesContainer;

    @FXML
    private Button btnRetour;

    private StackPane contentPaneRef;
    private OffreEmploi selectedOffre;
    private List<Candidature> candidatures; // store across refreshes
    private final ServiceCandidature serviceCandidature = new ServiceCandidature();

    private void initializeDummyData() {
        User ahmed = new User();
        ahmed.setName("Ahmed");
        ahmed.setEmail("ahmed@example.com");

        User yasmine = new User();
        yasmine.setName("Yasmine");
        yasmine.setEmail("yasmine@example.com");

        Candidature c1 = new Candidature();
        c1.setUser(ahmed);
        c1.setDateApplied(LocalDateTime.now().minusDays(1));
        c1.setStatut(StatutCandidature.EN_ATTENTE);

        Candidature c2 = new Candidature();
        c2.setUser(yasmine);
        c2.setDateApplied(LocalDateTime.now().minusDays(2));
        c2.setStatut(StatutCandidature.ACCEPTE);

        candidatures = new ArrayList<>(List.of(c1, c2));
    }

    public void setContentPaneRef(StackPane contentPaneRef) {
        this.contentPaneRef = contentPaneRef;
        loadCandidatures();
    }

    public void setOffre(OffreEmploi offre) {
        this.selectedOffre = offre;
        System.out.println("🔍 Loaded Candidatures for Offre ID: " + offre.getId());
        loadCandidatures();
    }

    public void setOffre(OffreEmploi offre, StackPane pane) {
        this.selectedOffre = offre;
        this.contentPaneRef = pane;
        loadCandidatures();
    }

    private void loadCandidatures() {
        candidaturesContainer.getChildren().clear();

        if (selectedOffre == null) {
            System.out.println("❌ No offer selected.");
            return;
        }

        List<Candidature> list = serviceCandidature.getByOffreId(selectedOffre.getId());

        for (Candidature c : list) {
            GridPane row = new GridPane();
            row.setHgap(20);
            row.setVgap(5);
            row.setPadding(new Insets(10));

            Label name = new Label("👤 " + c.getNom());
            name.getStyleClass().add("label-name");
            name.setOnMouseClicked(e -> showWorkerPopup(c.getUser()));

            Label email = new Label("✉ " + c.getEmail());
            email.getStyleClass().add("label-email");

            Label date = new Label("📅 " + c.getDateApplied().toLocalDate());
            date.getStyleClass().add("label-date");

            Label statut = new Label("⏳ Statut: " + c.getStatut());
            statut.getStyleClass().add("label-statut");

            Label experience = new Label("💼 Expérience: " + c.getUser().getExperience());
            experience.getStyleClass().add("label-exp");

            Button acceptBtn = new Button("Accepter");
            Button rejectBtn = new Button("Refuser");

            acceptBtn.getStyleClass().add("btn-accept");
            rejectBtn.getStyleClass().add("btn-reject");

            if (c.getStatut() == StatutCandidature.ACCEPTE) {
                acceptBtn.getStyleClass().add("btn-active");
            } else if (c.getStatut() == StatutCandidature.REJETE) {
                rejectBtn.getStyleClass().add("btn-active");
            }

            acceptBtn.setOnAction(e -> {
                c.setStatut(StatutCandidature.ACCEPTE);
                serviceCandidature.updateStatut(c.getId(), StatutCandidature.ACCEPTE);
                loadCandidatures();
            });

            rejectBtn.setOnAction(e -> {
                c.setStatut(StatutCandidature.REJETE);
                serviceCandidature.updateStatut(c.getId(), StatutCandidature.REJETE);
                loadCandidatures();
            });

            row.add(name, 0, 0);
            row.add(email, 1, 0);
            row.add(date, 2, 0);
            row.add(statut, 3, 0);
            row.add(acceptBtn, 4, 0);
            row.add(rejectBtn, 5, 0);
            row.add(experience, 0, 1); // next line

            VBox wrapper = new VBox(row);
            wrapper.getStyleClass().add("candidature-card");

            switch (c.getStatut()) {
                case ACCEPTE -> wrapper.getStyleClass().add("accepted");
                case REJETE -> wrapper.getStyleClass().add("rejected");
            }

            candidaturesContainer.getChildren().add(wrapper);
        }
    }


    @FXML
    private void handleRetourAction() {
        System.out.println("🔙 [handleRetourAction] Clicked: Retour aux offres");

        if (contentPaneRef == null) {
            System.out.println("⚠️ [handleRetourAction] contentPaneRef is null. Make sure setContentPaneRef() is called before showing this view.");
            return;
        }

        try {
            String fxmlPath = "/front/farmer_front/MesOffresView.fxml";
            System.out.println("📂 [handleRetourAction] Attempting to load: " + fxmlPath);

            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Parent offresView = loader.load();

            contentPaneRef.getChildren().setAll(offresView);
            System.out.println("🔁 [handleRetourAction] View switched to MesOffresView.");
        } catch (IOException e) {
            System.out.println("💥 [handleRetourAction] IOException occurred while loading the FXML:");
            e.printStackTrace();
        } catch (Exception ex) {
            System.out.println("💥 [handleRetourAction] Unexpected exception occurred:");
            ex.printStackTrace();
        }
    }

    // ✅ POPUP: User profile modal
    private void showWorkerPopup(User user) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/front/ouvrier_front/UserProfilePopup.fxml"));
            Parent root = loader.load();

            tn.esprit.Controllers.ouvrier.UserProfilePopupController controller = loader.getController();
            controller.setUser(user);

            Stage popupStage = new Stage();
            popupStage.initModality(Modality.APPLICATION_MODAL);
            popupStage.setTitle("Profil de " + user.getName());
            popupStage.setScene(new Scene(root));
            popupStage.showAndWait();
        } catch (IOException e) {
            System.out.println("❌ Erreur lors de l'ouverture du profil:");
            e.printStackTrace();
        }
    }
}
