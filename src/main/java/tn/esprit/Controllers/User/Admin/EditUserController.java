package tn.esprit.Controllers.User.Admin;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;
import tn.esprit.entities.User;
import tn.esprit.services.UserService;

import java.time.LocalDate;

public class EditUserController {

    @FXML private TextField firstNameField;
    @FXML private TextField lastNameField;
    @FXML private TextField emailField;
    @FXML private TextField phoneField;
    @FXML private TextField cinField;
    @FXML private ComboBox<String> roleCombo;
    @FXML private CheckBox activeCheckbox;

    @FXML private Label ouvrierLabel;
    @FXML private Label disponibilityLabel;
    @FXML private DatePicker disponibilityField;
    @FXML private Label locationLabel;
    @FXML private TextField locationField;
    @FXML private Label experienceLabel;
    @FXML private TextField experienceField;

    private User userToEdit;
    private final UserService userService = new UserService();

    public void setUserToEdit(User user) {
        this.userToEdit = user;
        populateForm();
    }

    private void populateForm() {
        firstNameField.setText(userToEdit.getName());
        lastNameField.setText(userToEdit.getLastName());
        emailField.setText(userToEdit.getEmail());
        phoneField.setText(userToEdit.getPhoneNumber());
        cinField.setText(userToEdit.getCarteIdentite());
        roleCombo.setValue(userToEdit.getRole());
        activeCheckbox.setSelected(userToEdit.isActive());

        // Set Ouvrier fields if applicable
        if ("Ouvrier".equals(userToEdit.getRole())) {
            toggleOuvrierFields(true);
            if (userToEdit.getDisponibility() != null) {
                disponibilityField.setValue(userToEdit.getDisponibility().toLocalDate());
            }
            locationField.setText(userToEdit.getLocation());
            experienceField.setText(userToEdit.getExperience());
        }

        // Add listener for role changes
        roleCombo.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            boolean isOuvrier = "Ouvrier".equals(newVal);
            toggleOuvrierFields(isOuvrier);
        });
    }

    private void toggleOuvrierFields(boolean show) {
        ouvrierLabel.setVisible(show);
        disponibilityLabel.setVisible(show);
        disponibilityField.setVisible(show);
        locationLabel.setVisible(show);
        locationField.setVisible(show);
        experienceLabel.setVisible(show);
        experienceField.setVisible(show);
    }

    @FXML
    private void handleSave() {
        if (!validateInputs()) {
            return;
        }

        // Update the user object with form data
        userToEdit.setName(firstNameField.getText().trim());
        userToEdit.setLastName(lastNameField.getText().trim());
        userToEdit.setEmail(emailField.getText().trim());
        userToEdit.setPhoneNumber(phoneField.getText().trim());
        userToEdit.setCarteIdentite(cinField.getText().trim());
        userToEdit.setRole(roleCombo.getValue());
        userToEdit.setActive(activeCheckbox.isSelected());

        // Handle Ouvrier-specific fields
        if ("Ouvrier".equals(userToEdit.getRole())) {
            if (disponibilityField.getValue() != null) {
                userToEdit.setDisponibility(disponibilityField.getValue().atStartOfDay());
            }
            userToEdit.setLocation(locationField.getText().trim());
            userToEdit.setExperience(experienceField.getText().trim());
        }

        try {
            userService.update(userToEdit);
            showAlert("Success", "User updated successfully", Alert.AlertType.INFORMATION);
            closeWindow();
        } catch (Exception e) {
            showAlert("Error", "Failed to update user: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    @FXML
    private void handleReset() {
        populateForm(); // Reset to original values
    }

    @FXML
    private void handleCancel() {
        closeWindow();
    }

    private boolean validateInputs() {
        // Basic validation (similar to AddUserController)
        if (firstNameField.getText().trim().isEmpty()) {
            showAlert("Validation", "Please enter first name", Alert.AlertType.ERROR);
            return false;
        }
        // Add other validation checks as needed...

        return true;
    }

    private void showAlert(String title, String message, Alert.AlertType alertType) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void closeWindow() {
        Stage stage = (Stage) firstNameField.getScene().getWindow();
        stage.close();
    }
}