package tn.esprit.Controllers.User.Profile;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import tn.esprit.entities.User;
import tn.esprit.services.UserService;
import tn.esprit.tools.SessionManager;

import java.io.IOException;
import java.sql.SQLException;

public class ProfileController {

    @FXML private BorderPane borderPane;
    @FXML private Label fullNameLabel;
    @FXML private Label roleLabel;
    @FXML private Label firstNameLabel;
    @FXML private Label lastNameLabel;
    @FXML private Label emailLabel;
    @FXML private Label phoneLabel;
    @FXML private Label cinLabel;
    @FXML private VBox additionalInfoSection;
    @FXML private Label availabilityLabel;
    @FXML private Label locationLabel;
    @FXML private Label experienceLabel;
    private User currentUser = SessionManager.getInstance().getCurrentUser();
    private UserService userService = new UserService();

    @FXML
    public void initialize() {

        if (currentUser != null) {
            loadUserData();
        }
    }

    private void loadUserData() {
        // Refresh user data from database
        try {
            currentUser = userService.getById(currentUser.getId());
            SessionManager.getInstance().updateCurrentUser(currentUser);
        } catch (SQLException e) {
            showAlert("Error", "Failed to load user data", Alert.AlertType.ERROR);
            return;
        }

        // Basic info
        fullNameLabel.setText(currentUser.getName() + " " + currentUser.getLastName());
        roleLabel.setText(currentUser.getRole());
        firstNameLabel.setText(currentUser.getName());
        lastNameLabel.setText(currentUser.getLastName());
        emailLabel.setText(currentUser.getEmail());
        phoneLabel.setText(currentUser.getPhoneNumber());
        cinLabel.setText(currentUser.getCarteIdentite());

        // Show additional fields for Ouvrier role
        if ("Ouvrier".equalsIgnoreCase(currentUser.getRole())) {
            additionalInfoSection.setVisible(true);
            availabilityLabel.setText(currentUser.getDisponibility() != null ?
                    currentUser.getDisponibility().toString() : "Not specified");
            locationLabel.setText(currentUser.getLocation());
            experienceLabel.setText(currentUser.getExperience());
        } else {
            additionalInfoSection.setVisible(false);
        }
    }

    @FXML
    private void handleEditProfile() {
        try {
            String fxmlPath = "/views/User/Profile/EditProfile.fxml";

            // First verify the resource exists
            if (getClass().getResource(fxmlPath) == null) {
                showAlert("Error", "Edit profile form not found at: " + fxmlPath, Alert.AlertType.ERROR);
                return;
            }

            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Parent root = loader.load();

            UpdateProfileController controller = loader.getController();

            controller.initData(currentUser);

            Stage stage = new Stage();
            stage.setScene(new Scene(root));
            stage.setTitle("Edit Profile");
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.initOwner(borderPane.getScene().getWindow());
            // Size constraints
            stage.setMinWidth(400);
            stage.setMinHeight(300);

            // Center on parent
            stage.setOnShown(event -> {
                stage.setX(stage.getX() + stage.getWidth()/2 - stage.getWidth()/2);
                stage.setY(stage.getY() + stage.getHeight()/2 - stage.getHeight()/2);
            });

            stage.showAndWait();
            loadUserData();
        } catch (IOException e) {
            System.err.println("IO Error opening edit profile:");
            e.printStackTrace();
            showAlert("Error", "Failed to open edit profile page: " + e.getMessage(), Alert.AlertType.ERROR);
        } catch (Exception e) {
            System.err.println("Unexpected error opening edit profile:");
            e.printStackTrace();
            showAlert("Error", "An unexpected error occurred: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    @FXML
    private void handleChangePassword() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/User/Profile/ChangePassword.fxml"));
            Parent root = loader.load();

            ChangePasswordController controller = loader.getController();
            controller.initData(currentUser);

            Stage stage = new Stage();
            stage.setScene(new Scene(root));
            stage.setTitle("Change Password");
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.initOwner(borderPane.getScene().getWindow());
            stage.showAndWait();
        } catch (IOException e) {
            showAlert("Error", "Failed to open change password page", Alert.AlertType.ERROR);
        }
    }

    // Navigation methods
    @FXML
    private void loadDashboard() { loadScene("/views/User/Admin/Dashboard.fxml"); }

    @FXML
    private void loadPosts() { loadScene("/views/Blog/ShowPosts.fxml"); }

    @FXML
    private void handleLogout() {
        SessionManager.getInstance().invalidateCurrentSession();
        loadScene("/views/User/Authentication/Login.fxml");
    }

    private void loadScene(String fxmlPath) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource(fxmlPath));
            Scene scene = new Scene(root,950,800);
            Stage stage = (Stage) borderPane.getScene().getWindow();
            stage.setScene(scene);
            stage.show();
        } catch (IOException e) {
            showAlert("Error", "Failed to load page: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    private void showAlert(String title, String message, Alert.AlertType alertType) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}