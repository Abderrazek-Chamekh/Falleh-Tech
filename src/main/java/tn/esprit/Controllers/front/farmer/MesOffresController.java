package tn.esprit.Controllers.front.farmer;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import tn.esprit.entities.OffreEmploi;
import tn.esprit.entities.User;
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
    private int currentUserId = -1;
    private User currentUser;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        System.out.println("[MesOffresController] Initialized");

        ToggleGroup toggleGroup = new ToggleGroup();
        btnAllOffers.setToggleGroup(toggleGroup);
        btnMyOffers.setToggleGroup(toggleGroup);

        btnAddOffer.setOnAction(e -> openAddOfferPopup());

        btnAllOffers.setOnAction(e -> {
            if (btnAllOffers.isSelected()) {
                System.out.println("All Offers selected");
                loadAllOffers();
            }
        });

        btnMyOffers.setOnAction(e -> {
            if (btnMyOffers.isSelected()) {
                System.out.println("My Offers selected");
                loadMyOffers();
            }
        });

        btnAllOffers.setSelected(true); // default
    }

    public void setCurrentUser(User user) {
        this.currentUser = user;
        this.currentUserId = user.getId();
        System.out.println("✅ Current user set: " + user.getName() + " (ID: " + currentUserId + ")");
        loadAllOffers();
    }

    public void setContentPaneRef(StackPane pane) {
        this.contentPaneRef = pane;
    }

    private void loadAllOffers() {
        System.out.println("Loading all offers...");
        List<OffreEmploi> offres = service.getAll();
        System.out.println("Total offers found: " + offres.size());
        displayOffers(offres);
    }

    private void loadMyOffers() {
        if (currentUserId <= 0) {
            System.err.println("❌ currentUserId is not set properly.");
            return;
        }
        System.out.println("Loading my offers for userId: " + currentUserId);
        List<OffreEmploi> myOffres = service.getByEmployeurId(currentUserId);
        System.out.println("Total my offers found: " + myOffres.size());
        displayOffers(myOffres);
    }

    private void displayOffers(List<OffreEmploi> offers) {
        offersGrid.getChildren().clear();
        boolean isMyOffers = btnMyOffers.isSelected();
        System.out.println("Displaying " + offers.size() + " offers. isMyOffers = " + isMyOffers);

        if (contentPaneRef == null) {
            System.err.println("❌ contentPaneRef is not set.");
            return;
        }

        for (OffreEmploi o : offers) {
            try {
                String fxmlFile = isMyOffers ? "/front/farmer_front/card_offre_my.fxml" : "/front/farmer_front/card_offre.fxml";
                FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlFile));
                VBox card = loader.load();

                OffreCardController controller = loader.getController();
                controller.setData(o);
                controller.setContentPaneRef(contentPaneRef);
                controller.setCurrentUser(currentUser); // ✅ Pass logged-in user

                offersGrid.getChildren().add(card);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void openAddOfferPopup() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/front/farmer_front/AddOffreDialog.fxml"));
            ScrollPane form = loader.load();
            AddOffreDialogController controller = loader.getController();

            controller.setCurrentUser(currentUser); // ✅ Pass full user object

            Dialog<ButtonType> dialog = new Dialog<>();
            dialog.setTitle("Nouvelle Offre");
            dialog.getDialogPane().setContent(form);
            dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
            dialog.getDialogPane().getStylesheets().add(getClass().getResource("/styles/add_offre_dialog.css").toExternalForm());

            Button okButton = (Button) dialog.getDialogPane().lookupButton(ButtonType.OK);
            okButton.addEventFilter(javafx.event.ActionEvent.ACTION, event -> {
                boolean valid = controller.validateAndSubmit();
                if (!valid) {
                    event.consume();
                }
            });

            dialog.showAndWait();
            loadAllOffers(); // refresh after adding

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handleVoirCandidatures(OffreEmploi selectedOffre) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/front/farmer_front/CandidaturesView.fxml"));
            Parent view = loader.load();

            CandidaturesController controller = loader.getController();
            controller.setContentPaneRef(contentPaneRef);
            controller.setOffre(selectedOffre);

            contentPaneRef.getChildren().setAll(view);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
