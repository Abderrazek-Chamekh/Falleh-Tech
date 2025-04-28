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
import tn.esprit.services.CategorieService;
import tn.esprit.utils.ImageUtils;
import tn.esprit.Controllers.produitfront.AjouterCategorieController;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

public class CategorieController implements Initializable {

    @FXML private TableView<Categorie> tableView;

    @FXML private TableColumn<Categorie, String> nomCol;
    @FXML private TableColumn<Categorie, String> imageCol;
    @FXML private TableColumn<Categorie, Void> actionCol;
    @FXML private Label notifLabel;
    @FXML private TextField nomField;
    @FXML private ImageView imagePreview;
    @FXML
    private TextField searchFieldCategorie;

    private final CategorieService service = new CategorieService();
    private Categorie categorieEnCoursEdition = null;
    private String selectedImageFilename;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        searchFieldCategorie.textProperty().addListener((observable, oldValue, newValue) -> {
            filtrerCategoriesParNom(newValue);
        });


        nomCol.setCellValueFactory(new PropertyValueFactory<>("nom"));
        imageCol.setCellValueFactory(new PropertyValueFactory<>("image"));

        // Affichage des images dans la colonne image
        imageCol.setCellFactory(col -> new TableCell<>() {
            private final ImageView imageView = new ImageView();

            {
                imageView.setFitWidth(80);
                imageView.setFitHeight(60);
                imageView.setPreserveRatio(true);
            }

            @Override
            protected void updateItem(String filename, boolean empty) {
                super.updateItem(filename, empty);
                if (empty || filename == null || filename.isBlank()) {
                    setGraphic(null);
                } else {
                    imageView.setImage(ImageUtils.chargerDepuisNom(filename));
                    setGraphic(imageView);
                }
            }
        });

        ajusterColonnes();
        setupActionColumn();
        afficherCategories();
        notifLabel.setVisible(false);
    }

    private void ajusterColonnes() {
        tableView.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        nomCol.setMaxWidth(1f * Integer.MAX_VALUE * 0.30);
        imageCol.setMaxWidth(1f * Integer.MAX_VALUE * 0.30);
        actionCol.setMaxWidth(1f * Integer.MAX_VALUE * 0.30);
    }

    public void afficherCategories() {
        tableView.getItems().setAll(service.getAll());
    }

    @FXML
    public void ajouterCategorie() {
        String nom = nomField.getText().trim();

        if (nom.isEmpty()) {
            showNotification("⚠ Le nom est requis.", true);
            nomField.requestFocus();
            return;
        }

        if (nom.length() < 3 || nom.matches("\\d+")) {
            showNotification("⚠ Le nom doit contenir au moins 3 lettres et ne pas être uniquement des chiffres.", true);
            nomField.requestFocus();
            return;
        }

        boolean existe = service.getAll().stream()
                .anyMatch(c -> c.getNom().equalsIgnoreCase(nom) &&
                        (categorieEnCoursEdition == null || !c.getId().equals(categorieEnCoursEdition.getId())));

        if (existe) {
            showNotification("⚠ Une catégorie avec ce nom existe déjà !", true);
            nomField.requestFocus();
            return;
        }

        if (selectedImageFilename == null || selectedImageFilename.isBlank()) {
            showNotification("⚠ Veuillez sélectionner une image.", true);
            return;
        }

        if (categorieEnCoursEdition == null) {
            Categorie c = new Categorie(nom, selectedImageFilename);
            service.ajouter(c);
            showNotification("✅ Catégorie ajoutée avec succès !", false);
        } else {
            categorieEnCoursEdition.setNom(nom);
            categorieEnCoursEdition.setImage(selectedImageFilename);
            service.modifier(categorieEnCoursEdition);
            showNotification("✏ Catégorie modifiée avec succès !", false);
            categorieEnCoursEdition = null;
        }

        clearFields();
        afficherCategories();
    }

    @FXML
    public void choisirImage() {
        File file = ImageUtils.ouvrirEtCopierImage();
        if (file != null) {
            selectedImageFilename = file.getName();
            imagePreview.setImage(ImageUtils.chargerDepuisNom(selectedImageFilename));
        }
    }

    @FXML
    public void prendrePhoto() {
        File file = ImageUtils.prendrePhotoDepuisWebcam();
        if (file != null) {
            selectedImageFilename = file.getName();
            imagePreview.setImage(ImageUtils.chargerDepuisNom(selectedImageFilename));
        }
    }

    private void clearFields() {
        nomField.clear();
        imagePreview.setImage(null);
        selectedImageFilename = null;
        categorieEnCoursEdition = null;
    }

    private void setupActionColumn() {
        actionCol.setCellFactory(col -> new TableCell<>() {
            private final Button btnEdit = new Button();
            private final Button btnDelete = new Button();
            private final HBox hbox = new HBox(10, btnEdit, btnDelete);
            private final ImageView editIcon;
            private final ImageView deleteIcon;

            {
                URL editUrl = getClass().getResource("/icons/modifier.png");
                URL deleteUrl = getClass().getResource("/icons/delete.png");

                editIcon = (editUrl != null) ? new ImageView(new Image(editUrl.toExternalForm())) : new ImageView();
                deleteIcon = (deleteUrl != null) ? new ImageView(new Image(deleteUrl.toExternalForm())) : new ImageView();

                editIcon.setFitWidth(18);
                editIcon.setFitHeight(18);
                deleteIcon.setFitWidth(18);
                deleteIcon.setFitHeight(18);

                btnEdit.setGraphic(editIcon);
                btnDelete.setGraphic(deleteIcon);
                btnEdit.getStyleClass().add("action-button");
                btnDelete.getStyleClass().add("action-button");

                btnEdit.setOnAction(event -> {
                    Categorie selected = getTableView().getItems().get(getIndex());

                    try {
                        FXMLLoader loader = new FXMLLoader(getClass().getResource("/front/produit/ajouterCategorie.fxml"));
                        Parent root = loader.load();

                        // 🔄 Charger les données dans le contrôleur du popup
                        AjouterCategorieController controller = loader.getController();
                        controller.setCategorie(selected);  // Pré-remplir le formulaire

                        Stage stage = new Stage();
                        stage.setTitle("Modifier une Catégorie");
                        stage.initModality(Modality.APPLICATION_MODAL);
                        stage.setScene(new Scene(root));
                        stage.showAndWait();

                        afficherCategories();  // 🔁 Rafraîchir la table après modification
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                });


                btnDelete.setOnAction(event -> {
                    Categorie selected = getTableView().getItems().get(getIndex());
                    service.supprimer(selected);
                    afficherCategories();
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
                Thread.currentThread().interrupt();
            }
        }).start();
    }
    @FXML
    public void ouvrirPopupAjout() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/front/produit/ajouterCategorie.fxml"));
            Parent root = loader.load();

            Stage stage = new Stage();
            stage.setTitle("Ajouter une Catégorie");
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setScene(new Scene(root));
            stage.showAndWait();

            // Après fermeture, rafraîchir la liste
            afficherCategories();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    private void filtrerCategoriesParNom(String nomRecherche) {
        final String filtre = (nomRecherche != null) ? nomRecherche.toLowerCase() : "";

        List<Categorie> toutesCategories = service.getAll();
        List<Categorie> resultat = toutesCategories.stream()
                .filter(c -> c.getNom() != null && c.getNom().toLowerCase().contains(filtre))
                .collect(Collectors.toList());

        tableView.setItems(FXCollections.observableArrayList(resultat));
    }



}
