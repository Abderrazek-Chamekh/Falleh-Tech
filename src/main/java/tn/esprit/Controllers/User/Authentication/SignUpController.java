package tn.esprit.Controllers.User.Authentication;

import javafx.animation.*;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.util.Duration;
import tn.esprit.entities.User;
import tn.esprit.services.UserService;
import tn.esprit.tools.PasswordHasher;

import java.io.IOException;
import java.sql.SQLException;
import java.util.regex.Pattern;

public class SignUpController {

    @FXML private VBox signupCard;
    @FXML private AnchorPane leftPane;
    @FXML private BorderPane border;
    @FXML private Hyperlink login;
    @FXML private PasswordField passwordField;
    @FXML private TextField visiblePasswordField;
    @FXML private ImageView toggleEye;
    @FXML private TextField nomField;
    @FXML private TextField prenomField;
    @FXML private TextField emailField;
    @FXML private TextField telField;
    @FXML private TextField cinField;
    @FXML private ComboBox<String> roleComboBox;
    @FXML private VBox ouvrierFields;
    @FXML private DatePicker disponibilityField;
    @FXML private TextField locationField;
    @FXML private TextField experienceField;
    @FXML private Button signupButton;

    private static final double DEFAULT_WIDTH = 900;
    private static final double DEFAULT_HEIGHT = 700;
    private static final double OUVRER_WIDTH = 900;
    private static final double OUVRER_HEIGHT = 900;
    private final UserService userService = new UserService();
    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[A-Za-z0-9+_.-]+@(.+)$");
    private static final Pattern PHONE_PATTERN = Pattern.compile("^\\d{8}$");
    private static final Pattern CIN_PATTERN = Pattern.compile("^\\d{8}$");

    @FXML
    public void initialize() {
        // Animations
        setupAnimations();

        // Password toggle setup
        setupPasswordToggle();

        // Login link action
        login.setOnMouseClicked(event -> loadScene("/views/User/Authentication/login.fxml"));

        // Signup button action
        signupButton.setOnAction(event -> handleSignup());


        // Add listener to role selection
        // In your role selection listener:
        roleComboBox.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            boolean isOuvrier = "Ouvrier".equals(newVal);
            ouvrierFields.setVisible(isOuvrier);
            ouvrierFields.setManaged(isOuvrier);

            // Get the current stage
            Stage stage = (Stage) border.getScene().getWindow();

            // Set target dimensions based on role
            double targetWidth = isOuvrier ? OUVRER_WIDTH : DEFAULT_WIDTH;
            double targetHeight = isOuvrier ? OUVRER_HEIGHT : DEFAULT_HEIGHT;

            // Animate the resize by setting the size directly in a Timeline
            Timeline timeline = new Timeline(
                    new KeyFrame(Duration.millis(300), e -> {
                        stage.setWidth(targetWidth);
                        stage.setHeight(targetHeight);
                        stage.centerOnScreen();
                    })
            );
            timeline.play();

            // Clear fields when hiding
            if (!isOuvrier) {
                disponibilityField.setValue(null);
                locationField.clear();
                experienceField.clear();
            }
        });
    }

    private void setupAnimations() {
        FadeTransition fade = new FadeTransition(Duration.millis(800), signupCard);
        fade.setFromValue(0);
        fade.setToValue(1);

        TranslateTransition slide = new TranslateTransition(Duration.millis(800), signupCard);
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

    private void setupPasswordToggle() {
        visiblePasswordField.textProperty().bindBidirectional(passwordField.textProperty());

        toggleEye.setOnMouseClicked(event -> {
            boolean isVisible = visiblePasswordField.isVisible();
            visiblePasswordField.setVisible(!isVisible);
            visiblePasswordField.setManaged(!isVisible);
            passwordField.setVisible(isVisible);
            passwordField.setManaged(isVisible);

            // Move focus to the visible field
            if (visiblePasswordField.isVisible()) {
                visiblePasswordField.requestFocus();
                visiblePasswordField.positionCaret(visiblePasswordField.getText().length());
            } else {
                passwordField.requestFocus();
                passwordField.positionCaret(passwordField.getText().length());
            }
        });
    }

    private void handleSignup() {
        // Validate inputs
        if (!validateInputs()) {
            return;
        }

        // Create new user
        User newUser = new User();
        newUser.setName(nomField.getText().trim());
        newUser.setLastName(prenomField.getText().trim());
        newUser.setEmail(emailField.getText().trim().toLowerCase());

        // Hash password
        PasswordHasher ps= new PasswordHasher();
        String encryptedPassword = ps.hashPassword(passwordField.getText());
        newUser.setPassword(encryptedPassword);

        newUser.setPhoneNumber(telField.getText().trim());
        newUser.setRole(roleComboBox.getValue());
        newUser.setCarteIdentite(cinField.getText().trim());
        newUser.setActive(true); // Automatically activate new users

        if ("Ouvrier".equals(newUser.getRole())) {
            if (disponibilityField.getValue() != null) {
                newUser.setDisponibility(disponibilityField.getValue().atStartOfDay());
            }
            newUser.setLocation(locationField.getText().trim());
            newUser.setExperience(experienceField.getText().trim());
        }

        try {
            // Check if email or CIN already exists
            if (userService.emailExists(newUser.getEmail())) {
                showAlert("Erreur", "Cet email est déjà utilisé", Alert.AlertType.ERROR);
                return;
            }

            if (userService.carteIdentiteExists(newUser.getCarteIdentite())) {
                showAlert("Erreur", "Ce numéro de carte d'identité est déjà utilisé", Alert.AlertType.ERROR);
                return;
            }

            // Add user to database
            userService.add(newUser);

            // Show success message
            showAlert("Succès", "Compte créé avec succès!", Alert.AlertType.INFORMATION);

            // Redirect to login page
            loadScene("/views/User/Authentication/login.fxml");

        } catch (SQLException e) {
            showAlert("Erreur", "Erreur lors de la création du compte: " + e.getMessage(), Alert.AlertType.ERROR);
            e.printStackTrace();
        }
    }

    private boolean validateInputs() {
        // Name validation
        if (nomField.getText().trim().isEmpty()) {
            showAlert("Validation", "Veuillez entrer votre nom", Alert.AlertType.ERROR);
            return false;
        }

        // Last name validation
        if (prenomField.getText().trim().isEmpty()) {
            showAlert("Validation", "Veuillez entrer votre prénom", Alert.AlertType.ERROR);
            return false;
        }

        // Email validation
        if (emailField.getText().trim().isEmpty() || !EMAIL_PATTERN.matcher(emailField.getText().trim()).matches()) {
            showAlert("Validation", "Veuillez entrer une adresse email valide", Alert.AlertType.ERROR);
            return false;
        }

        // Password validation
        if (passwordField.getText().isEmpty() || passwordField.getText().length() < 8) {
            showAlert("Validation", "Le mot de passe doit contenir au moins 8 caractères", Alert.AlertType.ERROR);
            return false;
        }

        // Phone validation
        if (telField.getText().trim().isEmpty() || !PHONE_PATTERN.matcher(telField.getText().trim()).matches()) {
            showAlert("Validation", "Veuillez entrer un numéro de téléphone valide (8 chiffres)", Alert.AlertType.ERROR);
            return false;
        }

        // CIN validation
        if (cinField.getText().trim().isEmpty() || !CIN_PATTERN.matcher(cinField.getText().trim()).matches()) {
            showAlert("Validation", "Veuillez entrer un numéro de carte d'identité valide (8 chiffres)", Alert.AlertType.ERROR);
            return false;
        }

        // Role validation
        if (roleComboBox.getValue() == null) {
            showAlert("Validation", "Veuillez sélectionner un rôle", Alert.AlertType.ERROR);
            return false;
        }

        if ("Ouvrier".equals(roleComboBox.getValue())) {
            if (disponibilityField.getValue() == null) {
                showAlert("Validation", "Veuillez sélectionner une date de disponibilité", Alert.AlertType.ERROR);
                return false;
            }
            if (locationField.getText().trim().isEmpty()) {
                showAlert("Validation", "Veuillez entrer votre localisation", Alert.AlertType.ERROR);
                return false;
            }
        }
        return true;
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
            Scene scene = new Scene(root,1000,600);
            Stage stage = (Stage) border.getScene().getWindow();
            stage.setScene(scene);
            stage.show();
        } catch (IOException e) {
            showAlert("Erreur", "Erreur lors du chargement de la page: " + e.getMessage(), Alert.AlertType.ERROR);
            e.printStackTrace();
        }
    }
}