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
        // Réinitialisation des messages d’erreur
        nomError.setVisible(false);
        prixError.setVisible(false);
        stockError.setVisible(false);
        descriptionError.setVisible(false);
        categorieError.setVisible(false);
        imageError.setVisible(false);

        BigDecimal prix = BigDecimal.ZERO;
        int stock = 0;

        String nom = nomField.getText().trim();
        String prixStr = prixField.getText().trim();
        String stockStr = stockField.getText().trim();
        String desc = descriptionField.getText().trim();
        Categorie cat = categorieComboBox.getValue();
        SousCategorie sousCat = sousCategorieComboBox.getValue();

        // Validation du nom
        if (nom.isEmpty() || nom.length() < 3) {
            nomError.setText("⚠ Le nom est requis et doit contenir au moins 3 lettres.");
            nomError.setVisible(true);
            nomField.requestFocus();
            return;
        }

        // Validation du prix
        try {
            prix = new BigDecimal(prixStr);
            if (prix.compareTo(new BigDecimal("3")) < 0) {
                throw new NumberFormatException();
            }
        } catch (NumberFormatException e) {
            prixError.setText("⚠ Prix invalide (minimum 3 DT).");
            prixError.setVisible(true);
            prixField.requestFocus();
            return;
        }

        // Validation du stock
        try {
            stock = Integer.parseInt(stockStr);
            if (stock < 0) throw new NumberFormatException();
        } catch (NumberFormatException e) {
            stockError.setText("⚠ Stock invalide.");
            stockError.setVisible(true);
            stockField.requestFocus();
            return;
        }

        // Validation de la description
        if (desc.length() < 7) {
            descriptionError.setText("⚠ La description doit contenir au moins 7 caractères.");
            descriptionError.setVisible(true);
            descriptionField.requestFocus();
            return;
        }

        // Validation de la catégorie et sous-catégorie
        if (cat == null || sousCat == null) {
            categorieError.setText("⚠ Catégorie et sous-catégorie requises.");
            categorieError.setVisible(true);
            categorieComboBox.requestFocus();
            return;
        }

        // Validation image
        if (selectedImage == null || selectedImage.isBlank()) {
            imageError.setText("⚠ Veuillez choisir une image.");
            imageError.setVisible(true);
            return;
        }

        // ✅ Ajout ou mise à jour
        if (produitEnEdition == null) {
            // Vérifier s’il existe déjà un produit avec même nom et même sous-catégorie
            Produit existant = produitService.getAll().stream()
                    .filter(p -> p.getNom().equalsIgnoreCase(nom)
                            && p.getSousCategorie().getId().equals(sousCat.getId()))
                    .findFirst()
                    .orElse(null);

            if (existant != null) {
                // ➕ Incrémentation du stock
                existant.setStock(existant.getStock() + stock);
                existant.setUpdatedAt(java.time.LocalDateTime.now());
                produitService.modifier(existant);
            } else {
                // 🆕 Création d’un nouveau produit
                Produit nouveau = new Produit();
                nouveau.setNom(nom);
                nouveau.setDescription(desc);
                nouveau.setPrix(prix);
                nouveau.setStock(stock);
                nouveau.setImage(selectedImage);
                nouveau.setCategorie(cat);
                nouveau.setSousCategorie(sousCat);
                nouveau.setUpdatedAt(java.time.LocalDateTime.now());
                produitService.ajouter(nouveau);
            }

        } else {
            // 🔄 Mise à jour du produit en cours d’édition
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

        // ✅ Fermer le popup après 1s
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
