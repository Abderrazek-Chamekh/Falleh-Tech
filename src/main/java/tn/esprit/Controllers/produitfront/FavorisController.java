package tn.esprit.Controllers.produitfront;

import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import tn.esprit.entities.Favoris;
import tn.esprit.entities.Produit;
import tn.esprit.services.FavorisService;
import tn.esprit.services.PanierService;
import tn.esprit.utils.ImageUtils;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

public class FavorisController implements Initializable {

    @FXML private TableView<Favoris> tableFavoris;
    @FXML private TableColumn<Favoris, ImageView> colImage;
    @FXML private TableColumn<Favoris, String> colNom;
    @FXML private TableColumn<Favoris, String> colPrix;

    @FXML private TableColumn<Favoris, Void> colActions;

    private final FavorisService favorisService = new FavorisService();
    private final PanierService panierService = PanierService.getInstance();

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        tableFavoris.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        colActions.setMaxWidth(140); // Ajusté pour la taille des boutons
        loadFavoris();
    }

    private void loadFavoris() {
        tableFavoris.getItems().clear();
        List<Favoris> favorisList = favorisService.getFavorisParUser(1);

        colImage.setCellValueFactory(data -> {
            Produit produit = data.getValue().getProduit();
            ImageView imageView = new ImageView(ImageUtils.chargerDepuisNom(produit.getImage()));
            imageView.setFitHeight(50);
            imageView.setFitWidth(50);
            imageView.setPreserveRatio(true);
            return new SimpleObjectProperty<>(imageView);
        });

        colNom.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getProduit().getNom()));
        colPrix.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getProduit().getPrix() + " DT"));


        colActions.setCellFactory(param -> new TableCell<>() {
            private final Button btnAddCart = new Button();
            private final Button btnDelete = new Button("🗑");
            private final HBox actionBox = new HBox(8, btnAddCart, btnDelete);

            {
                actionBox.setAlignment(Pos.CENTER_LEFT);

                // ✅ Ajouter une icône panier à btnAddCart
                try {
                    Image image = new Image(getClass().getResourceAsStream("/icons/cart.png"));
                    ImageView imageView = new ImageView(image);
                    imageView.setFitWidth(16);
                    imageView.setFitHeight(16);
                    btnAddCart.setGraphic(imageView);
                } catch (Exception e) {
                    System.err.println("❌ Erreur chargement icône panier: " + e.getMessage());
                    btnAddCart.setText("🛒");  // fallback
                }

                // ✅ Style des boutons
                btnAddCart.getStyleClass().add("add-to-cart-button");

                btnAddCart.setPrefSize(30, 30);

                btnDelete.getStyleClass().add("delete-button");
                btnDelete.setPrefSize(30, 30);

                btnAddCart.setOnAction(event -> {
                    Favoris favoris = getTableView().getItems().get(getIndex());
                    Produit produit = favoris.getProduit();

                    int stock = produit.getStock();

                    if (stock == 0) {
                        Alert alert = new Alert(Alert.AlertType.ERROR);
                        alert.setHeaderText("Rupture de stock");
                        alert.setContentText("Ce produit est actuellement en rupture de stock.");
                        alert.showAndWait();
                        return;
                    }

                    if (stock <= 5) {
                        Alert warning = new Alert(Alert.AlertType.WARNING);
                        warning.setHeaderText("Stock faible");
                        warning.setContentText("⚠ Il ne reste que " + stock + " unités en stock !");
                        warning.showAndWait();
                    }

                    panierService.ajouterProduit(produit, 1); // ajoute 1 unité

                    Alert alert = new Alert(Alert.AlertType.INFORMATION);
                    alert.setHeaderText(null);
                    alert.setContentText(produit.getNom() + " a été ajouté au panier !");
                    alert.showAndWait();
                });



                // ✅ Action: Supprimer
                btnDelete.setOnAction(event -> {
                    Favoris favoris = getTableView().getItems().get(getIndex());
                    favorisService.supprimer(favoris.getId());
                    getTableView().getItems().remove(favoris);
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : actionBox);
            }
        });


        tableFavoris.getItems().addAll(favorisList);
    }
}
