package tn.esprit.Controllers.produitfront;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ToggleButton;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import tn.esprit.entities.Categorie;
import tn.esprit.entities.Produit;
import tn.esprit.entities.SousCategorie;
import tn.esprit.services.CategorieService;
import tn.esprit.services.FavorisService;
import tn.esprit.services.PanierService;
import tn.esprit.services.ProduitService;
import tn.esprit.utils.ImageUtils;

import java.math.BigDecimal;
import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

public class ProduitFrontViewController implements Initializable {

    @FXML private FlowPane categorieFlowPane;
    @FXML private Label sectionLabel;
    @FXML private Button retourButton;
    @FXML private Label notifLabel;
    private final PanierService panierService = PanierService.getInstance();


    private final CategorieService categorieService = new CategorieService();
    private final ProduitService produitService = new ProduitService();
    private final FavorisService favorisService = new FavorisService();
    private List<Categorie> categories;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        categories = categorieService.getAll();
        afficherCategories();
        retourButton.setOnAction(e -> afficherCategories());
    }

    private void afficherCategories() {
        sectionLabel.setText("🛒 Choisissez une catégorie de produits");
        retourButton.setVisible(false);
        categorieFlowPane.getChildren().clear();

        for (Categorie cat : categories) {
            VBox card = creerCarte(cat.getNom(), cat.getImage(), "category-card");
            int sousCatCount = (cat.getSousCategories() != null) ? cat.getSousCategories().size() : 0;
            Label count = new Label("Sous-catégories : " + sousCatCount);
            count.getStyleClass().add("category-count");
            card.getStyleClass().add("produit-card");
            card.getChildren().add(count);
            card.setOnMouseClicked((MouseEvent e) -> afficherSousCategories(cat));
            categorieFlowPane.getChildren().add(card);
        }
    }

    private void afficherSousCategories(Categorie cat) {
        sectionLabel.setText(" Sous-catégories de : " + cat.getNom());
        retourButton.setVisible(true);
        categorieFlowPane.getChildren().clear();

        List<SousCategorie> sousCategories = categorieService.getSousCategoriesParCategorie(cat.getId());

        for (SousCategorie sc : sousCategories) {
            VBox card = creerCarte(sc.getNom(), sc.getImage(), "category-card");
            card.setOnMouseClicked((MouseEvent e) -> afficherProduits(sc));
            categorieFlowPane.getChildren().add(card);
        }
    }

    private void afficherProduits(SousCategorie sc) {
        sectionLabel.setText("Produits de : " + sc.getNom());
        retourButton.setVisible(true);
        categorieFlowPane.getChildren().clear();

        List<Produit> produits = produitService.getProduitsParSousCategorie(sc.getId());

        for (Produit p : produits) {
            StackPane cardPane = new StackPane();
            VBox card = new VBox(10);
            card.getStyleClass().add("product-card");
            card.setAlignment(Pos.CENTER);

            // === Image ===
            ImageView img = new ImageView(ImageUtils.chargerDepuisNom(p.getImage()));
            img.setFitWidth(150);
            img.setFitHeight(100);
            img.setPreserveRatio(true);

            // === Infos produit ===
            Label name = new Label(p.getNom());
            name.getStyleClass().add("product-name");

            Label desc = new Label(p.getDescription());
            desc.getStyleClass().add("product-desc");

            Label prix = new Label("Prix : " + (p.getPrix() != null ? p.getPrix() : BigDecimal.ZERO) + " DT");
            prix.getStyleClass().add("product-price");

            // === Stock label ===
            Label stock = new Label("Stock : " + p.getStock() + " unités");
            stock.getStyleClass().add("product-stock");

            if (p.getStock() == 0) {
                stock.setStyle("-fx-text-fill: red;");
            } else if (p.getStock() <= 5) {
                stock.setStyle("-fx-text-fill: orange;");
            } else {
                stock.setStyle("-fx-text-fill: green;");
            }

            // === Quantité ===
            Label quantityLabel = new Label("1");
            quantityLabel.getStyleClass().add("quantity-label");

            Button plusBtn = new Button("+");
            Button minusBtn = new Button("-");

            plusBtn.setOnAction(evt -> {
                int q = Integer.parseInt(quantityLabel.getText());
                quantityLabel.setText(String.valueOf(q + 1));
            });

            minusBtn.setOnAction(evt -> {
                int q = Integer.parseInt(quantityLabel.getText());
                if (q > 1) quantityLabel.setText(String.valueOf(q - 1));
            });

            HBox quantityBox = new HBox(5, minusBtn, quantityLabel, plusBtn);
            quantityBox.getStyleClass().add("quantity-box");

            // === Panier ===
            Button ajouter = new Button("Ajouter au panier");
            ajouter.getStyleClass().add("add-to-cart-btn");

            if (p.getStock() == 0) {
                ajouter.setDisable(true);
                ajouter.setText("Rupture de stock");
            }

            ajouter.setOnAction(e -> {
                if (panierService.contient(p)) {
                    showNotification("⚠️ Produit déjà dans le panier !", true);
                } else {
                    int quantite = Integer.parseInt(quantityLabel.getText());
                    panierService.ajouterProduit(p, quantite);
                    showNotification("🛒 Produit ajouté au panier !", false);
                }
            });

            // === Favoris ===
            ToggleButton coeur = new ToggleButton("♡");
            coeur.setSelected(favorisService.existeDansFavoris(p.getId(), 1));
            coeur.getStyleClass().add("heart-button");
            updateHeartIcon(coeur);

            coeur.setOnAction(event -> {
                int userId = 1;
                if (coeur.isSelected()) {
                    favorisService.ajouterFavoris(p.getId(), userId);
                    showNotification("❤️ Produit ajouté aux favoris !", false);
                } else {
                    favorisService.supprimerParProduitEtUser(p.getId(), userId);
                    showNotification("🗑️ Produit retiré des favoris !", true);
                }
                updateHeartIcon(coeur);
            });

            StackPane.setAlignment(coeur, Pos.TOP_RIGHT);
            StackPane.setMargin(coeur, new Insets(10, 10, 0, 0));

            // === Ajout final ===
            card.getChildren().addAll(img, name, desc, prix, stock, quantityBox, ajouter);
            cardPane.getChildren().addAll(card, coeur);
            categorieFlowPane.getChildren().add(cardPane);
        }
    }



    private void updateHeartIcon(ToggleButton coeur) {
        if (coeur.isSelected()) {
            coeur.setText("❤");
        } else {
            coeur.setText("♡");
        }
    }


    private VBox creerCarte(String nom, String imagePath, String styleClass) {
        VBox card = new VBox(10);
        card.getStyleClass().add(styleClass);
        ImageView img = new ImageView();
        img.setImage(ImageUtils.chargerDepuisNom(imagePath));
        img.setFitWidth(150);
        img.setFitHeight(100);
        img.setPreserveRatio(true);

        Label name = new Label(nom);
        name.getStyleClass().add("category-name");

        card.getChildren().addAll(img, name);
        return card;
    }

    private void showNotification(String message, boolean isWarning) {
        notifLabel.setText(message);
        notifLabel.getStyleClass().clear();
        notifLabel.getStyleClass().add("notif-label");
        if (isWarning) notifLabel.getStyleClass().add("warning");

        notifLabel.setVisible(true);

        new Thread(() -> {
            try {
                Thread.sleep(2000);
                javafx.application.Platform.runLater(() -> notifLabel.setVisible(false));
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }).start();
    }
}