package tn.esprit.controllers.farmer;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import tn.esprit.entities.OffreEmploi;
import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;
import java.util.List;

public class MesOffresController implements Initializable {

    @FXML private ScrollPane scrollPane;
    @FXML private FlowPane offersGrid;

    // Static pane to swap views from FrontViewController
    private static StackPane contentPaneRef;

    public static void setContentPaneRef(StackPane pane) {
        contentPaneRef = pane;
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        System.out.println("🔍 Loading all job offers...");

        // Sample data
        List<OffreEmploi> offres = List.of(
                new OffreEmploi("Récolte d’olives", "Saida", "2025-04-12", "2025-04-15", "Récolte manuelle d’olives..."),
                new OffreEmploi("Tractoriste", "Kairouan", "2025-04-20", "2025-04-22", "Conduite de tracteur dans les champs..."),
                new OffreEmploi("Plantation de tomates", "Gafsa", "2025-05-05", "2025-05-10", "Préparation et plantation de jeunes plants...")
        );

        offersGrid.getChildren().clear();

        for (OffreEmploi offre : offres) {
            VBox card = createCard(offre);
            offersGrid.getChildren().add(card);
        }
    }

    private VBox createCard(OffreEmploi offre) {
        Label titreLabel = new Label("🧑‍🌾 " + offre.getTitre());
        titreLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");

        Label lieuLabel = new Label("📍 " + offre.getLieu());
        Label dateLabel = new Label("📅 " + offre.getStartDate() + " - " + offre.getDateExpiration());

        Button detailsBtn = new Button("Voir détails");
        detailsBtn.setOnAction(e -> showDetails(offre));

        VBox card = new VBox(10);
        card.setStyle("-fx-background-color: #f8f8f8; -fx-padding: 15; -fx-border-color: #ccc; -fx-border-radius: 8; -fx-background-radius: 8;");
        card.setPrefWidth(200);
        card.getChildren().addAll(titreLabel, lieuLabel, dateLabel, detailsBtn);

        return card;
    }

    private void showDetails(OffreEmploi offre) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/front/farmer_front/OffreDetailsView.fxml"));
            Parent detailsView = loader.load();

            OffreDetailsController controller = loader.getController();
            controller.setOffre(offre, contentPaneRef); // 🤝 Pass offer and parent StackPane

            contentPaneRef.getChildren().setAll(detailsView);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
