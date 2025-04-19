package tn.esprit.Controllers.User.Admin;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;
import tn.esprit.entities.User;
import tn.esprit.services.UserService;
import tn.esprit.tools.PasswordHasher;
import tn.esprit.tools.SessionManager;

import java.io.IOException;
import java.sql.SQLException;

public class AddUserController {

    @FXML private TextField firstNameField;
    @FXML private TextField lastNameField;
    @FXML private TextField emailField;
    @FXML private PasswordField passwordField;
    @FXML private PasswordField confirmPasswordField;
    @FXML private TextField phoneField;
    @FXML private TextField cinField;
    @FXML private ComboBox<String> roleCombo;
    @FXML private Label ouvrierLabel;
    @FXML private Label disponibilityLabel;
    @FXML private DatePicker disponibilityField;
    @FXML private Label locationLabel;
    @FXML private TextField locationField;
    @FXML private Label experienceLabel;
    @FXML private TextField experienceField;
    @FXML private Label userRoleLabel;
    private final UserService userService = new UserService();
    private User currentUser = SessionManager.getInstance().getCurrentUser();

    @FXML
    public void initialize() {
        // Set up role combo listener to show/hide ouvrier fields
        roleCombo.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            boolean isOuvrier = "Ouvrier".equals(newVal);
            ouvrierLabel.setVisible(isOuvrier);
            disponibilityLabel.setVisible(isOuvrier);
            disponibilityField.setVisible(isOuvrier);
            locationLabel.setVisible(isOuvrier);
            locationField.setVisible(isOuvrier);
            experienceLabel.setVisible(isOuvrier);
            experienceField.setVisible(isOuvrier);
        });

        userRoleLabel.setText(currentUser.getRole());
    }

    @FXML
    private void handleSave() {
        // Validate inputs
        if (!validateInputs()) {
            return;
        }

        // Create new user
        User newUser = new User();
        newUser.setName(firstNameField.getText().trim());
        newUser.setLastName(lastNameField.getText().trim());
        newUser.setEmail(emailField.getText().trim().toLowerCase());

        // Hash password
        PasswordHasher ps= new PasswordHasher();
        String encryptedPassword = ps.hashPassword(passwordField.getText());
        newUser.setPassword(encryptedPassword);

        newUser.setPhoneNumber(phoneField.getText().trim());
        newUser.setRole(roleCombo.getValue());
        newUser.setCarteIdentite(cinField.getText().trim());
        newUser.setActive(true); // Automatically activate new users

        // Set ouvrier-specific fields if applicable
        if ("Ouvrier".equals(newUser.getRole())) {
            if (disponibilityField.getValue() != null) {
                newUser.setDisponibility(disponibilityField.getValue().atStartOfDay());
            }
            newUser.setLocation(locationField.getText().trim());
            newUser.setExperience(experienceField.getText().trim());
        }

        try {
            // Add user to database
            userService.add(newUser);

            // Show success message
            showAlertAndReturn("Success", "User created successfully!", Alert.AlertType.INFORMATION);

            // Clear form
            handleReset();

        } catch (SQLException e) {
            showAlert("Error", "Failed to create user: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    private void showAlertAndReturn(String title, String message, Alert.AlertType alertType) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();

        // Return to dashboard after alert is closed
        handleBack();
    }
    private boolean validateInputs() {
        // Name validation
        if (firstNameField.getText().trim().isEmpty()) {
            showAlert("Validation", "Please enter first name", Alert.AlertType.ERROR);
            return false;
        }

        if (lastNameField.getText().trim().isEmpty()) {
            showAlert("Validation", "Please enter last name", Alert.AlertType.ERROR);
            return false;
        }

        // Email validation
        if (emailField.getText().trim().isEmpty()) {
            showAlert("Validation", "Please enter email", Alert.AlertType.ERROR);
            return false;
        }

        // Password validation
        if (passwordField.getText().isEmpty()) {
            showAlert("Validation", "Please enter password", Alert.AlertType.ERROR);
            return false;
        }

        if (!passwordField.getText().equals(confirmPasswordField.getText())) {
            showAlert("Validation", "Passwords do not match", Alert.AlertType.ERROR);
            return false;
        }

        // Phone validation
        if (phoneField.getText().trim().isEmpty()) {
            showAlert("Validation", "Please enter phone number", Alert.AlertType.ERROR);
            return false;
        }

        // CIN validation
        if (cinField.getText().trim().isEmpty()) {
            showAlert("Validation", "Please enter CIN", Alert.AlertType.ERROR);
            return false;
        }

        // Role validation
        if (roleCombo.getValue() == null) {
            showAlert("Validation", "Please select a role", Alert.AlertType.ERROR);
            return false;
        }

        // Additional validation for Ouvrier
        if ("Ouvrier".equals(roleCombo.getValue())) {
            if (disponibilityField.getValue() == null) {
                showAlert("Validation", "Please select disponibility date", Alert.AlertType.ERROR);
                return false;
            }
            if (locationField.getText().trim().isEmpty()) {
                showAlert("Validation", "Please enter location", Alert.AlertType.ERROR);
                return false;
            }
        }

        return true;
    }

    @FXML
    private void handleReset() {
        firstNameField.clear();
        lastNameField.clear();
        emailField.clear();
        passwordField.clear();
        confirmPasswordField.clear();
        phoneField.clear();
        cinField.clear();
        roleCombo.getSelectionModel().clearSelection();
        disponibilityField.setValue(null);
        locationField.clear();
        experienceField.clear();
    }

    @FXML
    private void handleBack() {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/views/User/Admin/Dashboard.fxml"));
            Stage stage = (Stage) firstNameField.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
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