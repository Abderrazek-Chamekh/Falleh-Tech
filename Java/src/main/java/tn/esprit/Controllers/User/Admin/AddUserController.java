package tn.esprit.Controllers.User.Admin;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.util.Duration;
import javafx.animation.TranslateTransition;
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

    // Error labels
    @FXML private Label generalError;
    @FXML private Label firstNameError;
    @FXML private Label lastNameError;
    @FXML private Label emailError;
    @FXML private Label passwordError;
    @FXML private Label confirmPasswordError;
    @FXML private Label phoneError;
    @FXML private Label cinError;
    @FXML private Label roleError;
    @FXML private Label disponibilityError;
    @FXML private Label locationError;
    @FXML private Label experienceError;

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

            // Clear errors when changing role
            disponibilityError.setVisible(false);
            locationError.setVisible(false);
            experienceError.setVisible(false);
        });

        // Clear errors when fields are modified
        firstNameField.textProperty().addListener((obs, oldVal, newVal) -> firstNameError.setVisible(false));
        lastNameField.textProperty().addListener((obs, oldVal, newVal) -> lastNameError.setVisible(false));
        emailField.textProperty().addListener((obs, oldVal, newVal) -> emailError.setVisible(false));
        passwordField.textProperty().addListener((obs, oldVal, newVal) -> passwordError.setVisible(false));
        confirmPasswordField.textProperty().addListener((obs, oldVal, newVal) -> confirmPasswordError.setVisible(false));
        phoneField.textProperty().addListener((obs, oldVal, newVal) -> phoneError.setVisible(false));
        cinField.textProperty().addListener((obs, oldVal, newVal) -> cinError.setVisible(false));
        disponibilityField.valueProperty().addListener((obs, oldVal, newVal) -> disponibilityError.setVisible(false));
        locationField.textProperty().addListener((obs, oldVal, newVal) -> locationError.setVisible(false));
        experienceField.textProperty().addListener((obs, oldVal, newVal) -> experienceError.setVisible(false));
    }

    @FXML
    private void handleSave() {
        // Clear all errors first
        clearAllErrors();

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
        PasswordHasher ps = new PasswordHasher();
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
            showSuccess("User created successfully!");

            // Clear form
            handleReset();

        } catch (SQLException e) {
            showGeneralError("Failed to create user: " + e.getMessage());
        }
    }

    private void clearAllErrors() {
        generalError.setVisible(false);
        firstNameError.setVisible(false);
        lastNameError.setVisible(false);
        emailError.setVisible(false);
        passwordError.setVisible(false);
        confirmPasswordError.setVisible(false);
        phoneError.setVisible(false);
        cinError.setVisible(false);
        roleError.setVisible(false);
        disponibilityError.setVisible(false);
        locationError.setVisible(false);
        experienceError.setVisible(false);
    }

    private boolean validateInputs() {
        boolean isValid = true;

        // Name validation
        if (firstNameField.getText().trim().isEmpty()) {
            firstNameError.setText("Please enter first name");
            firstNameError.setVisible(true);
            shakeError(firstNameError);
            isValid = false;
        }

        if (lastNameField.getText().trim().isEmpty()) {
            lastNameError.setText("Please enter last name");
            lastNameError.setVisible(true);
            shakeError(lastNameError);
            isValid = false;
        }

        // Email validation
        String email = emailField.getText().trim();
        if (email.isEmpty()) {
            emailError.setText("Please enter email");
            emailError.setVisible(true);
            shakeError(emailError);
            isValid = false;
        } else if (!email.matches("^[A-Za-z0-9+_.-]+@(.+)$")) {
            emailError.setText("Invalid email format");
            emailError.setVisible(true);
            shakeError(emailError);
            isValid = false;
        }

        // Password validation
        String password = passwordField.getText();
        if (password.isEmpty()) {
            passwordError.setText("Please enter password");
            passwordError.setVisible(true);
            shakeError(passwordError);
            isValid = false;
        } else if (password.length() < 8) {
            passwordError.setText("Password must be at least 8 characters");
            passwordError.setVisible(true);
            shakeError(passwordError);
            isValid = false;
        }

        if (!password.equals(confirmPasswordField.getText())) {
            confirmPasswordError.setText("Passwords do not match");
            confirmPasswordError.setVisible(true);
            shakeError(confirmPasswordError);
            isValid = false;
        }

        // Phone validation
        if (phoneField.getText().trim().isEmpty()) {
            phoneError.setText("Please enter phone number");
            phoneError.setVisible(true);
            shakeError(phoneError);
            isValid = false;
        }

        // CIN validation
        if (cinField.getText().trim().isEmpty()) {
            cinError.setText("Please enter CIN");
            cinError.setVisible(true);
            shakeError(cinError);
            isValid = false;
        }

        // Role validation
        if (roleCombo.getValue() == null) {
            roleError.setText("Please select a role");
            roleError.setVisible(true);
            shakeError(roleError);
            isValid = false;
        }

        // Additional validation for Ouvrier
        if ("Ouvrier".equals(roleCombo.getValue())) {
            if (disponibilityField.getValue() == null) {
                disponibilityError.setText("Please select disponibility date");
                disponibilityError.setVisible(true);
                shakeError(disponibilityError);
                isValid = false;
            }
            if (locationField.getText().trim().isEmpty()) {
                locationError.setText("Please enter location");
                locationError.setVisible(true);
                shakeError(locationError);
                isValid = false;
            }
        }

        return isValid;
    }

    private void shakeError(Label errorLabel) {
        TranslateTransition shake = new TranslateTransition(Duration.millis(70), errorLabel);
        shake.setFromX(0);
        shake.setByX(10);
        shake.setCycleCount(3);
        shake.setAutoReverse(true);
        shake.play();
    }

    private void showGeneralError(String message) {
        generalError.setText(message);
        generalError.setVisible(true);
        shakeError(generalError);
    }

    private void showSuccess(String message) {
        generalError.setStyle("-fx-text-fill: #27ae60;");
        generalError.setText(message);
        generalError.setVisible(true);

        // Reset style after showing
        new java.util.Timer().schedule(
                new java.util.TimerTask() {
                    @Override
                    public void run() {
                        javafx.application.Platform.runLater(() -> {
                            generalError.setStyle("-fx-text-fill: #e74c3c;");
                            generalError.setVisible(false);
                        });
                    }
                },
                3000
        );
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
        clearAllErrors();
    }
}