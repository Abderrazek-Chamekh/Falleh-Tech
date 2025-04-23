package tn.esprit.Controllers.produitfront;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
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
    @FXML private Label nomProduit, descriptionProduit, prixProduit, categorieProduit, sousCategorieProduit, stockProduit;
    @FXML private Button modifierButton, supprimerButton;

    private Produit produit;
    private AgriculteurController parentController;

    public void setProduit(Produit p) {
        this.produit = p;

        nomProduit.setText(p.getNom());
        descriptionProduit.setText(tronquerTexte(p.getDescription(), 60));
        prixProduit.setText("Prix : " + p.getPrix() + " DT");
        categorieProduit.setText("Catégorie : " + p.getCategorie().getNom());
        sousCategorieProduit.setText("Sous-Catégorie : " + p.getSousCategorie().getNom());

        int stock = p.getStock();
        String couleur = stock > 10 ? "#28a745" : (stock > 0 ? "#ffc107" : "#dc3545");

        stockProduit.setText("Stock : " + stock + " unités");
        stockProduit.setStyle("-fx-background-color: " + couleur + "; -fx-text-fill: white; -fx-padding: 3 8 3 8; -fx-background-radius: 6;");

        if (p.getImage() != null && !p.getImage().isEmpty()) {
            imageProduit.setImage(ImageUtils.chargerDepuisNom(p.getImage()));
        }
    }

    public void setParentController(AgriculteurController controller) {
        this.parentController = controller;
    }

    @FXML
    private void onModifierClicked() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/front/produit/AjouterProduit.fxml"));
            Parent root = loader.load();

            AjouterProduitController controller = loader.getController();
            controller.setProduit(produit);

            Stage stage = new Stage();
            stage.setTitle("Modifier Produit");
            stage.setScene(new Scene(root));
            stage.setResizable(false);

            // Quand la modale se ferme, on recharge toute la liste
            stage.setOnHidden(e -> parentController.rafraichirProduits());
            stage.show();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void onSupprimerClicked() {
        Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION);
        confirmation.setTitle("Suppression");
        confirmation.setHeaderText("Supprimer ce produit ?");
        confirmation.setContentText("Cette action est irréversible.");

        confirmation.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                ProduitService produitService = new ProduitService();
                produitService.supprimer(produit);

                parentController.rafraichirProduits(); // recharge la liste
            }
        });
    }

    private String tronquerTexte(String texte, int max) {
        return texte.length() <= max ? texte : texte.substring(0, max) + "...";
    }
}
