package tn.esprit.Controllers.E_Commerce;

import javafx.beans.property.SimpleStringProperty;
import javafx.event.ActionEvent;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import org.kordamp.ikonli.javafx.FontIcon;
import tn.esprit.entities.Commande;
import tn.esprit.services.ServiceCommande;
import tn.esprit.services.UserService;
import org.kordamp.ikonli.fontawesome5.FontAwesomeSolid;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.cell.PropertyValueFactory;
import tn.esprit.tools.SessionManager;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

public class AfficherCommandeController {

    @FXML private TableView<Commande> commandeTableView;
    @FXML private TableColumn<Commande, String> dateCreationColumn;
    @FXML private TableColumn<Commande, String> utilisateurColumn;
    @FXML private TableColumn<Commande, Double> totalColumn;
    @FXML private TableColumn<Commande, String> statutColumn;
    @FXML private TableColumn<Commande, String> adresseLivraisonColumn;
    @FXML private TableColumn<Commande, String> modePaiementColumn;
    @FXML private TableColumn<Commande, String> datePaiementColumn;
    @FXML private TableColumn<Commande, String> statutPaiementColumn;
    @FXML private TableColumn<Commande, Void> actionsColumn = new TableColumn<>("Actions");
    @FXML private TextField searchField;
    @FXML private BorderPane borderPane;
    private UserService userService = new UserService();
    @FXML private Button addCommandeButton;

    private ObservableList<Commande> commandes = FXCollections.observableArrayList();

    @FXML
    private void initialize() throws SQLException {


        // Initialize the columns

        dateCreationColumn.setCellValueFactory(new PropertyValueFactory<>("dateCreation"));
        utilisateurColumn.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().getUser().getName())
        );
        totalColumn.setCellValueFactory(new PropertyValueFactory<>("total"));
        statutColumn.setCellValueFactory(new PropertyValueFactory<>("status"));
        adresseLivraisonColumn.setCellValueFactory(new PropertyValueFactory<>("adresseLivraison"));
        modePaiementColumn.setCellValueFactory(new PropertyValueFactory<>("modePaiement"));
        datePaiementColumn.setCellValueFactory(new PropertyValueFactory<>("datePaiement"));
        statutPaiementColumn.setCellValueFactory(new PropertyValueFactory<>("statusPaiement"));
        actionsColumn.setCellFactory(col -> new TableCell<Commande, Void>() {
            // Icônes
            private final FontIcon iconDelete = new FontIcon(FontAwesomeSolid.TRASH);
            private final FontIcon iconDetails = new FontIcon(FontAwesomeSolid.EYE);
            private final FontIcon iconDelivery = new FontIcon(FontAwesomeSolid.TRUCK);

            // Boutons
            private final Button btnAnnuler = new Button();
            private final Button btnDetails = new Button();
            private final Button btnCamion = new Button();

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

                iconDelivery.setIconColor(Color.web(iconColor));
                iconDelivery.setIconSize(iconSize);

                // Affecter les icônes aux boutons
                btnAnnuler.setGraphic(iconDelete);
                btnDetails.setGraphic(iconDetails);
                btnCamion.setGraphic(iconDelivery);

                // Style des boutons
                String buttonStyle = "-fx-background-color: transparent; -fx-cursor: hand; -fx-padding: 4 6 4 6;";
                btnAnnuler.setStyle(buttonStyle);
                btnDetails.setStyle(buttonStyle);
                btnCamion.setStyle(buttonStyle);

                // Hover effects
                setupButtonHover(btnAnnuler, iconDelete, iconColor, hoverColor);
                setupButtonHover(btnDetails, iconDetails, iconColor, hoverColor);
                setupButtonHover(btnCamion, iconDelivery, iconColor, hoverColor);

                // Actions des boutons
                btnAnnuler.setOnAction(event -> {
                    Commande commande = getTableView().getItems().get(getIndex());
                    handleAnnulerCommande(commande);
                });

                btnDetails.setOnAction(event -> {
                    Commande commande = getTableView().getItems().get(getIndex());
                    handleAfficherDetails(commande);
                });

                btnCamion.setOnAction(event -> {
                    Commande commande = getTableView().getItems().get(getIndex());
                    handlePasserLivraison(commande);
                });
            }

            // Hover effect helper
            private void setupButtonHover(Button button, FontIcon icon, String normalColor, String hoverColor) {
                button.setOnMouseEntered(e -> icon.setIconColor(Color.web(hoverColor)));
                button.setOnMouseExited(e -> icon.setIconColor(Color.web(normalColor)));
            }

            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    HBox hbox = new HBox(10, btnDetails, btnAnnuler, btnCamion);
                    hbox.setAlignment(Pos.CENTER);
                    setGraphic(hbox);
                }
            }
        });

        // Load the data
        loadCommandeData();
        setupSearchFilter();
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
    private void loadCommandeData() {
        ServiceCommande service = new ServiceCommande();
        List<Commande> listeCommandes = service.getAll();

        commandes = FXCollections.observableArrayList(listeCommandes);
        commandeTableView.setItems(commandes);
    }


    @FXML
    private void handleAddCommande(javafx.event.ActionEvent event) {
        try {
            // Load the new scene
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/Commande/AjouterCommande.fxml"));
            Parent root = loader.load();

            // Create a new stage (new window)
            Stage newStage = new Stage();
            newStage.setScene(new Scene(root));

            // Show the new window
            newStage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void handleAnnulerCommande(Commande commande) {
        if (!commande.getStatus().equalsIgnoreCase("Annulée")) {
            commande.setStatus("Annulée");
            new ServiceCommande().modifier(commande);
            loadCommandeData();
        }
    }

    private void handleAfficherDetails(Commande commande) {
        try {
            System.out.println(getClass().getResource("/views/Commande/DetailsCommande.fxml"));
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/Commande/DetailsCommande.fxml"));
            Parent root = loader.load();

            // Passer la commande au contrôleur de la vue des détails
            DetailsCommandeController controller = loader.getController();
            controller.setCommande(commande);

            Stage stage = new Stage();
            stage.setScene(new Scene(root));
            stage.setTitle("Détails de la commande");
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void handlePasserLivraison(Commande commande) {
        if (commande.getStatus().equals("En Attente") ) {
            commande.setStatus("Confirmée");

            new ServiceCommande().modifier(commande);

            loadCommandeData();

            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/Livraison/AjouterLivraison.fxml"));
                Parent root = loader.load();
                AjouterLivraisonController controller = loader.getController();
                controller.setCommande(commande);


                Stage stage = new Stage();
                stage.setScene(new Scene(root));
                stage.setTitle("Ajouter Livraison");
                stage.show();

                Stage currentStage = (Stage) commandeTableView.getScene().getWindow();
                currentStage.close();

            } catch (IOException e) {
                e.printStackTrace();
                showAlert(Alert.AlertType.ERROR, "Erreur", "Erreur de chargement de la page de livraison.");
            }

            showAlert(Alert.AlertType.INFORMATION, "Succès", "Commande passée à la livraison et redirection vers la page de Livraison.");
        } else {
            showAlert(Alert.AlertType.WARNING, "Avertissement", "La commande ne peut pas être passée à la livraison car son statut est " + commande.getStatus() + ".");
        }
    }

    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void setupSearchFilter() {
        // Wrap the ObservableList in a FilteredList
        FilteredList<Commande> filteredData = new FilteredList<>(commandes, p -> true);

        // Add listener to search field
        searchField.textProperty().addListener((observable, oldValue, newValue) -> {
            filteredData.setPredicate(commande -> {
                // If filter text is empty, show all commands
                if (newValue == null || newValue.isEmpty()) {
                    return true;
                }

                // Compare user name with filter text
                String lowerCaseFilter = newValue.toLowerCase();
                String userName = commande.getUser().getName().toLowerCase();

                // Filter matches user name
                return userName.contains(lowerCaseFilter);
            });
        });

        // Wrap the FilteredList in a SortedList
        SortedList<Commande> sortedData = new SortedList<>(filteredData);

        // Bind the SortedList comparator to the TableView comparator
        sortedData.comparatorProperty().bind(commandeTableView.comparatorProperty());

        // Add sorted (and filtered) data to the table
        commandeTableView.setItems(sortedData);
    }

    @FXML
    private void clearSearch() {
        searchField.clear();
    }
    @FXML
    private void handleShowStats(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/Commande/stats.fxml"));
            Parent root = loader.load();

            Stage stage = new Stage();
            stage.setScene(new Scene(root));
            stage.setTitle("Commandes Statistics");
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Error", "Could not load statistics page");
        }
    }
    @FXML
    private void handleGoToLivraison(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/Livraison/AfficherLivraison.fxml"));
            Parent root = loader.load();

            Stage stage = new Stage();
            stage.setTitle("Gestion des Livraisons");
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }



}
