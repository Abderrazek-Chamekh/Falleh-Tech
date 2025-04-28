package tn.esprit.Controllers.User.Authentication;

import jakarta.mail.*;
import javafx.animation.FadeTransition;
import javafx.animation.TranslateTransition;
import javafx.application.Platform;
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
import tn.esprit.tools.EmailService;
import tn.esprit.tools.PasswordHasher;
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
import tn.esprit.utils.SessionUtilisateur;
import java.io.IOException;
import java.sql.SQLException;
import java.util.Optional;
import java.util.Random;

public class LoginController {

    @FXML private VBox loginCard;
    @FXML private AnchorPane leftPane;
    @FXML private BorderPane border;
    @FXML private Hyperlink signup;
    @FXML private TextField emailField;
    @FXML private PasswordField passwordField;
    @FXML private Button loginButton;
    @FXML private Label errorLabel;
    @FXML private Hyperlink forgotPasswordLink;
    @FXML private Label emailError;
    @FXML private Label passwordError;
    private final UserService userService = new UserService();
    private EmailService emailService;
    private static final String REDIRECT_URI = "http://localhost:8000/login/google/callback";


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

        // Forgot password action
        forgotPasswordLink.setOnAction(event -> handleForgotPassword());

        // Initialize email service
        emailService = new EmailService();
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
    private void clearErrors() {
        emailError.setVisible(false);
        passwordError.setVisible(false);
    }
    private void handleLogin() {
        clearErrors();
        String email = emailField.getText().trim();
        String password = passwordField.getText();

        if (email.isEmpty()) {
            emailError.setText("Veuillez entrer votre email");
            emailError.setVisible(true);
            return;
        }

        if (password.isEmpty()) {
            passwordError.setText("Veuillez entrer votre mot de passe");
            passwordError.setVisible(true);
            return;
        }


        try {
            User user = userService.authenticate(email, password);

            if (user != null) {
                SessionManager sessionManager = SessionManager.getInstance();
                sessionManager.createSession(user);
                SessionUtilisateur.setUserId(user.getId());
                SessionUtilisateur.setRole(user.getRole());

                if (errorLabel != null) {
                    errorLabel.setVisible(false);
                }

                if ("ROLE_ADMIN".equalsIgnoreCase(user.getRole())) {
                    loadScene("/fxml/main_layout.fxml");
                } else {
                    FXMLLoader loader = new FXMLLoader(getClass().getResource("/front/frontview.fxml"));
                    Parent root = loader.load();

                    FrontViewController controller = loader.getController();
                    controller.setCurrentUser(user);

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
                emailError.setText("Email ou mot de passe incorrect");
                emailError.setVisible(true);
            }

        } catch (SQLException e) {
            emailError.setText("Erreur de connexion à la base de données");
            emailError.setVisible(true);
            e.printStackTrace();
        } catch (IOException e) {
            emailError.setText("Erreur de chargement de l'interface");
            emailError.setVisible(true);
            e.printStackTrace();
        } catch (Exception e) {
            emailError.setText("Une erreur inattendue s'est produite");
            emailError.setVisible(true);
            e.printStackTrace();
        }
    }

    private void handleForgotPassword() {
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Mot de passe oublié");
        dialog.setHeaderText("Réinitialisation du mot de passe");
        dialog.setContentText("Veuillez entrer votre email:");

        Optional<String> result = dialog.showAndWait();
        result.ifPresent(email -> {
            try {
                if (!email.matches("^[A-Za-z0-9+_.-]+@(.+)$")) {
                    showError("Format d'email invalide");
                    return;
                }
                if (userService.emailExists(email)) {
                    String newPassword = generateRandomPassword();
                    PasswordHasher passwordHasher = new PasswordHasher();
                    String hashedPassword = passwordHasher.hashPassword(newPassword);

                    userService.updatePasswordByEmail(email, hashedPassword);
                    sendPasswordResetEmail(email, newPassword);

                    showAlert("Succès", "Un nouveau mot de passe a été envoyé à votre email", Alert.AlertType.INFORMATION);
                } else {
                    showError("Aucun compte trouvé avec cet email");
                }
            } catch (SQLException e) {
                showError("Erreur lors de la réinitialisation du mot de passe");
                e.printStackTrace();
            } catch (Exception e) {
                showError("Erreur lors de l'envoi de l'email");
                e.printStackTrace();
            }
        });
    }
    private void sendPasswordResetEmail(String email, String newPassword) {
        new Thread(() -> {
            try {
                emailService.sendPasswordResetEmail(email, newPassword);
                Platform.runLater(() ->
                        showAlert("Succès", "Email envoyé avec succès", Alert.AlertType.INFORMATION));
            } catch (MessagingException e) {
                Platform.runLater(() ->
                        showError("Échec d'envoi de l'email"));
            }
        }).start();
    }

    private String generateRandomPassword() {
        String upper = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
        String lower = "abcdefghijklmnopqrstuvwxyz";
        String digits = "0123456789";
        String special = "!@#$%^&*()_+";
        String all = upper + lower + digits + special;

        Random random = new Random();
        StringBuilder password = new StringBuilder();

        // Ensure at least one character from each category
        password.append(upper.charAt(random.nextInt(upper.length())));
        password.append(lower.charAt(random.nextInt(lower.length())));
        password.append(digits.charAt(random.nextInt(digits.length())));
        password.append(special.charAt(random.nextInt(special.length())));

        // Fill remaining characters
        for (int i = 0; i < 6; i++) {
            password.append(all.charAt(random.nextInt(all.length())));
        }

        // Shuffle the characters
        return shuffleString(password.toString());
    }

    private String shuffleString(String input) {
        char[] characters = input.toCharArray();
        Random random = new Random();
        for (int i = 0; i < characters.length; i++) {
            int randomIndex = random.nextInt(characters.length);
            char temp = characters[i];
            characters[i] = characters[randomIndex];
            characters[randomIndex] = temp;
        }
        return new String(characters);
    }

    private void showError(String message) {
        if (errorLabel != null) {
            errorLabel.setText(message);
            errorLabel.setVisible(true);

            TranslateTransition shake = new TranslateTransition(Duration.millis(100), errorLabel);
            shake.setFromX(0);
            shake.setByX(10);
            shake.setCycleCount(6);
            shake.setAutoReverse(true);
            shake.play();
        } else {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Erreur");
            alert.setHeaderText(null);
            alert.setContentText(message);
            alert.showAndWait();
        }
    }

    private void showAlert(String title, String message, Alert.AlertType alertType) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void loadScene(String fxmlPath) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource(fxmlPath));
            Stage stage = (Stage) emailField.getScene().getWindow();
            stage.setScene(new Scene(root, 950, 800));
            stage.show();
        } catch (IOException e) {
            showError("Erreur lors du chargement de la page");
            e.printStackTrace();
        }
    }

    @FXML
    private void handleGoogleLogin() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/User/Authentication/google_login.fxml"));
            Parent root = loader.load();
            Stage stage = new Stage();
            stage.setTitle("Connexion Google");
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
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