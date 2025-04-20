package tn.esprit.Controllers.produitfront;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.stage.Stage;
import javafx.util.Duration;
import tn.esprit.entities.Categorie;
import tn.esprit.entities.Produit;
import tn.esprit.entities.SousCategorie;
import tn.esprit.services.CategorieService;
import tn.esprit.services.ProduitService;
import tn.esprit.services.SousCategorieService;
import tn.esprit.utils.ImageUtils;

import java.io.File;
import java.math.BigDecimal;
import java.util.List;

public class AjouterProduitController {

    @FXML private TextField nomField, prixField, stockField, descriptionField;
    @FXML private ComboBox<Categorie> categorieComboBox;
    @FXML private ComboBox<SousCategorie> sousCategorieComboBox;
    @FXML private ImageView imagePreview;

    @FXML private Label nomError, prixError, stockError, descriptionError, categorieError, imageError;

    private final ProduitService produitService = new ProduitService();
    private final CategorieService categorieService = new CategorieService();
    private final SousCategorieService sousCategorieService = new SousCategorieService();

    private String selectedImage;
    private Produit produitEnEdition;

    @FXML
    public void initialize() {
        Tooltip prixTooltip = new Tooltip("Exemple : 4.50 DT (minimum 3 DT)");
        prixTooltip.setShowDelay(Duration.millis(300));
        prixTooltip.setShowDuration(Duration.seconds(15));
        prixTooltip.setHideDelay(Duration.millis(300));
        prixField.setTooltip(prixTooltip);

        List<Categorie> categories = categorieService.getAll();
        categorieComboBox.getItems().addAll(categories);

        categorieComboBox.setOnAction(e -> {
            Categorie selected = categorieComboBox.getValue();
            sousCategorieComboBox.getItems().clear();
            if (selected != null) {
                List<SousCategorie> sousCategories = sousCategorieService.getByCategorie(selected.getId());
                sousCategorieComboBox.getItems().setAll(sousCategories);
            }
        });

        prixField.setTextFormatter(new TextFormatter<>(change -> {
            return change.getControlNewText().matches("^\\d*(\\.\\d{0,2})?$") ? change : null;
        }));

        stockField.setTextFormatter(new TextFormatter<>(change -> {
            return change.getControlNewText().matches("\\d{0,5}") ? change : null;
        }));

        descriptionField.setTextFormatter(new TextFormatter<>(change -> {
            return change.getControlNewText().matches("[a-zA-Z0-9\\s.,;:'\"!?()-]*") ? change : null;
        }));
    }

    public void setProduit(Produit p) {
        this.produitEnEdition = p;
        nomField.setText(p.getNom());
        prixField.setText(p.getPrix().toPlainString());
        stockField.setText(String.valueOf(p.getStock()));
        descriptionField.setText(p.getDescription() != null ? p.getDescription() : "Aucune description.");
        selectedImage = p.getImage();
        if (selectedImage != null && !selectedImage.isBlank()) {
            imagePreview.setImage(ImageUtils.chargerDepuisNom(selectedImage));
        }
        categorieComboBox.setValue(p.getCategorie());
        categorieComboBox.getOnAction().handle(null);
        sousCategorieComboBox.setValue(p.getSousCategorie());
    }

    @FXML
    public void ajouterProduit() {
        // Réinitialiser erreurs
        resetError(nomField, nomError);
        resetError(prixField, prixError);
        resetError(stockField, stockError);
        resetError(descriptionField, descriptionError);
        resetError(categorieComboBox, categorieError);
        resetError(imageError);

        boolean valid = true;
        BigDecimal prix = BigDecimal.ZERO;
        int stock = 0;

        String nom = nomField.getText().trim();
        String prixStr = prixField.getText().trim();
        String stockStr = stockField.getText().trim();
        String desc = descriptionField.getText().trim();
        Categorie cat = categorieComboBox.getValue();
        SousCategorie sousCat = sousCategorieComboBox.getValue();

        // Nom
        if (nom.isEmpty() || nom.length() < 3) {
            showError(nomField, nomError, "⚠ Le nom est requis et doit contenir au moins 3 lettres.");
            return;
        }

        // Prix
        try {
            prix = new BigDecimal(prixStr);
            if (prix.compareTo(new BigDecimal("3")) < 0) throw new NumberFormatException();
        } catch (NumberFormatException e) {
            showError(prixField, prixError, "⚠ Prix invalide (minimum 3 DT).");
            return;
        }

        // Stock
        try {
            stock = Integer.parseInt(stockStr);
            if (stock < 0) throw new NumberFormatException();
        } catch (NumberFormatException e) {
            showError(stockField, stockError, "⚠ Stock invalide.");
            return;
        }

        // Description
        if (desc.length() < 7) {
            showError(descriptionField, descriptionError, "⚠ La description doit contenir au moins 7 caractères.");
            return;
        }

        // Catégorie
        if (cat == null || sousCat == null) {
            showError(categorieComboBox, categorieError, "⚠ Catégorie et sous-catégorie requises.");
            return;
        }

        // Image
        if (selectedImage == null || selectedImage.isBlank()) {
            showError(imageError, "⚠ Veuillez choisir une image.");
            return;
        }

        // Ajouter ou modifier
        if (produitEnEdition == null) {
            Produit produit = new Produit();
            produit.setNom(nom);
            produit.setDescription(desc);
            produit.setPrix(prix);
            produit.setStock(stock);
            produit.setImage(selectedImage);
            produit.setCategorie(cat);
            produit.setSousCategorie(sousCat);
            produit.setUpdatedAt(java.time.LocalDateTime.now());

            produit.setUpdatedAt(java.time.LocalDateTime.now());
            produitService.ajouter(produit);
        } else {
            produitEnEdition.setNom(nom);
            produitEnEdition.setDescription(desc);
            produitEnEdition.setPrix(prix);
            produitEnEdition.setStock(stock);
            produitEnEdition.setImage(selectedImage);
            produitEnEdition.setCategorie(cat);
            produitEnEdition.setSousCategorie(sousCat);
            produitEnEdition.setUpdatedAt(java.time.LocalDateTime.now());
            produitService.modifier(produitEnEdition);
        }

        // Fermer après 1s
        new Thread(() -> {
            try {
                Thread.sleep(1000);
                Platform.runLater(() -> ((Stage) nomField.getScene().getWindow()).close());
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }).start();
    }

    private void showError(Control field, Label errorLabel, String message) {
        field.getStyleClass().add("error-field");
        errorLabel.setText(message);
        errorLabel.setVisible(true);
        field.requestFocus();
    }

    private void showError(Label errorLabel, String message) {
        errorLabel.setText(message);
        errorLabel.setVisible(true);
    }

    private void resetError(Control field, Label label) {
        field.getStyleClass().remove("error-field");
        label.setVisible(false);
    }

    private void resetError(Label label) {
        label.setVisible(false);
    }

    @FXML
    public void choisirImage() {
        File file = ImageUtils.ouvrirEtCopierImage();
        if (file != null) {
            selectedImage = file.getName();
            imagePreview.setImage(ImageUtils.chargerDepuisNom(selectedImage));
        }
    }

    @FXML
    public void prendrePhoto() {
        File file = ImageUtils.prendrePhotoDepuisWebcam();
        if (file != null) {
            selectedImage = file.getName();
            imagePreview.setImage(ImageUtils.chargerDepuisNom(selectedImage));
        }
    }

    @FXML
    public void fermerFenetre() {
        ((Stage) nomField.getScene().getWindow()).close();
    }
}
