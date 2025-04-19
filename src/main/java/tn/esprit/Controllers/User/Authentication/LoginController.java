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
import  tn.esprit.Controllers.front.FrontViewController;



import javafx.scene.control.Button;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.control.PasswordField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

import javafx.geometry.Pos;


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

        if (email.isEmpty() || password.isEmpty()) {
            showError("Veuillez entrer votre email et mot de passe");
            return;
        }

        try {
            User user = userService.authenticate(email, password);

            if (user != null) {
                SessionManager sessionManager = SessionManager.getInstance();
                sessionManager.createSession(user);

                if (errorLabel != null) {
                    errorLabel.setVisible(false);
                }

                if ("ROLE_ADMIN".equalsIgnoreCase(user.getRole())) {
                    loadScene("/fxml/main_layout.fxml");
                } else {
                    // ✅ Load frontview with user data and styles
                    FXMLLoader loader = new FXMLLoader(getClass().getResource("/front/frontview.fxml"));
                    Parent root = loader.load();

                    FrontViewController controller = loader.getController();
                    controller.setCurrentUser(user); // pass authenticated user

                    Scene scene = new Scene(root);
                    scene.getStylesheets().addAll(
                            getClass().getResource("/styles/main.css").toExternalForm(),
                            getClass().getResource("/styles/dashboard.css").toExternalForm(),
                            getClass().getResource("/styles/offre.css").toExternalForm(),
                            getClass().getResource("/styles/popup.css").toExternalForm(),
                            getClass().getResource("/styles/sidebar.css").toExternalForm(),
                            getClass().getResource("/styles/job-cards.css").toExternalForm(),
                            getClass().getResource("/styles/offre_card.css").toExternalForm(),
                            getClass().getResource("/styles/candidatures.css").toExternalForm(),
                            getClass().getResource("/styles/add_offre_dialog.css").toExternalForm(),
                            getClass().getResource("/styles/edit_offre_dialog.css").toExternalForm()
                    );

                    Stage stage = (Stage) emailField.getScene().getWindow();
                    stage.setScene(scene);
                    stage.show();
                }
            } else {
                showError("Email ou mot de passe incorrect");
            }

        } catch (SQLException e) {
            showError("Erreur de base de données");
            e.printStackTrace();
        } catch (IOException e) {
            showError("Erreur de chargement de l'interface");
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
            Stage stage = (Stage) emailField.getScene().getWindow(); // ✅ FIXED
            stage.setScene(new Scene(root, 950, 800));
            stage.show();
        } catch (IOException e) {
            showError("Erreur lors du chargement de la page");
            e.printStackTrace();
        }
    }

    @FXML
    private void onUseLastAccount() {
        emailField.setText("ziedalimi2244@gmail.com");
        passwordField.requestFocus();
    }
    @FXML
    private void onAddAccount() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/User/Authentication/AddAccountDialog.fxml"));
            Parent root = loader.load();

            AddAccountDialogController controller = loader.getController();
            controller.setCallback((name, email) -> addAccountCard(name, email));

            Stage dialog = new Stage();
            dialog.setTitle("Ajouter un compte");
            dialog.setScene(new Scene(root));
            dialog.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    @FXML
    private VBox leftSection;

    private void addAccountCard(String name, String email) {
        VBox card = new VBox(10);
        card.setStyle("-fx-background-color: white; -fx-padding: 10; -fx-border-radius: 10; -fx-background-radius: 10;");
        card.setAlignment(Pos.CENTER);

        ImageView icon = new ImageView(new Image("/Images/user_icon.png"));
        icon.setFitWidth(80);
        icon.setFitHeight(80);

        Label nameLabel = new Label(name);
        nameLabel.setStyle("-fx-font-weight: bold; -fx-text-fill: #2e7d32;");

        Hyperlink useLink = new Hyperlink("Utiliser ce compte");
        useLink.setOnAction(e -> {
            emailField.setText(email);
            passwordField.requestFocus();
        });

        card.getChildren().addAll(icon, nameLabel, useLink);

        leftSection.getChildren().add(1, card); // add after first card
    }


}