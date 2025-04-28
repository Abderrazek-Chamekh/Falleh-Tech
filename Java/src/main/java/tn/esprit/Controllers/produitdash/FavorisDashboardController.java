package tn.esprit.Controllers.produitdash;

import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.image.ImageView;
import tn.esprit.entities.Favoris;
import tn.esprit.entities.Produit;
import tn.esprit.services.FavorisService;
import tn.esprit.utils.ImageUtils;

import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

public class FavorisDashboardController implements Initializable {

    @FXML private TableView<Favoris> tableFavoris;
    @FXML private TableColumn<Favoris, Integer> colUserId;
    @FXML private TableColumn<Favoris, ImageView> colImage;
    @FXML private TableColumn<Favoris, String> colNom;
    @FXML private TableColumn<Favoris, String> colPrix;
    @FXML private TableColumn<Favoris, Integer> colStock;

    private final FavorisService favorisService = new FavorisService();

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        List<Favoris> allFavoris = favorisService.getAllFavoris();



        colImage.setCellValueFactory(data -> {
            Produit produit = data.getValue().getProduit();
            ImageView imageView = new ImageView(ImageUtils.chargerDepuisNom(produit.getImage()));
            imageView.setFitHeight(40);
            imageView.setFitWidth(40);
            imageView.setPreserveRatio(true);
            return new SimpleObjectProperty<>(imageView);
        });

        colNom.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getProduit().getNom()));
        colPrix.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getProduit().getPrix() + " DT"));
        colStock.setCellValueFactory(data -> new SimpleObjectProperty<>(data.getValue().getProduit().getStock()));

        tableFavoris.getItems().addAll(allFavoris);
    }
}
