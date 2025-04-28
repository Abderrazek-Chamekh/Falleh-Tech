package tn.esprit.Controllers.E_Commerce;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import tn.esprit.entities.Commande;
import tn.esprit.services.ServiceCommande;
import tn.esprit.utils.SessionUtilisateur;
import org.kordamp.ikonli.javafx.FontIcon;
import org.kordamp.ikonli.fontawesome5.FontAwesomeSolid;

import java.io.IOException;
import java.net.URL;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.ResourceBundle;

public class AfficherCommandeUserController implements Initializable {

    @FXML
    private TableView<Commande> commandeTableView;

    @FXML
    private TableColumn<Commande, Float> totalColumn;

    @FXML
    private TableColumn<Commande, String> statutColumn;

    @FXML
    private TableColumn<Commande, String> adresseLivraisonColumn;

    @FXML
    private TableColumn<Commande, LocalDateTime> dateCreationColumn;

    @FXML
    private TableColumn<Commande, String> modePaiementColumn;

    @FXML
    private TableColumn<Commande, LocalDateTime> datePaiementColumn;

    @FXML
    private TableColumn<Commande, String> statutPaiementColumn;

    @FXML
    private TableColumn<Commande, Void> actionsColumn;

    private final ServiceCommande commandeService = new ServiceCommande();
    private final ObservableList<Commande> commandeList = FXCollections.observableArrayList();

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        setupTableColumns();
        loadCommandes();
    }

    private void setupTableColumns() {
        totalColumn.setCellValueFactory(new PropertyValueFactory<>("total"));
        statutColumn.setCellValueFactory(new PropertyValueFactory<>("status"));
        adresseLivraisonColumn.setCellValueFactory(new PropertyValueFactory<>("adresseLivraison"));
        dateCreationColumn.setCellValueFactory(new PropertyValueFactory<>("dateCreation"));
        modePaiementColumn.setCellValueFactory(new PropertyValueFactory<>("modePaiement"));
        datePaiementColumn.setCellValueFactory(new PropertyValueFactory<>("datePaiement"));
        statutPaiementColumn.setCellValueFactory(new PropertyValueFactory<>("statusPaiement"));

        actionsColumn.setCellFactory(col -> new TableCell<>() {
            private final FontIcon iconDelete = new FontIcon(FontAwesomeSolid.TRASH);
            private final FontIcon iconFacture = new FontIcon(FontAwesomeSolid.FILE_INVOICE);
            private final Button btnAnnuler = new Button();
            private final Button btnFacture = new Button();

            {
                // Configuration icône Supprimer
                iconDelete.setIconColor(Color.web("#2ecc71"));
                iconDelete.setIconSize(16);
                btnAnnuler.setGraphic(iconDelete);
                btnAnnuler.setStyle("-fx-background-color: transparent; -fx-cursor: hand; -fx-padding: 4 6 4 6;");
                btnAnnuler.setOnMouseEntered(e -> iconDelete.setIconColor(Color.web("#27ae60")));
                btnAnnuler.setOnMouseExited(e -> iconDelete.setIconColor(Color.web("#2ecc71")));
                btnAnnuler.setOnAction(event -> {
                    Commande commande = getTableView().getItems().get(getIndex());
                    handleAnnulerCommande(commande);
                });

                // Configuration icône Facture
                iconFacture.setIconColor(Color.web("#3498db")); // Bleu
                iconFacture.setIconSize(16);
                btnFacture.setGraphic(iconFacture);
                btnFacture.setStyle("-fx-background-color: transparent; -fx-cursor: hand; -fx-padding: 4 6 4 6;");
                btnFacture.setOnMouseEntered(e -> iconFacture.setIconColor(Color.web("#2980b9"))); // Bleu foncé
                btnFacture.setOnMouseExited(e -> iconFacture.setIconColor(Color.web("#3498db")));
                btnFacture.setOnAction(event -> {
                    Commande commande = getTableView().getItems().get(getIndex());
                    handleAfficherFacture(commande); // ➕ Appelle ta fonction d'ouverture
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    HBox hbox = new HBox(10, btnFacture, btnAnnuler); // ➕ Ajouter les 2 boutons
                    hbox.setAlignment(Pos.CENTER);
                    setGraphic(hbox);
                }
            }
        });

    }

    private void loadCommandes() {
        int connectedUserId = SessionUtilisateur.getUserId();
        List<Commande> commandes = commandeService.getCommandesByUserId(connectedUserId);

        commandeList.setAll(commandes);
        commandeTableView.setItems(commandeList);
    }

    private void handleAnnulerCommande(Commande commande) {
        if (!commande.getStatus().equalsIgnoreCase("Annulée")) {
            commande.setStatus("Annulée");
            commandeService.modifier(commande);
            loadCommandes(); // Reload data
        }
    }
    private void handleAfficherFacture(Commande commande) {
        try {
            // First load the complete commande with products
            ServiceCommande service = new ServiceCommande();
            Commande completeCommande = service.getCommandeWithProducts(commande.getId());

            System.out.println(getClass().getResource("/views/Commande/Facture.fxml"));
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/Commande/facture.fxml"));
            Parent root = loader.load();

            // Pass the complete commande with products to the controller
            FactureController controller = loader.getController();
            controller.setCommande(completeCommande);

            Stage stage = new Stage();
            stage.setScene(new Scene(root));
            stage.setTitle("Facture - Commande #" + completeCommande.getId());
            stage.show();

            // Debug output
            System.out.println("Displaying facture for commande ID: " + completeCommande.getId());
            if (completeCommande.getCommandeProduits() != null) {
                System.out.println("Number of products in commande: " + completeCommande.getCommandeProduits().size());
                completeCommande.getCommandeProduits().forEach(cp ->
                        System.out.println("Product: " + cp.getProduit().getNom() +
                                " - Qty: " + cp.getQuantite() +
                                " - Price: " + cp.getPrixUnitaire()));
            } else {
                System.out.println("No products found for this commande");
            }
        } catch (IOException e) {
            e.printStackTrace();
            showAlert("Erreur", "Impossible d'afficher la facture", Alert.AlertType.ERROR);
        }
    }

    private void showAlert(String title, String message, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }


}
