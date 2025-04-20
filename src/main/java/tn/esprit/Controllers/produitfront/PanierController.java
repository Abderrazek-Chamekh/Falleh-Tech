package tn.esprit.Controllers.produitfront;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import tn.esprit.entities.PanierItem;
import tn.esprit.entities.Produit;
import tn.esprit.services.PanierService;
import tn.esprit.utils.ImageUtils;

import java.net.URL;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

public class PanierController implements Initializable {

    @FXML private TableView<PanierItem> tablePanier;
    @FXML private TableColumn<PanierItem, ImageView> colImage;
    @FXML private TableColumn<PanierItem, String> colNom;
    @FXML private TableColumn<PanierItem, Double> colPrix;
    @FXML private TableColumn<PanierItem, Integer> colQuantite;
    @FXML private TableColumn<PanierItem, Double> colTotal;
    @FXML private TableColumn<PanierItem, Void> colAction;
    @FXML private Label totalLabel;

    private final PanierService panierService = PanierService.getInstance();

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        setupColumns();
        chargerPanier();
    }

    private void setupColumns() {
        colImage.setCellValueFactory(cell -> {
            ImageView img = new ImageView(ImageUtils.chargerDepuisNom(cell.getValue().getProduit().getImage()));
            img.setFitWidth(50);
            img.setFitHeight(50);
            img.setPreserveRatio(true);
            return new javafx.beans.property.SimpleObjectProperty<>(img);
        });

        colNom.setCellValueFactory(cell -> new javafx.beans.property.SimpleStringProperty(
                cell.getValue().getProduit().getNom()));

        colPrix.setCellValueFactory(cell -> new javafx.beans.property.SimpleObjectProperty<>(
                cell.getValue().getProduit().getPrix().doubleValue()));

        // ✅ Ajout de boutons - et + dans chaque cellule pour gérer la quantité
        colQuantite.setCellValueFactory(cellData ->
                new javafx.beans.property.SimpleIntegerProperty(cellData.getValue().getQuantite()).asObject());

        colQuantite.setCellFactory(col -> new TableCell<>() {
            private final Button minusBtn = new Button("-");
            private final Button plusBtn = new Button("+");
            private final Label qtyLabel = new Label();
            private final HBox box = new HBox(8, minusBtn, qtyLabel, plusBtn);

            {
                box.setAlignment(Pos.CENTER);
                box.getStyleClass().add("quantity-box");

                minusBtn.getStyleClass().add("quantity-btn");
                plusBtn.getStyleClass().add("quantity-btn");
                qtyLabel.getStyleClass().add("quantity-label");

                minusBtn.setOnAction(e -> updateQuantity(-1));
                plusBtn.setOnAction(e -> updateQuantity(1));
            }

            private void updateQuantity(int delta) {
                PanierItem item = getTableView().getItems().get(getIndex());
                int newQty = item.getQuantite() + delta;
                if (newQty >= 1) {
                    panierService.ajouterProduit(item.getProduit(), delta);
                    item.setQuantite(newQty);
                    item.updateTotal();
                    tablePanier.refresh();
                    updateTotal();
                }
            }

            @Override
            protected void updateItem(Integer quantite, boolean empty) {
                super.updateItem(quantite, empty);
                if (empty || quantite == null) {
                    setGraphic(null);
                } else {
                    qtyLabel.setText(String.valueOf(quantite));
                    setGraphic(box);
                }
            }
        });

        colTotal.setCellValueFactory(cell ->
                new javafx.beans.property.SimpleObjectProperty<>(cell.getValue().getTotal()));

        colAction.setCellFactory(param -> new TableCell<>() {
            private final Button deleteBtn = new Button("🗑");

            {
                deleteBtn.getStyleClass().add("delete-button");
                deleteBtn.setOnAction(event -> {
                    PanierItem item = getTableView().getItems().get(getIndex());
                    panierService.supprimerProduit(item.getProduit().getId());
                    chargerPanier();
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : deleteBtn);
            }
        });
    }

    private void chargerPanier() {
        Map<Produit, Integer> map = panierService.getPanier();
        tablePanier.getItems().setAll(
                map.entrySet().stream()
                        .map(e -> new PanierItem(e.getKey(), e.getValue()))
                        .collect(Collectors.toList())
        );
        updateTotal();
    }

    private void updateTotal() {
        totalLabel.setText(String.format("%.2f DT", panierService.getTotal()));
    }
}
