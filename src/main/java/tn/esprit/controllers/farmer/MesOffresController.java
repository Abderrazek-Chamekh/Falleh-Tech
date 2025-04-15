package tn.esprit.controllers.farmer;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import tn.esprit.entities.OffreEmploi;
import tn.esprit.services.ServiceOffreEmploi;

import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

public class MesOffresController implements Initializable {

    @FXML private ScrollPane scrollPane;
    @FXML private FlowPane offersGrid;
    @FXML private ToggleButton btnAllOffers;
    @FXML private ToggleButton btnMyOffers;
    @FXML private Button btnAddOffer;
    @FXML private StackPane contentPaneRef;

    private final ServiceOffreEmploi service = new ServiceOffreEmploi();
    private static StackPane staticContentPane;
    private int currentUserId = 30; // 👤 Replace with logged-in user if dynamic

    public static void setContentPaneRef(StackPane pane) {
        staticContentPane = pane;
    }

    public void setCurrentUserId(int id) {
        this.currentUserId = id;
        loadAllOffers(); // 👈 auto-load after setting user
    }


    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        ToggleGroup toggleGroup = new ToggleGroup();
        btnAllOffers.setToggleGroup(toggleGroup);
        btnMyOffers.setToggleGroup(toggleGroup);

        btnAllOffers.setOnAction(e -> {
            if (btnAllOffers.isSelected()) loadAllOffers();
        });

        btnMyOffers.setOnAction(e -> {
            if (btnMyOffers.isSelected()) loadMyOffers();
        });

        btnAddOffer.setOnAction(e -> openAddOfferPopup());

        loadAllOffers(); // Load all offers by default
    }

    private void loadAllOffers() {
        List<OffreEmploi> offres = service.getAll();
        displayOffers(offres);
    }

    private void loadMyOffers() {
        List<OffreEmploi> myOffres = service.getByEmployeurId(currentUserId);
        displayOffers(myOffres);
    }

    private void displayOffers(List<OffreEmploi> offers) {
        offersGrid.getChildren().clear();
        boolean isMyOffers = btnMyOffers.isSelected();

        for (OffreEmploi o : offers) {
            try {
                String fxmlFile = isMyOffers ? "/front/farmer_front/card_offre_my.fxml" : "/front/farmer_front/card_offre.fxml";
                FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlFile));
                VBox card = loader.load();

                OffreCardController controller = loader.getController();
                controller.setData(o); // Your controller sets labels + button handlers

                offersGrid.getChildren().add(card);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }


    private void openAddOfferPopup() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/front/farmer_front/AddOffreDialog.fxml"));
            DialogPane dialogPane = loader.load();

            Dialog<ButtonType> dialog = new Dialog<>();
            dialog.setDialogPane(dialogPane);
            dialog.setTitle("Nouvelle Offre");
            dialog.showAndWait();

            loadAllOffers(); // Refresh after add
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
