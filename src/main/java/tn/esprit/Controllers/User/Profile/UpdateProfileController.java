package tn.esprit.Controllers.User.Profile;

import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.DatePicker;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import tn.esprit.entities.User;
import tn.esprit.services.UserService;
import tn.esprit.tools.SessionManager;

import java.sql.SQLException;
import java.time.LocalDate;

public class UpdateProfileController {

    @FXML
    private TextField firstNameField;
    @FXML private TextField lastNameField;
    @FXML private TextField emailField;
    @FXML private TextField phoneField;
    @FXML private TextField cinField;
    @FXML private VBox ouvrierFields;
    @FXML private DatePicker availabilityField;
    @FXML private TextField locationField;
    @FXML private TextField experienceField;

    private UserService userService = new UserService();
    private User currentUser = SessionManager.getInstance().getCurrentUser();

    public void initData(User user) {
        this.currentUser = user;

        // Set initial values
        firstNameField.setText(user.getName());
        lastNameField.setText(user.getLastName());
        emailField.setText(user.getEmail());
        phoneField.setText(user.getPhoneNumber());
        cinField.setText(user.getCarteIdentite());

        // Show additional fields for Ouvrier
        if ("Ouvrier".equalsIgnoreCase(user.getRole())) {
            ouvrierFields.setVisible(true);
            if (user.getDisponibility() != null) {
                availabilityField.setValue(user.getDisponibility().toLocalDate());
            }
            locationField.setText(user.getLocation());
            experienceField.setText(user.getExperience());
        }
    }

    @FXML
    private void handleSave() {
        try {
            // Update user object with new values
            currentUser.setName(firstNameField.getText());
            currentUser.setLastName(lastNameField.getText());
            currentUser.setEmail(emailField.getText());
            currentUser.setPhoneNumber(phoneField.getText());
            currentUser.setCarteIdentite(cinField.getText());

            if ("Ouvrier".equalsIgnoreCase(currentUser.getRole())) {
                LocalDate availability = availabilityField.getValue();
                if (availability != null) {
                    currentUser.setDisponibility(availability.atStartOfDay());
                }
                currentUser.setLocation(locationField.getText());
                currentUser.setExperience(experienceField.getText());
            }

            // Save to database
            userService.update(currentUser);

            // Update session
            SessionManager.getInstance().updateCurrentUser(currentUser);

            // Close the window
            ((Stage) firstNameField.getScene().getWindow()).close();

        } catch (SQLException e) {
            showAlert("Error", "Failed to update profile: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    @FXML
    private void handleCancel() {
        ((Stage) firstNameField.getScene().getWindow()).close();
    }

    private void showAlert(String title, String message, Alert.AlertType alertType) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
