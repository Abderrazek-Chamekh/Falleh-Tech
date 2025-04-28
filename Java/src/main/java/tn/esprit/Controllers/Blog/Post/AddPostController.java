package tn.esprit.Controllers.Blog.Post;

import javafx.animation.TranslateTransition;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.util.Duration;
import tn.esprit.entities.Post;
import tn.esprit.entities.User;
import tn.esprit.services.ServicePost;
import tn.esprit.services.UserService;
import tn.esprit.tools.SessionManager;
import tn.esprit.utils.ImageUtils;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

public class AddPostController {

    @FXML private BorderPane borderPane;
    @FXML private TextField contenuField;
    @FXML private Label userNameLabel;
    @FXML private TextArea descriptionField;
    @FXML private ImageView imageView;
    @FXML private ComboBox<String> categoryComboBox;
    @FXML private Label errorLabel;
    @FXML private Label titleError;
    @FXML private Label descriptionError;
    @FXML private Label categoryError;
    @FXML private Label imageError;

    private String selectedImage;
    private Post publicationCourante;
    private User currentUser;
    private final UserService utilisateurService = new UserService();
    private final ServicePost publicationService = new ServicePost();
    private final Map<String, String> categoryMap = Map.of(
            "Agriculture News", "agriculture_news",
            "Farming Tips", "farming_tips",
            "Crop Management", "crop_management",
            "Livestock Care", "livestock_care"
    );

    @FXML
    public void initialize() {
        currentUser = SessionManager.getInstance().getCurrentUser();
        if (currentUser != null) {
            userNameLabel.setText(currentUser.getFullName());
        } else {
            showError("Utilisateur non trouvé");
        }

        ObservableList<String> categories = FXCollections.observableArrayList(
                "Agriculture News",
                "Farming Tips",
                "Crop Management",
                "Livestock Care"
        );
        categoryComboBox.setItems(categories);

        // Clear all errors when fields are modified
        contenuField.textProperty().addListener((obs, oldVal, newVal) -> clearError(titleError));
        descriptionField.textProperty().addListener((obs, oldVal, newVal) -> clearError(descriptionError));
        categoryComboBox.valueProperty().addListener((obs, oldVal, newVal) -> clearError(categoryError));
    }

    private void clearErrors() {
        errorLabel.setVisible(false);
        titleError.setVisible(false);
        descriptionError.setVisible(false);
        categoryError.setVisible(false);
        imageError.setVisible(false);
    }

    private void clearError(Label errorLabel) {
        errorLabel.setVisible(false);
    }

    private void showError(String message) {
        errorLabel.setText(message);
        errorLabel.setVisible(true);

        // Add shake animation
        TranslateTransition shake = new TranslateTransition(Duration.millis(100), errorLabel);
        shake.setFromX(0);
        shake.setByX(10);
        shake.setCycleCount(6);
        shake.setAutoReverse(true);
        shake.play();
    }

    private void showFieldError(Label fieldError, String message) {
        fieldError.setText(message);
        fieldError.setVisible(true);

        // Add shake animation to the error label
        TranslateTransition shake = new TranslateTransition(Duration.millis(100), fieldError);
        shake.setFromX(0);
        shake.setByX(5);
        shake.setCycleCount(4);
        shake.setAutoReverse(true);
        shake.play();
    }

    @FXML
    private void handleAddAction(ActionEvent event) {
        clearErrors();

        String contenu = contenuField.getText().trim();
        String description = descriptionField.getText().trim();
        String selectedCategory = categoryComboBox.getValue();

        boolean isValid = true;

        if (contenu.isEmpty()) {
            showFieldError(titleError, "Le champ 'Titre' est requis !");
            isValid = false;
        } else if (contenu.length() > 100) {
            showFieldError(titleError, "Le titre ne doit pas dépasser 100 caractères");
            isValid = false;
        }

        if (description.isEmpty()) {
            showFieldError(descriptionError, "Le champ 'Description' est requis !");
            isValid = false;
        } else if (description.length() > 500) {
            showFieldError(descriptionError, "La description ne doit pas dépasser 500 caractères");
            isValid = false;
        }

        if (selectedCategory == null || selectedCategory.isEmpty()) {
            showFieldError(categoryError, "Veuillez sélectionner une catégorie !");
            isValid = false;
        }

        if (!isValid) {
            showError("Veuillez corriger les erreurs dans le formulaire");
            return;
        }

        try {
            LocalDateTime currentDate = LocalDateTime.now();

            if (publicationCourante == null) {
                // Create new post
                Post newPub = new Post();
                newPub.setUser(currentUser);
                newPub.setTitre(contenu);
                newPub.setContenu(description);
                newPub.setImage(selectedImage);
                newPub.setDate(currentDate.toLocalDate());
                newPub.setCategory(categoryMap.get(selectedCategory));

                publicationService.ajouter(newPub);
                showSuccess("Publication ajoutée avec succès !");
                closeWindow(event);
            } else {
                // Update existing post
                publicationCourante.setTitre(contenu);
                publicationCourante.setContenu(description);
                publicationCourante.setImage(selectedImage);
                publicationCourante.setDate(currentDate.toLocalDate());
                publicationCourante.setCategory(categoryMap.get(selectedCategory));

                publicationService.modifier(publicationCourante);
                showSuccess("Publication mise à jour avec succès !");
                closeWindow(event);
            }
        } catch (Exception e) {
            showError("Erreur: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void showSuccess(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Succès");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void closeWindow(ActionEvent event) {
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        stage.close();
    }

    @FXML
    public void setPublication(Post pub) {
        this.publicationCourante = pub;

        if (pub != null) {
            contenuField.setText(pub.getTitre());
            descriptionField.setText(pub.getContenu());

            String categoryDisplayName = getKeyFromValue(categoryMap, pub.getCategory());
            if (categoryDisplayName != null) {
                categoryComboBox.setValue(categoryDisplayName);
            }
            if (pub.getImage() != null && !pub.getImage().isEmpty()) {
                selectedImage = pub.getImage();
                imageView.setImage(ImageUtils.chargerDepuisNom(selectedImage));
            }
        }
    }

    private String getKeyFromValue(Map<String, String> map, String value) {
        for (Map.Entry<String, String> entry : map.entrySet()) {
            if (entry.getValue().equals(value)) {
                return entry.getKey();
            }
        }
        return null;
    }

    @FXML
    public void choisirImage() {
        File file = ImageUtils.ouvrirEtCopierImage();
        if (file != null) {
            selectedImage = file.getName();
            imageView.setImage(ImageUtils.chargerDepuisNom(selectedImage));
            clearError(imageError);
        }
    }
}