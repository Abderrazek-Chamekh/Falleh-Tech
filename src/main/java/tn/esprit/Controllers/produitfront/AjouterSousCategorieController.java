package tn.esprit.Controllers.produitfront;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.image.ImageView;
import javafx.stage.Stage;
import tn.esprit.entities.Categorie;
import tn.esprit.entities.SousCategorie;
import tn.esprit.services.CategorieService;
import tn.esprit.services.SousCategorieService;
import tn.esprit.utils.ImageUtils;

import java.io.File;
import java.util.List;

public class AjouterSousCategorieController {

    @FXML private TextField nomField;
    @FXML private ComboBox<Categorie> categorieComboBox;
    @FXML private ImageView imagePreview;
    @FXML private Label nomError;
    @FXML private Label categorieError;
    @FXML private Label imageError;

    private final SousCategorieService sousCategorieService = new SousCategorieService();
    private final CategorieService categorieService = new CategorieService();
    private String selectedImageFilename;
    private SousCategorie sousCategorieEnCoursEdition;

    @FXML
    public void initialize() {
        List<Categorie> categories = categorieService.getAll();
        categorieComboBox.getItems().setAll(categories);
    }

    public void setSousCategorie(SousCategorie s) {
        this.sousCategorieEnCoursEdition = s;
        nomField.setText(s.getNom());
        categorieComboBox.setValue(s.getCategorie());
        selectedImageFilename = s.getImage();
        if (selectedImageFilename != null && !selectedImageFilename.isBlank()) {
            imagePreview.setImage(ImageUtils.chargerDepuisNom(selectedImageFilename));
        }
    }

    @FXML
    private void enregistrerSousCategorie() {
        // Réinitialiser tous les messages et styles
        nomError.setVisible(false);
        categorieError.setVisible(false);
        imageError.setVisible(false);

        nomField.setStyle("");
        categorieComboBox.setStyle("");

        String nom = nomField.getText().trim();
        Categorie selectedCategorie = categorieComboBox.getValue();

        // 🟥 1. Valider nom
        if (nom.isEmpty()) {
            nomError.setText("⚠ Le nom est requis.");
            nomError.setVisible(true);
            nomField.setStyle("-fx-border-color: red; -fx-border-width: 1.5px; -fx-background-color: #fff0f0;");
            nomField.requestFocus();
            return;
        }
        if (nom.length() < 3 || nom.matches("\\d+")) {
            nomError.setText("⚠ Le nom doit contenir au moins 3 lettres.");
            nomError.setVisible(true);
            nomField.setStyle("-fx-border-color: red; -fx-border-width: 1.5px; -fx-background-color: #fff0f0;");
            nomField.requestFocus();
            return;
        }

        // 🟥 2. Valider catégorie
        if (selectedCategorie == null) {
            categorieError.setText("⚠ Veuillez sélectionner une catégorie.");
            categorieError.setVisible(true);
            categorieComboBox.setStyle("-fx-border-color: red; -fx-border-width: 1.5px; -fx-background-color: #fff0f0;");
            categorieComboBox.requestFocus();
            return;
        }

        // 🟥 3. Valider image
        if (selectedImageFilename == null || selectedImageFilename.isBlank()) {
            imageError.setText("⚠ Veuillez sélectionner une image.");
            imageError.setVisible(true);
            return;
        }

        // ✅ Enregistrer
        if (sousCategorieEnCoursEdition == null) {
            SousCategorie nouvelle = new SousCategorie(nom, selectedImageFilename, selectedCategorie);
            sousCategorieService.ajouter(nouvelle);
        } else {
            sousCategorieEnCoursEdition.setNom(nom);
            sousCategorieEnCoursEdition.setCategorie(selectedCategorie);
            sousCategorieEnCoursEdition.setImage(selectedImageFilename);
            sousCategorieService.modifier(sousCategorieEnCoursEdition);
        }

        // ✅ Fermer après 1s
        new Thread(() -> {
            try {
                Thread.sleep(1000);
                Platform.runLater(() -> ((Stage) nomField.getScene().getWindow()).close());
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }).start();
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

    @FXML
    public void fermerFenetre() {
        Stage stage = (Stage) nomField.getScene().getWindow();
        stage.close();
    }
}
