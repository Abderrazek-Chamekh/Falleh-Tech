package tn.esprit.Controllers.User.Authentication;

import javafx.animation.FadeTransition;
import javafx.animation.TranslateTransition;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.util.Duration;
import tn.esprit.entities.User;
import tn.esprit.services.UserService;
import tn.esprit.tools.SessionManager;

import java.io.IOException;
import java.sql.SQLException;

public class LoginController {

    @FXML private VBox loginCard;
    @FXML private AnchorPane leftPane;
    @FXML private BorderPane border;
    @FXML private Hyperlink signup;
    @FXML private TextField emailField;
    @FXML private PasswordField passwordField;
    @FXML private Button loginButton;
    @FXML private Label errorLabel;

    private final UserService userService = new UserService();

    @FXML
    public void initialize() {
        // Initialize error label if it exists
        if (errorLabel != null) {
            errorLabel.setVisible(false);
        }

        // Animations
        setupAnimations();

        // Login button action
        loginButton.setOnAction(event -> handleLogin());

        // Signup link action
        signup.setOnMouseClicked(event -> loadScene("/views/User/Authentication/SignUp.fxml"));
    }

    private void setupAnimations() {
        FadeTransition fade = new FadeTransition(Duration.millis(800), loginCard);
        fade.setFromValue(0);
        fade.setToValue(1);

        TranslateTransition slide = new TranslateTransition(Duration.millis(800), loginCard);
        slide.setFromY(30);
        slide.setToY(0);

        fade.play();
        slide.play();

        if (leftPane != null) {
            FadeTransition leftFade = new FadeTransition(Duration.millis(1000), leftPane);
            leftFade.setFromValue(0);
            leftFade.setToValue(1);
            leftFade.play();
        }
    }

    private void handleLogin() {
        String email = emailField.getText().trim();
        String password = passwordField.getText();

        // Basic validation
        if (email.isEmpty() || password.isEmpty()) {
            showError("Veuillez entrer votre email et mot de passe");
            return;
        }

        try {
            // Authenticate user
            User user = userService.authenticate(email, password);

            if (user != null) {

                // Create session
                SessionManager sessionManager = SessionManager.getInstance();
                sessionManager.createSession(user);

                // Successful login
                if (errorLabel != null) {
                    errorLabel.setVisible(false);
                }

                // Redirect based on role
                if ("ROLE_ADMIN".equalsIgnoreCase(user.getRole())) {
                    loadScene("/views/User/Admin/Dashboard.fxml");
                } else {
                    loadScene("/views/User/Profile/Profile.fxml");
                }
            } else {
                showError("Email ou mot de passe incorrect");
            }
        } catch (SQLException e) {
            showError("Erreur de base de données");
            e.printStackTrace();
        } catch (Exception e) {
            showError("Une erreur inattendue s'est produite");
            e.printStackTrace();
        }
    }

    private void showError(String message) {
        if (errorLabel != null) {
            errorLabel.setText(message);
            errorLabel.setVisible(true);

            // Shake animation for error
            TranslateTransition shake = new TranslateTransition(Duration.millis(100), errorLabel);
            shake.setFromX(0);
            shake.setByX(10);
            shake.setCycleCount(6);
            shake.setAutoReverse(true);
            shake.play();
        } else {
            // Fallback if error label is not available
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Erreur");
            alert.setHeaderText(null);
            alert.setContentText(message);
            alert.showAndWait();
        }
    }

    private void loadScene(String fxmlPath) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource(fxmlPath));
            Stage stage = (Stage) border.getScene().getWindow();
            stage.setScene(new Scene(root,950,800));
            stage.show();
        } catch (IOException e) {
            showError("Erreur lors du chargement de la page");
            e.printStackTrace();
        }
    }
}