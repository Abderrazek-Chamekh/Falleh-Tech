package tn.esprit.Controllers.produitfront;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.image.ImageView;
import javafx.stage.Stage;
import tn.esprit.entities.Categorie;
import tn.esprit.services.CategorieService;
import tn.esprit.utils.ImageUtils;

import java.io.File;

public class AjouterCategorieController {

    @FXML private TextField nomField;
    @FXML private ImageView imagePreview;
    @FXML private Label nomError;
    @FXML private Label imageError;

    private final CategorieService categorieService = new CategorieService();
    private String selectedImageFilename;
    private Categorie categorieEnCoursEdition = null;

    @FXML
    private void ajouterCategorie() {
        // Réinitialisation
        nomError.setVisible(false);
        imageError.setVisible(false);
        nomField.getStyleClass().remove("error-field");

        String nom = nomField.getText().trim();

        // Validation du nom
        if (nom.isEmpty()) {
            nomError.setText("⚠ Le nom est requis.");
            nomError.setVisible(true);
            nomField.getStyleClass().add("error-field");
            nomField.requestFocus();
            return;
        }

        if (nom.length() < 3 || nom.matches("\\d+")) {
            nomError.setText("⚠ Le nom doit contenir au moins 3 lettres.");
            nomError.setVisible(true);
            nomField.getStyleClass().add("error-field");
            nomField.requestFocus();
            return;
        }

        boolean existe = categorieService.getAll().stream()
                .anyMatch(c -> c.getNom().equalsIgnoreCase(nom) &&
                        (categorieEnCoursEdition == null || !c.getId().equals(categorieEnCoursEdition.getId())));

        if (existe) {
            nomError.setText("⚠ Une catégorie avec ce nom existe déjà !");
            nomError.setVisible(true);
            nomField.getStyleClass().add("error-field");
            nomField.requestFocus();
            return;
        }
// ⚠ Vérifie unicité du nom
        boolean nomExiste = categorieService.getAll().stream()
                .anyMatch(c -> c.getNom().equalsIgnoreCase(nom) &&
                        (categorieEnCoursEdition == null || !c.getId().equals(categorieEnCoursEdition.getId())));

        if (nomExiste) {
            nomError.setText("⚠ Une catégorie avec ce nom existe déjà !");
            nomError.setVisible(true);
            nomField.requestFocus();
            return;
        }

        // Validation de l'image
        if (selectedImageFilename == null || selectedImageFilename.isBlank()) {
            imageError.setText("⚠ Veuillez sélectionner une image.");
            imageError.setVisible(true);
            return;
        }

        // Ajout ou mise à jour
        if (categorieEnCoursEdition == null) {
            Categorie nouvelle = new Categorie(nom, selectedImageFilename);
            categorieService.ajouter(nouvelle);
        } else {
            categorieEnCoursEdition.setNom(nom);
            categorieEnCoursEdition.setImage(selectedImageFilename);
            categorieService.modifier(categorieEnCoursEdition);
        }

        // Fermer la fenêtre après un délai
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

    public void setCategorie(Categorie categorie) {
        this.categorieEnCoursEdition = categorie;
        if (categorie != null) {
            nomField.setText(categorie.getNom());
            selectedImageFilename = categorie.getImage();
            if (selectedImageFilename != null && !selectedImageFilename.isBlank()) {
                imagePreview.setImage(ImageUtils.chargerDepuisNom(selectedImageFilename));
            }
        }
    }
}
