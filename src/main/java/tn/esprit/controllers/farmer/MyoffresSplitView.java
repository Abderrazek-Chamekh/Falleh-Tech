package tn.esprit.controllers.farmer;

import javafx.animation.FadeTransition;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.Duration;
import tn.esprit.entities.OffreEmploi;
import tn.esprit.entities.User;
import tn.esprit.services.ServiceOffreEmploi;
import tn.esprit.services.ServiceCandidature;

import java.io.IOException;
import java.util.List;

public class MyoffresSplitView {

    @FXML private TextField searchField;
    @FXML private ListView<OffreEmploi> offerListView;
    @FXML private VBox detailsPane;

    private final ServiceOffreEmploi service = new ServiceOffreEmploi();
    private final ServiceCandidature serviceCandidature = new ServiceCandidature();
    private List<OffreEmploi> allOffers;

    @FXML
    public void initialize() {
        allOffers = service.getByEmployeurId(30); // Replace with real connected user ID

        offerListView.setCellFactory(list -> new ListCell<>() {
            @Override
            protected void updateItem(OffreEmploi offer, boolean empty) {
                super.updateItem(offer, empty);
                if (empty || offer == null) {
                    setGraphic(null);
                } else {
                    VBox container = new VBox(2);
                    container.setStyle("-fx-padding: 8;");

                    Label title = new Label(offer.getTitre());
                    title.setStyle("-fx-font-weight: bold; -fx-font-size: 14px;");

                    int count = 0;
                    try {
                        count = serviceCandidature.getByOffreId(offer.getId()).size();
                    } catch (Exception ignored) {}

                    Label stats = new Label("👥 " + count + " candidat" + (count > 1 ? "s" : ""));
                    stats.setStyle("-fx-font-size: 11px; -fx-text-fill: #777;");

                    container.getChildren().addAll(title, stats);
                    setGraphic(container);
                }
            }
        });

        searchField.textProperty().addListener((obs, oldVal, newVal) -> updateOfferList(newVal.toLowerCase().trim()));
        offerListView.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) showOfferDetails(newVal);
        });

        updateOfferList("");
    }

    private void updateOfferList(String filter) {
        offerListView.getItems().clear();
        allOffers.stream()
                .filter(o -> o.getTitre().toLowerCase().contains(filter))
                .forEach(offerListView.getItems()::add);
    }

    private void showOfferDetails(OffreEmploi offer) {
        detailsPane.getChildren().clear();

        VBox content = new VBox(12);
        content.setOpacity(0);
        content.setStyle("-fx-background-color: #ffffff; -fx-padding: 20; -fx-border-color: #e0e0e0; -fx-border-radius: 10; -fx-background-radius: 10;");

        Label titleLabel = new Label("📌 " + offer.getTitre());
        titleLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");

        Label ownerLabel = new Label("Dirigé par: Voir Profil");
        ownerLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #3498db; -fx-underline: true;");
        ownerLabel.setOnMouseClicked(e -> {
            try {
                showEmployerPopup(offer.getIdEmployeur());
            } catch (Exception ex) {
                System.out.println("❌ Erreur lors du chargement du profil employeur");
            }
        });

        Label salaryLabel = new Label("💰 Salaire: " + offer.getSalaire() + " TND");
        salaryLabel.setStyle("-fx-font-size: 14px;");

        Label locationLabel = new Label("📍 Lieu: " + offer.getLieu());
        locationLabel.setStyle("-fx-font-size: 14px;");

        // 👉 Button to view candidatures
        Button voirBtn = new Button("📄 Gérer toutes les candidatures");
        voirBtn.setStyle("-fx-background-color: #1abc9c; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 6; -fx-padding: 8 18;");
        voirBtn.setOnAction(e -> openCandidaturesController(offer));

        content.getChildren().addAll(titleLabel, ownerLabel, salaryLabel, locationLabel, voirBtn);
        detailsPane.getChildren().add(content);

        FadeTransition fade = new FadeTransition(Duration.millis(300), content);
        fade.setFromValue(0);
        fade.setToValue(1);
        fade.play();
    }

    private void showEmployerPopup(User employer) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/front/ouvrier_front/UserProfilePopup.fxml"));
            Parent root = loader.load();

            tn.esprit.controllers.ouvrier.UserProfilePopupController controller = loader.getController();
            controller.setUser(employer);

            Stage popup = new Stage();
            popup.setTitle("Profil de " + employer.getName());
            popup.setScene(new Scene(root));
            popup.initModality(Modality.APPLICATION_MODAL);
            popup.showAndWait();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void openCandidaturesController(OffreEmploi offre) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/front/farmer_front/CandidaturesView.fxml"));
            Parent view = loader.load();

            CandidaturesController controller = loader.getController();
            controller.setOffre(offre);
            controller.setContentPaneRef(detailsPane.getParent() instanceof StackPane sp ? sp : null);

            detailsPane.getChildren().setAll(view);
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("❌ Erreur lors du chargement de la vue des candidatures");
        }
    }
}
