package tn.esprit.Controllers.produitfront;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import tn.esprit.entities.Produit;
import tn.esprit.entities.User;
import tn.esprit.services.FavorisService;
import tn.esprit.services.ProduitService;
import tn.esprit.tools.SessionManager;
import tn.esprit.utils.ImageUtils;

import java.io.IOException;
import java.math.BigDecimal;

public class ProduitCardController {

    @FXML private StackPane cardStackPane;
    @FXML private VBox cardVBox;
    @FXML private ImageView imageProduit;
    @FXML private Label nomProduit;
    @FXML private Label descriptionProduit;
    @FXML private Label prixProduit;
    @FXML private Label stockProduit;
    @FXML private ToggleButton favorisButton;
    @FXML private HBox quantiteContainer;
    @FXML private Button minusBtn;
    @FXML private Button plusBtn;
    @FXML private Label quantityLabel;
    @FXML private Button ajouterPanierButton;

    private Produit produit;
    private final FavorisService favorisService = new FavorisService();
    private AgriculteurController parentController;

    public void setProduit(Produit p) {
        this.produit = p;

        if (p.getImage() != null && !p.getImage().isEmpty()) {
            imageProduit.setImage(ImageUtils.chargerDepuisNom(p.getImage()));
        }

        nomProduit.setText(p.getNom());
        descriptionProduit.setText(tronquerTexte(p.getDescription(), 60));
        prixProduit.setText("Prix : " + (p.getPrix() != null ? p.getPrix() : BigDecimal.ZERO) + " DT");

        int stock = p.getStock();
        stockProduit.setText("Stock : " + stock + " unités");

        if (stock > 10) {
            stockProduit.setStyle("-fx-text-fill: green;");
        } else if (stock > 0) {
            stockProduit.setStyle("-fx-text-fill: orange;");
        } else {
            stockProduit.setStyle("-fx-text-fill: red;");
        }

        // Initialiser les événements des boutons
        setupQuantityControls();
        setupFavorisButton();
        setupAjouterPanierButton();
    }

    private void setupQuantityControls() {
        minusBtn.setOnAction(event -> {
            int qty = Integer.parseInt(quantityLabel.getText());
            if (qty > 1) quantityLabel.setText(String.valueOf(qty - 1));
        });

        plusBtn.setOnAction(event -> {
            int qty = Integer.parseInt(quantityLabel.getText());
            quantityLabel.setText(String.valueOf(qty + 1));
        });
    }

    private void setupFavorisButton() {
        favorisButton.setOnAction(event -> toggleFavoris(favorisButton));
    }

    private void setupAjouterPanierButton() {
        ajouterPanierButton.setOnAction(event -> ajouterAuPanier(Integer.parseInt(quantityLabel.getText())));
    }

    public void afficherMesActions() {
        favorisButton.setVisible(false);
        quantiteContainer.setVisible(false);
        ajouterPanierButton.setVisible(false);

        Button modifierButton = new Button("✏ Modifier");
        modifierButton.getStyleClass().add("modifier-btn");

        Button supprimerButton = new Button("🗑 Supprimer");
        supprimerButton.getStyleClass().add("supprimer-btn");


        modifierButton.setOnAction(event -> onModifierClicked());
        supprimerButton.setOnAction(event -> onSupprimerClicked());

        // Ajouter les boutons à la carte
        cardVBox.getChildren().addAll(modifierButton, supprimerButton);
    }

    public void afficherAutresActions() {
        favorisButton.setVisible(true);
        quantiteContainer.setVisible(true);
        ajouterPanierButton.setVisible(true);
    }

    private void onModifierClicked() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/front/produit/ajouterproduit.fxml"));
            Parent root = loader.load();

            AjouterProduitController controller = loader.getController();
            controller.setProduit(produit);

            Stage stage = new Stage();
            stage.setTitle("Modifier Produit");
            stage.setScene(new Scene(root));
            stage.setResizable(false);
            stage.showAndWait();

            ProduitService service = new ProduitService();
            Produit updated = service.getById(produit.getId());
            this.setProduit(updated);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void onSupprimerClicked() {
        Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION);
        confirmation.setTitle("Suppression");
        confirmation.setHeaderText("Supprimer ce produit ?");
        confirmation.setContentText("Cette action est irréversible.");

        confirmation.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                ProduitService produitService = new ProduitService();
                produitService.supprimer(produit);

                VBox carte = (VBox) cardVBox.getParent();
                ((javafx.scene.layout.FlowPane) carte.getParent()).getChildren().remove(carte);

                if (parentController != null) {
                    parentController.rafraichirProduits();
                }
            }
        });
    }

    private void ajouterAuPanier(int quantite) {
        tn.esprit.services.PanierService.getInstance().ajouterProduit(produit, quantite);
        new Alert(Alert.AlertType.INFORMATION, "Produit ajouté au panier 🛒").show();
    }

    private void toggleFavoris(ToggleButton button) {
        int userId = SessionManager.getInstance().getCurrentUser().getId();
        if (button.isSelected()) {
            favorisService.ajouterFavoris(produit.getId(), userId);
            button.setText("❤");
            new Alert(Alert.AlertType.INFORMATION, "Produit ajouté aux favoris ❤️").show();
        } else {
            favorisService.supprimerParProduitEtUser(produit.getId(), userId);
            button.setText("♡");
            new Alert(Alert.AlertType.INFORMATION, "Produit retiré des favoris 💔").show();
        }
    }

    private String tronquerTexte(String texte, int maxLongueur) {
        if (texte.length() <= maxLongueur) return texte;
        return texte.substring(0, maxLongueur) + "...";
    }

    public void setParentController(AgriculteurController controller) {
        this.parentController = controller;
    }
}
