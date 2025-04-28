package tn.esprit.Controllers.produitfront;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;
import tn.esprit.entities.Produit;
import tn.esprit.entities.User;
import tn.esprit.services.ProduitService;
import tn.esprit.tools.SessionManager;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

public class AgriculteurController implements Initializable {

    @FXML private FlowPane productContainer;
    @FXML private Button btnSuivant, btnPrecedent;
    @FXML private Label pageLabel;

    private final ProduitService produitService = new ProduitService();
    private int pageSize = 6;
    private int currentPage = 0;

    private List<Produit> mesProduits = new ArrayList<>();
    private List<Produit> autresProduits = new ArrayList<>();
    private List<Produit> allProduits = new ArrayList<>();

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        chargerTousProduits();
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
            stage.setOnHidden(event -> chargerTousProduits());
            stage.show();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void chargerTousProduits() {
        User user = SessionManager.getInstance().getCurrentUser();
        mesProduits = produitService.getProduitsParUser(user.getId());
        autresProduits = produitService.getProduitsDesAutres(user.getId());

        allProduits = new ArrayList<>();
        allProduits.addAll(mesProduits);
        allProduits.addAll(autresProduits);

        currentPage = 0;
        afficherPage();
    }

    private void afficherPage() {
        productContainer.getChildren().clear();

        int start = currentPage * pageSize;
        int end = Math.min(start + pageSize, allProduits.size());
        List<Produit> produitsPage = allProduits.subList(start, end);

        User currentUser = SessionManager.getInstance().getCurrentUser();

        for (Produit produit : produitsPage) {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/front/produit/ProduitCard.fxml"));
                Pane produitCard = loader.load();
                ProduitCardController controller = loader.getController();
                controller.setProduit(produit);
                controller.setParentController(this);

                if (produit.getUser() != null && produit.getUser().getId() == currentUser.getId()) {
                    controller.afficherMesActions();
                } else {
                    controller.afficherAutresActions();
                }

                productContainer.getChildren().add(produitCard);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        btnPrecedent.setDisable(currentPage == 0);
        btnSuivant.setDisable(end >= allProduits.size());
        pageLabel.setText("Page " + (currentPage + 1));
    }

    @FXML
    private void pageSuivante() {
        if ((currentPage + 1) * pageSize < allProduits.size()) {
            currentPage++;
            afficherPage();
        }
    }

    @FXML
    private void pagePrecedente() {
        if (currentPage > 0) {
            currentPage--;
            afficherPage();
        }
    }

    public void rafraichirProduits() {
        chargerTousProduits();
    }
}
