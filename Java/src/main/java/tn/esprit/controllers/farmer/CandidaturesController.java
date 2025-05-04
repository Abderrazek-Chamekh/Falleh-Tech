package tn.esprit.controllers.farmer;

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
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.layout.GridPane;
import javafx.geometry.Insets;
import javafx.scene.control.Label;
import javafx.scene.control.Button;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.stage.Modality;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Dialog;
import javafx.scene.control.DialogPane;
import javafx.scene.control.ButtonType;

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
        c2.setStatut(StatutCandidature.ACCEPTEE);

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

            // 🧠 Dynamic Buttons based on Statut
            if (c.getStatut() == StatutCandidature.EN_ATTENTE) {
                // Waiting ➔ show Accept and Reject
                Button acceptBtn = new Button("Accepter");
                Button rejectBtn = new Button("Refuser");

                acceptBtn.getStyleClass().add("btn-accept");
                rejectBtn.getStyleClass().add("btn-reject");

                acceptBtn.setOnAction(e -> {
                    c.setStatut(StatutCandidature.ACCEPTEE);
                    serviceCandidature.updateStatut(c.getId(), StatutCandidature.ACCEPTEE);
                    loadCandidatures();
                });

                rejectBtn.setOnAction(e -> {
                    c.setStatut(StatutCandidature.REFUSEE);
                    serviceCandidature.updateStatut(c.getId(), StatutCandidature.REFUSEE);
                    loadCandidatures();
                });

                row.add(acceptBtn, 4, 0);
                row.add(rejectBtn, 5, 0);

            } else if (c.getStatut() == StatutCandidature.ACCEPTEE) {
                // Accepted ➔ show nothing (waiting for worker to finish)
                Label waitingLabel = new Label("En cours...");
                waitingLabel.getStyleClass().add("label-waiting");
                row.add(waitingLabel, 4, 0);

            } else if (c.getStatut() == StatutCandidature.REFUSEE) {
                // Refused ➔ show only Accept button
                Button acceptBtn = new Button("Accepter");
                acceptBtn.getStyleClass().addAll("btn-accept", "btn-active");

                acceptBtn.setOnAction(e -> {
                    c.setStatut(StatutCandidature.ACCEPTEE);
                    serviceCandidature.updateStatut(c.getId(), StatutCandidature.ACCEPTEE);
                    loadCandidatures();
                });

                row.add(acceptBtn, 4, 0);

            }else if (c.getStatut() == StatutCandidature.TERMINEE) {
                // Worker finished ➔ show "Confirmer" button
                Button confirmerBtn = new Button("Confirmer");
                confirmerBtn.getStyleClass().add("btn-confirm");

                confirmerBtn.setOnAction(e -> {
                    showRatingPopup(c.getUser().getNom());  // 🔥 open the rating popup
                    confirmerBtn.setDisable(true);
                });

                row.add(confirmerBtn, 4, 0);
            }

            row.add(name, 0, 0);
            row.add(email, 1, 0);
            row.add(date, 2, 0);
            row.add(statut, 3, 0);
            row.add(experience, 0, 1); // next line

            VBox wrapper = new VBox(row);
            wrapper.getStyleClass().add("candidature-card");

            switch (c.getStatut()) {
                case ACCEPTEE -> wrapper.getStyleClass().add("accepted");
                case REFUSEE -> wrapper.getStyleClass().add("rejected");
                case TERMINEE -> wrapper.getStyleClass().add("terminee");
            }

            candidaturesContainer.getChildren().add(wrapper);
        }
    }


    private void showRatingPopup(String workerName) {
        Stage popup = new Stage();
        popup.initModality(Modality.APPLICATION_MODAL);
        popup.setTitle("Évaluer le travail de " + workerName);

        VBox layout = new VBox(20);
        layout.setStyle("-fx-padding: 25; -fx-alignment: center; -fx-background-color: linear-gradient(to bottom, #ffffff, #f2f2f2); -fx-border-color: #dddddd; -fx-border-radius: 10; -fx-background-radius: 10;");

        Label title = new Label("⭐ Notez le travail de " + workerName);
        title.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #333333;");

        HBox starsBox = new HBox(10);
        starsBox.setStyle("-fx-alignment: center;");

        List<Label> stars = new ArrayList<>();
        for (int i = 1; i <= 5; i++) {
            Label star = new Label("☆"); // empty star
            star.setStyle("-fx-font-size: 40px; -fx-text-fill: #cccccc; -fx-cursor: hand; -fx-transition: all 0.3s;");

            int ratingValue = i;

            // Mouse hover
            star.setOnMouseEntered(e -> {
                for (int j = 0; j < 5; j++) {
                    stars.get(j).setStyle("-fx-font-size: 40px; -fx-cursor: hand; -fx-text-fill: " + (j < ratingValue ? "#ffd700" : "#cccccc") + ";");
                }
            });

            // Mouse exit
            star.setOnMouseExited(e -> {
                int currentRating = (int) stars.stream().filter(s -> s.getText().equals("★")).count();
                for (int j = 0; j < 5; j++) {
                    stars.get(j).setStyle("-fx-font-size: 40px; -fx-cursor: hand; -fx-text-fill: " + (j < currentRating ? "#ffd700" : "#cccccc") + ";");
                }
            });

            // Click
            star.setOnMouseClicked(e -> {
                for (int j = 0; j < 5; j++) {
                    stars.get(j).setText(j < ratingValue ? "★" : "☆");
                }
            });

            stars.add(star);
            starsBox.getChildren().add(star);
        }

        Button submitBtn = new Button("Valider l'évaluation ✅");
        submitBtn.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white; -fx-font-size: 14px; -fx-font-weight: bold; -fx-background-radius: 8; -fx-padding: 10 20;");
        submitBtn.setOnAction(e -> {
            int finalRating = (int) stars.stream().filter(s -> s.getText().equals("★")).count();
            System.out.println("⭐ Note attribuée à " + workerName + ": " + finalRating + " étoiles");
            popup.close();

            // You can add a nice success popup here if you want
        });

        layout.getChildren().addAll(title, starsBox, submitBtn);

        Scene scene = new Scene(layout, 400, 280);
        popup.setScene(scene);
        popup.showAndWait();
    }


    @FXML
    private void handleRetourAction() {
        System.out.println("🔙 [handleRetourAction] Clicked: Retour aux offres");

        if (contentPaneRef == null) {
            System.out.println("⚠️ [handleRetourAction] contentPaneRef is null. Make sure setContentPaneRef() is called before showing this view.");
            return;
        }

        try {
            String fxmlPath = "/front/farmer_front/MyOffresSplitView.fxml";
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

            tn.esprit.controllers.ouvrier.UserProfilePopupController controller = loader.getController();
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
