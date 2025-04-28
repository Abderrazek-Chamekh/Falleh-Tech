package tn.esprit.Controllers.E_Commerce;

import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.fxml.FXML;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import org.kordamp.ikonli.fontawesome5.FontAwesomeSolid;
import org.kordamp.ikonli.javafx.FontIcon;
import tn.esprit.entities.Livraison;
import tn.esprit.services.ServiceLivraison;
import tn.esprit.tools.SessionManager;
import javafx.geometry.Pos;

import java.io.IOException;
import java.util.List;

public class AfficherLivraisonController {

    @FXML private BorderPane borderPane;
    @FXML private TableView<Livraison> livraisonTableView;
    @FXML private TableColumn<Livraison, Integer> idColumn;
    @FXML private TableColumn<Livraison, String> statutColumn;
    @FXML private TableColumn<Livraison, String> transporteurColumn;
    @FXML private TableColumn<Livraison, String> numTelColumn;
    @FXML private TableColumn<Livraison, String> dateLivraisonColumn;
    @FXML private TableColumn<Livraison, Integer> commandeColumn;
    @FXML private TableColumn<Livraison, Void> actionsColumn;
    @FXML private Button addLivraisonButton;
    @FXML
    private Button retourButton;

    private final ServiceLivraison serviceLivraison = new ServiceLivraison();

    @FXML
    public void initialize() {
        idColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
        statutColumn.setCellValueFactory(new PropertyValueFactory<>("statut"));
        transporteurColumn.setCellValueFactory(new PropertyValueFactory<>("transporteur"));
        numTelColumn.setCellValueFactory(new PropertyValueFactory<>("numTelTransporteur"));
        dateLivraisonColumn.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().getDateLivraison() != null
                        ? cellData.getValue().getDateLivraison().toString()
                        : ""));

        commandeColumn.setCellValueFactory(cellData ->
                new SimpleIntegerProperty(cellData.getValue().getCommande().getId()).asObject());

        actionsColumn.setCellFactory(col -> new TableCell<Livraison, Void>() {
            // Icônes
            private final FontIcon iconDelete = new FontIcon(FontAwesomeSolid.TRASH);
            private final FontIcon iconDetails = new FontIcon(FontAwesomeSolid.EYE);

            // Boutons
            private final Button btnAnnuler = new Button();
            private final Button btnDetails = new Button();

            {
                // Style commun
                String iconColor = "#2ecc71";
                String hoverColor = "#27ae60";
                int iconSize = 16;

                // Configuration des icônes
                iconDelete.setIconColor(Color.web(iconColor));
                iconDelete.setIconSize(iconSize);

                iconDetails.setIconColor(Color.web(iconColor));
                iconDetails.setIconSize(iconSize);

                // Affecter les icônes aux boutons
                btnAnnuler.setGraphic(iconDelete);
                btnDetails.setGraphic(iconDetails);

                // Style des boutons
                String buttonStyle = "-fx-background-color: transparent; -fx-cursor: hand; -fx-padding: 4 6 4 6;";
                btnAnnuler.setStyle(buttonStyle);
                btnDetails.setStyle(buttonStyle);

                // Hover effects
                setupButtonHover(btnAnnuler, iconDelete, iconColor, hoverColor);
                setupButtonHover(btnDetails, iconDetails, iconColor, hoverColor);

                // Actions des boutons
                btnAnnuler.setOnAction(event -> {
                    Livraison livraison = getTableView().getItems().get(getIndex());
                    handleAnnulerLivraison(livraison);
                });

                btnDetails.setOnAction(event -> {
                    Livraison livraison = getTableView().getItems().get(getIndex());
                    handleAfficherDetails(livraison);
                });
            }

            private void setupButtonHover(Button button, FontIcon icon, String normalColor, String hoverColor) {
                button.setOnMouseEntered(e -> icon.setIconColor(Color.web(hoverColor)));
                button.setOnMouseExited(e -> icon.setIconColor(Color.web(normalColor)));
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    HBox box = new HBox(8, btnDetails, btnAnnuler);
                    box.setAlignment(Pos.CENTER);
                    setGraphic(box);
                }
            }
        });


        loadLivraisons();
    }

    @FXML
    private void loadDashboard() {
        loadScene("/views/User/Admin/Dashboard.fxml");
    }

    @FXML
    private void handleProfile() { loadScene("/views/User/Profile/Profile.fxml"); }

    @FXML
    private void handleLogout() {
        SessionManager.getInstance().invalidateCurrentSession();
        loadScene("/views/User/Authentication/Login.fxml");
    }


    private void loadScene(String fxmlPath) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource(fxmlPath));
            Scene scene = new Scene(root,950,800);
            Stage stage = (Stage) borderPane.getScene().getWindow();
            stage.setScene(scene);
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void loadLivraisons() {
        List<Livraison> livraisons = serviceLivraison.getAll();
        livraisonTableView.getItems().setAll(livraisons);
    }

    private void handleAnnulerLivraison(Livraison livraison) {
        if (!livraison.getStatut().equalsIgnoreCase("Annulée")) {
            livraison.setStatut("Annulée");
            serviceLivraison.modifier(livraison); // Assuming you have a method to modify the Livraison in ServiceLivraison
            loadLivraisons(); // Refresh the table
        }
    }

    // Handle the "Détails" button action
    private void handleAfficherDetails(Livraison livraison) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/Livraison/DetailsLivraison.fxml"));
            Parent root = loader.load();

            // Pass the livraison to the details controller
            DetailsLivraisonController controller = loader.getController();
            controller.setLivraison(livraison);

            Stage stage = new Stage();
            stage.setScene(new Scene(root));
            stage.setTitle("Détails de la Livraison");
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handleRetour() {
        try {
            // Charger la vue des commandes
            System.out.println(getClass().getResource("/views/Commande/AfficherCommandes.fxml"));
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/Commande/AfficherCommandes.fxml"));
            Parent root = loader.load();

            // Obtenir la scène actuelle et la rediriger
            Stage stage = (Stage) retourButton.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("Liste des Commandes");
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
