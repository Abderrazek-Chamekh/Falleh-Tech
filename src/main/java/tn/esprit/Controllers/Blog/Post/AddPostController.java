package tn.esprit.Controllers.Blog.Post;

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
import tn.esprit.entities.Post;
import tn.esprit.entities.User;
import tn.esprit.services.ServicePost;
import tn.esprit.services.UserService;
import tn.esprit.tools.SessionManager;

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
    @FXML private Button returnButton;
    @FXML private TextField contenuField;
    @FXML private Label userNameLabel; // Displaying the user's name
    @FXML private TextArea descriptionField;
    @FXML private ImageView imageView;
    @FXML private ComboBox<String> categoryComboBox;
    private final Map<String, String> categoryMap = Map.of(
            "Agriculture News", "agriculture_news",
            "Farming Tips", "farming_tips",
            "Crop Management", "crop_management",
            "Livestock Care", "livestock_care"
    );
    private String imagePath = null;
    private Post publicationCourante;
    private User currentUser;
    private final UserService utilisateurService = new UserService();
    private final ServicePost publicationService = new ServicePost();
    private static final String UPLOAD_DIR = System.getProperty("user.home") + "/Falleh-TechImages/";
    @FXML
    public void initialize() {
        currentUser = SessionManager.getInstance().getCurrentUser();
        if (currentUser != null) {
            userNameLabel.setText(currentUser.getFullName());
        } else {
            showAlert("Erreur", "Utilisateur non trouvé.", Alert.AlertType.ERROR);
        }
        ObservableList<String> categories = FXCollections.observableArrayList(
                "Agriculture News",
                "Farming Tips",
                "Crop Management",
                "Livestock Care"
        );
        categoryComboBox.setItems(categories);

        returnButton.setOnMouseClicked(event -> loadScene(("/views/Blog/ShowPosts.fxml")));
        createUploadDirectory();
    }
    @FXML
    private void loadDashboard() {
        loadScene("/views/User/Admin/Dashboard.fxml");
    }

    @FXML
    private void handleLogout() {
        loadScene("/views/User/Authentication/Login.fxml");
    }
    private void loadScene(String fxmlPath) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource(fxmlPath));
            Scene scene = new Scene(root,1150,800);
            Stage stage = (Stage) borderPane.getScene().getWindow();
            stage.setScene(scene);
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void createUploadDirectory() {
        try {
            Path uploadPath = Paths.get(UPLOAD_DIR);
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }
        } catch (IOException e) {
            showAlert("Error", "Could not create upload directory: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    @FXML
    private void handleSelectImage() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Images", "*.png", "*.jpg", "*.jpeg", "*.gif"));

        File selectedFile = fileChooser.showOpenDialog(null);
        if (selectedFile != null) {
            try {

                String originalFilename = selectedFile.getName();
                String fileExtension = originalFilename.substring(originalFilename.lastIndexOf("."));
                String uniqueFilename = UUID.randomUUID().toString() + fileExtension;

                // Define destination path
                Path destinationPath = Paths.get(UPLOAD_DIR + uniqueFilename);

                // Copy the file
                Files.copy(selectedFile.toPath(), destinationPath, StandardCopyOption.REPLACE_EXISTING);

                // Store relative path in the database
                imagePath = UPLOAD_DIR + uniqueFilename;

                // Display the image
                imageView.setImage(new Image(new File(destinationPath.toString()).toURI().toString()));

            } catch (IOException e) {
                showAlert("Erreur", "Erreur lors de la copie de l'image : " + e.getMessage(), Alert.AlertType.ERROR);
            }
        } else {
            showAlert("Info", "Aucune image sélectionnée.", Alert.AlertType.INFORMATION);
        }
    }

    @FXML
    private void handleAddAction(ActionEvent event) {
        try {
            String contenu = contenuField.getText();
            String description = descriptionField.getText();
            String selectedCategory = categoryComboBox.getValue();

            if (contenu == null || contenu.trim().isEmpty()) {
                showAlert("Erreur", "Le champ 'Contenu' est vide !", Alert.AlertType.ERROR);
                return;
            }

            if (description == null || description.trim().isEmpty()) {
                showAlert("Erreur", "Le champ 'Description' est vide !", Alert.AlertType.ERROR);
                return;
            }

            if (selectedCategory == null || selectedCategory.isEmpty()) {
                showAlert("Error", "Please select a category!", Alert.AlertType.ERROR);
                return;
            }

            if (imagePath == null || imagePath.isEmpty()) {
                imagePath = getDefaultImagePath();
            }

            LocalDateTime currentDate = LocalDateTime.now(); // Get the current date and time

            if (publicationCourante == null) {
                Post newPub = new Post();
                newPub.setUser(currentUser);
                newPub.setTitre(contenu);
                newPub.setContenu(description);
                newPub.setImage(imagePath);
                newPub.setDate(currentDate.toLocalDate()); // Set the current date
                newPub.setCategory(categoryMap.get(selectedCategory));
                publicationService.ajouter(newPub);
                showAlert("Succès", "Publication ajoutée avec succès !", Alert.AlertType.INFORMATION);
            } else {
                // 🔹 Update existing publication
                publicationCourante.setTitre(contenu);
                publicationCourante.setContenu(description);
                publicationCourante.setImage(imagePath);
                publicationCourante.setDate(currentDate.toLocalDate()); // Set the current date
                publicationCourante.setCategory(categoryMap.get(selectedCategory));
                publicationService.modifier(publicationCourante);
                showAlert("Succès", "Publication mise à jour avec succès !", Alert.AlertType.INFORMATION);
            }

            redirectToPublications(event);

        } catch (Exception e) {
            showAlert("Erreur", "Erreur lors de l'ajout/modification : " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    @FXML
    public void setPublication(Post pub) {
        this.publicationCourante = pub;

        if (pub != null) {
            contenuField.setText(pub.getContenu());
            descriptionField.setText(pub.getContenu());

            String categoryDisplayName = getKeyFromValue(categoryMap, pub.getCategory());
            if (categoryDisplayName != null) {
                categoryComboBox.setValue(categoryDisplayName);
            }
            if (pub.getImage() != null && !pub.getImage().isEmpty()) {
                imagePath = pub.getImage();
                File imageFile = new File(imagePath);
                if (imageFile.exists()) {
                    imageView.setImage(new Image(imageFile.toURI().toString()));
                } else {
                    showAlert("Info", "Image introuvable, affichage impossible.", Alert.AlertType.WARNING);
                }
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

    private void redirectToPublications(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/Blog/ShowPosts.fxml"));
            BorderPane root = loader.load();
            Scene scene = new Scene(root);
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(scene);
            stage.show();
        } catch (IOException e) {
            showAlert("Erreur", "Erreur lors de la navigation : " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    private void showAlert(String title, String content, Alert.AlertType alertType) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setContentText(content);
        alert.setHeaderText(null);
        alert.showAndWait();
    }

    private String getDefaultImagePath() {
        // Path to a default image in your resources
        return "/Images/uploads/default.png";
    }
}
