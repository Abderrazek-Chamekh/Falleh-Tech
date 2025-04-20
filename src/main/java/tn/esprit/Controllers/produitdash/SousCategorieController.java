package tn.esprit.Controllers.produitdash;

import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import tn.esprit.entities.Categorie;
import tn.esprit.entities.SousCategorie;
import tn.esprit.services.CategorieService;
import tn.esprit.services.ProduitService;
import tn.esprit.services.SousCategorieService;
import tn.esprit.utils.ImageUtils;
import tn.esprit.Controllers.produitfront.AjouterSousCategorieController;

import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

public class SousCategorieController implements Initializable {

    @FXML private TextField nomField;
    @FXML private TableView<SousCategorie> tableView;

    @FXML private TableColumn<SousCategorie, String> nomCol;
    @FXML private TableColumn<SousCategorie, String> categorieCol;
    @FXML private TableColumn<SousCategorie, String> imageCol;
    @FXML private TableColumn<SousCategorie, Void> actionCol;
    @FXML private ImageView imagePreview;
    @FXML private Label notifLabel;
    @FXML private TextField searchFieldSousCategorie;

    private final SousCategorieService sousCategorieService = new SousCategorieService();
    private final ProduitService produitService = new ProduitService();

    private String selectedImageName;
    private SousCategorie sousCategorieEnCoursEdition;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        setupColonnes();
        setupFiltrage();
        setupActionColumn();
        afficherSousCategories();
    }

    private void setupColonnes() {

        nomCol.setCellValueFactory(new PropertyValueFactory<>("nom"));

        categorieCol.setCellValueFactory(cellData -> {
            Categorie cat = cellData.getValue().getCategorie();
            return new ReadOnlyObjectWrapper<>(cat != null ? cat.getNom() : "Aucune");
        });

        imageCol.setCellValueFactory(new PropertyValueFactory<>("image"));
        imageCol.setCellFactory(param -> new TableCell<>() {
            private final ImageView imageView = new ImageView();
            {
                imageView.setFitWidth(80);
                imageView.setFitHeight(60);
                imageView.setPreserveRatio(true);
            }

            @Override
            protected void updateItem(String imageName, boolean empty) {
                super.updateItem(imageName, empty);
                if (empty || imageName == null || imageName.isBlank()) {
                    setGraphic(null);
                } else {
                    imageView.setImage(ImageUtils.chargerDepuisNom(imageName));
                    setGraphic(imageView);
                }
            }
        });

        tableView.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
     
        nomCol.setMaxWidth(1f * Integer.MAX_VALUE * 0.25);
        categorieCol.setMaxWidth(1f * Integer.MAX_VALUE * 0.25);
        imageCol.setMaxWidth(1f * Integer.MAX_VALUE * 0.20);
        actionCol.setMaxWidth(1f * Integer.MAX_VALUE * 0.20);
    }

    private void setupFiltrage() {
        searchFieldSousCategorie.textProperty().addListener((obs, oldVal, newVal) -> {
            filtrerSousCategoriesParNom(newVal);
        });
    }

    private void setupActionColumn() {
        actionCol.setCellFactory(col -> new TableCell<>() {
            private final Button btnEdit = new Button();
            private final Button btnDelete = new Button();
            private final HBox hbox = new HBox(10, btnEdit, btnDelete);

            {
                btnEdit.setGraphic(new ImageView(new Image(getClass().getResource("/icons/modifier.png").toExternalForm(), 18, 18, true, true)));
                btnDelete.setGraphic(new ImageView(new Image(getClass().getResource("/icons/delete.png").toExternalForm(), 18, 18, true, true)));

                btnEdit.getStyleClass().add("action-button");
                btnDelete.getStyleClass().add("action-button");

                btnEdit.setOnAction(event -> {
                    SousCategorie selected = getTableView().getItems().get(getIndex());
                    ouvrirPopupEdition(selected);
                });

                btnDelete.setOnAction(event -> {
                    SousCategorie selected = getTableView().getItems().get(getIndex());
                    if (produitService.existsBySousCategorie(selected.getId())) {
                        showNotification("Impossible de supprimer : liée à des produits.", true);
                    } else {
                        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION,
                                "Supprimer cette sous-catégorie ?", ButtonType.YES, ButtonType.NO);
                        confirm.setHeaderText("Confirmation");
                        confirm.showAndWait().ifPresent(response -> {
                            if (response == ButtonType.YES) {
                                sousCategorieService.supprimer(selected);
                                afficherSousCategories();
                                showNotification("Sous-catégorie supprimée avec succès.", false);
                            }
                        });
                    }
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : hbox);
            }
        });

        actionCol.setCellValueFactory(param -> new ReadOnlyObjectWrapper<>(null));
    }

    private void showNotification(String message, boolean isWarning) {
        notifLabel.setText(message);
        notifLabel.getStyleClass().removeAll("notif-label", "warning");
        notifLabel.getStyleClass().add("notif-label");
        if (isWarning) notifLabel.getStyleClass().add("warning");
        notifLabel.setVisible(true);

        new Thread(() -> {
            try {
                Thread.sleep(3000);
                javafx.application.Platform.runLater(() -> notifLabel.setVisible(false));
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }).start();
    }


    @FXML
    private void ouvrirPopupAjout() {
        ouvrirPopupEdition(null);
    }

    public void ouvrirPopupEdition(SousCategorie sousCategorie) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/front/produit/ajoutersouscat.fxml"));
            Parent root = loader.load();

            AjouterSousCategorieController controller = loader.getController();
            if (sousCategorie != null) controller.setSousCategorie(sousCategorie);

            Stage stage = new Stage();
            stage.setTitle(sousCategorie == null ? "Ajouter une Sous-Catégorie" : "Modifier une Sous-Catégorie");
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setScene(new Scene(root));
            stage.showAndWait();

            afficherSousCategories();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void afficherSousCategories() {
        tableView.getItems().setAll(sousCategorieService.getAll());
    }

    private void filtrerSousCategoriesParNom(String search) {
        List<SousCategorie> all = sousCategorieService.getAll();
        List<SousCategorie> filtered = all.stream()
                .filter(sc -> sc.getNom().toLowerCase().contains(search.toLowerCase()))
                .collect(Collectors.toList());
        tableView.setItems(FXCollections.observableArrayList(filtered));
    }
}
