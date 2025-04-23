package tn.esprit.Controllers.produitfront;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;
import tn.esprit.entities.Produit;
import tn.esprit.entities.User;
import tn.esprit.services.ProduitService;
import tn.esprit.tools.SessionManager;

import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

public class AgriculteurController implements Initializable {

    @FXML
    private FlowPane productContainer;

    private final ProduitService produitService = new ProduitService();

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        rafraichirProduits();
    }

    @FXML
    private void ajouterProduitPopup() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/front/produit/AjouterProduit.fxml"));
            Parent root = loader.load();

            Stage stage = new Stage();
            stage.setTitle("Ajouter un Produit");
            stage.setScene(new Scene(root));
            stage.setResizable(false);
            stage.setOnHidden(event -> rafraichirProduits());
            stage.show();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void rafraichirProduits() {
        productContainer.getChildren().clear();
        User user = SessionManager.getInstance().getCurrentUser();
        List<Produit> produits = produitService.getProduitsParUser(user.getId());

        for (Produit produit : produits) {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/front/produit/ProduitCard.fxml"));
                Pane produitCard = loader.load();
                ProduitCardController controller = loader.getController();
                controller.setProduit(produit);
                controller.setParentController(this); // pour pouvoir rafraîchir depuis carte
                productContainer.getChildren().add(produitCard);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
