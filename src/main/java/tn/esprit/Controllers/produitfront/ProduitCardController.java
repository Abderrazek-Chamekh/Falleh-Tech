package tn.esprit.Controllers.produitfront;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import tn.esprit.entities.Produit;
import tn.esprit.services.ProduitService;
import tn.esprit.utils.ImageUtils;

import java.io.IOException;

public class ProduitCardController {

    @FXML private ImageView imageProduit;
    @FXML private Label nomProduit;
    @FXML private Label descriptionProduit;
    @FXML private Label prixProduit;
    @FXML private Label categorieProduit;
    @FXML private Label sousCategorieProduit;
    @FXML private Label stockProduit;
    @FXML private Button modifierButton;
    @FXML private Button supprimerButton;
    private AgriculteurController parentController;
    private Produit produit;

    public void setProduit(Produit p) {
        this.produit = p;

        nomProduit.setText(p.getNom());
        descriptionProduit.setText(tronquerTexte(p.getDescription(), 60));
        prixProduit.setText("Prix : " + p.getPrix() + " DT");
        categorieProduit.setText("Catégorie : " + p.getCategorie().getNom());
        sousCategorieProduit.setText("Sous-Catégorie : " + p.getSousCategorie().getNom());

        // ✅ Couleur dynamique selon le stock
        int stock = p.getStock();
        String couleur;
        if (stock > 10) {
            couleur = "#28a745"; // vert
        } else if (stock > 0) {
            couleur = "#ffc107"; // jaune
        } else {
            couleur = "#dc3545"; // rouge
        }
        stockProduit.setText("Stock : " + stock + " unités");
        stockProduit.setStyle("-fx-background-color: " + couleur + "; -fx-text-fill: white; -fx-padding: 3 8 3 8; -fx-background-radius: 6;");

        if (p.getImage() != null && !p.getImage().isEmpty()) {
            imageProduit.setImage(ImageUtils.chargerDepuisNom(p.getImage()));
        }
    }

    private String tronquerTexte(String texte, int maxLongueur) {
        if (texte.length() <= maxLongueur) return texte;
        return texte.substring(0, maxLongueur) + "...";
    }
    public void setParentController(AgriculteurController controller) {
        this.parentController = controller;
    }
    @FXML
    private void onModifierClicked() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/front/produit/ajouterproduit.fxml"));
            Parent root = loader.load();

            AjouterProduitController controller = loader.getController();
            controller.setProduit(produit); // pré-remplir

            Stage stage = new Stage();
            stage.setTitle("Modifier Produit");
            stage.setScene(new Scene(root));
            stage.setResizable(false);
            stage.showAndWait(); // ⏳ Attendre que la modale se ferme

            // ✅ Recharger les données affichées dans la carte après modification
            ProduitService service = new ProduitService();
            Produit updated = service.getById(produit.getId()); // ou getAll() si pas de méthode getById()
            this.setProduit(updated); // met à jour les champs visuels

        } catch (IOException e) {
            e.printStackTrace();
        }
    }



    @FXML
    private void onSupprimerClicked() {
        Alert confirmation = new Alert(AlertType.CONFIRMATION);
        confirmation.setTitle("Suppression");
        confirmation.setHeaderText("Supprimer ce produit ?");
        confirmation.setContentText("Cette action est irréversible.");

        confirmation.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                ProduitService produitService = new ProduitService();
                produitService.supprimer(produit);  // ✅ On passe l'objet entier

                // ✅ Supprimer la carte du FlowPane
                VBox carte = (VBox) supprimerButton.getParent().getParent(); // bouton → HBox → VBox
                ((FlowPane) carte.getParent()).getChildren().remove(carte);
            }
        });
    }

}
