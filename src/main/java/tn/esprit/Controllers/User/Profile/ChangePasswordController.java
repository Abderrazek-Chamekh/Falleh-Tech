package tn.esprit.Controllers.User.Profile;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;
import tn.esprit.entities.User;
import tn.esprit.services.UserService;
import tn.esprit.tools.PasswordHasher;
import tn.esprit.tools.SessionManager;

import java.sql.SQLException;

public class ChangePasswordController {

    @FXML private PasswordField currentPasswordField;
    @FXML private PasswordField newPasswordField;
    @FXML private PasswordField confirmPasswordField;
    @FXML private Label errorLabel;

    private final UserService userService = new UserService();
    private final PasswordHasher passwordHasher = new PasswordHasher();
    private User currentUser;

    public void initData(User user) {
        this.currentUser = user;
    }

    @FXML
    private void handleChangePassword() {
        errorLabel.setVisible(false);

        String currentPassword = currentPasswordField.getText();
        String newPassword = newPasswordField.getText();
        String confirmPassword = confirmPasswordField.getText();

        // Validate inputs
        if (currentPassword.isEmpty() || newPassword.isEmpty() || confirmPassword.isEmpty()) {
            showError("Please fill in all fields");
            return;
        }

        if (!newPassword.equals(confirmPassword)) {
            showError("New passwords don't match");
            return;
        }

        if (newPassword.length() < 8) {
            showError("Password must be at least 8 characters");
            return;
        }

        // Verify current password
        if (!passwordHasher.verifyPassword(currentPassword, currentUser.getPassword())) {
            showError("Current password is incorrect");
            return;
        }

        try {
            // Hash the new password
            String hashedPassword = passwordHasher.hashPassword(newPassword);

            // Update user password
            currentUser.setPassword(hashedPassword);
            userService.update(currentUser);

            // Update session
            SessionManager.getInstance().updateCurrentUser(currentUser);

            // Show success and close
            showAlert("Success", "Password changed successfully", Alert.AlertType.INFORMATION);
            ((Stage) currentPasswordField.getScene().getWindow()).close();

        } catch (SQLException e) {
            showError("Failed to update password: " + e.getMessage());
        }
    }

    @FXML
    private void handleCancel() {
        ((Stage) currentPasswordField.getScene().getWindow()).close();
    }

    private void showError(String message) {
        errorLabel.setText(message);
        errorLabel.setVisible(true);
    }

    private void showAlert(String title, String message, Alert.AlertType alertType) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}