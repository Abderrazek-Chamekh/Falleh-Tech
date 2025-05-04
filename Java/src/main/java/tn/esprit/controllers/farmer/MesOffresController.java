
package tn.esprit.controllers.farmer;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.StageStyle;
import tn.esprit.controllers.farmer.CandidaturesController;
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
    @FXML private TextField searchField;
    @FXML private ComboBox<String> locationFilter;
    @FXML private ToggleButton sortDateToggle;

    private final ServiceOffreEmploi service = new ServiceOffreEmploi();
    private static StackPane staticContentPane;
    private int currentUserId = 30; // Replace with actual logged-in user ID

    public static void setContentPaneRef(StackPane pane) {
        staticContentPane = pane;
    }

    public static StackPane getContentPaneRef() {
        return staticContentPane;
    }

    public void setCurrentUserId(int id) {
        this.currentUserId = id;
        System.out.println("User ID set to: " + id);
        loadAllOffers();
    }

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
                System.out.println("My Offers (split view) selected");
                try {
                    FXMLLoader loader = new FXMLLoader(getClass().getResource("/front/farmer_front/MyOffersSplitView.fxml"));
                    Parent view = loader.load();
                    contentPaneRef.getChildren().setAll(view);
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            }
        });


        // Initial load
        if (btnMyOffers.isSelected()) {
            loadMyOffers();
        } else {
            loadAllOffers();
        }
        locationFilter.getItems().addAll(
                "Tunis", "Ariana", "Ben Arous", "Manouba", "Nabeul", "Zaghouan", "Bizerte", "Béja",
                "Jendouba", "Le Kef", "Siliana", "Kairouan", "Kasserine", "Sidi Bouzid", "Sousse", "Monastir",
                "Mahdia", "Sfax", "Gafsa", "Tozeur", "Kebili", "Gabès", "Tataouine", "Médenine"
        );
        searchField.textProperty().addListener((observable, oldValue, newValue) -> {
            applyFilters(); // triggers filtering on every keystroke
        });
        locationFilter.valueProperty().addListener((obs, oldVal, newVal) -> applyFilters());
        sortDateToggle.selectedProperty().addListener((obs, oldVal, newVal) -> applyFilters());


    }

    private void loadAllOffers() {
        System.out.println("Loading all offers...");
        List<OffreEmploi> offres = service.getAll();
        System.out.println("Total offers found: " + offres.size());
        displayOffers(offres);
    }

    private void loadMyOffers() {
        System.out.println("Loading my offers for userId: " + currentUserId);
        List<OffreEmploi> myOffres = service.getByEmployeurId(currentUserId);
        System.out.println("Total my offers found: " + myOffres.size());
        displayOffers(myOffres);
    }

    private void displayOffers(List<OffreEmploi> offers) {
        offersGrid.getChildren().clear();
        boolean isMyOffers = btnMyOffers.isSelected();
        System.out.println("Displaying " + offers.size() + " offers. isMyOffers = " + isMyOffers);

        StackPane paneToUse = contentPaneRef != null ? contentPaneRef : staticContentPane;
        if (paneToUse == null) {
            System.err.println("❌ No contentPaneRef is set. Cannot inject into cards.");
            return;
        }

        for (OffreEmploi o : offers) {
            try {
                String fxmlFile = isMyOffers ? "/front/farmer_front/card_offre_my.fxml" : "/front/farmer_front/card_offre.fxml";
                FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlFile));
                VBox card = loader.load();

                OffreCardController controller = loader.getController();
                controller.setData(o);
                controller.setContentPaneRef(paneToUse);

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

            Dialog<ButtonType> dialog = new Dialog<>();
            dialog.initStyle(StageStyle.UNDECORATED); // 🔥 This removes the default header!

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
            loadAllOffers();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    @FXML
    private void applyFilters() {
        String searchText = searchField.getText().toLowerCase().trim();
        String location = locationFilter.getValue();
        boolean newestFirst = sortDateToggle.isSelected();

        List<OffreEmploi> filtered = service.getAll().stream()
                .filter(o -> searchText.isEmpty() ||
                        o.getTitre().toLowerCase().contains(searchText) ||
                        o.getDescription().toLowerCase().contains(searchText))
                .filter(o -> location == null || location.isEmpty() || o.getLieu().equalsIgnoreCase(location))
                .sorted((a, b) -> {
                    if (newestFirst) {
                        return b.getStartDate().compareTo(a.getStartDate());
                    } else {
                        return a.getStartDate().compareTo(b.getStartDate());
                    }
                })
                .toList();

        displayOffers(filtered);
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