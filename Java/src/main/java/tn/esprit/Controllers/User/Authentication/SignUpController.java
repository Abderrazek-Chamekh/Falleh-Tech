package tn.esprit.Controllers.User.Authentication;

import jakarta.mail.MessagingException;
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
import tn.esprit.tools.EmailService;
import tn.esprit.tools.PasswordHasher;
import tn.esprit.tools.VerificationCodeGenerator;

import java.io.IOException;
import java.sql.SQLException;
import java.util.regex.Pattern;

public class SignUpController {

    @FXML private VBox signupCard;
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
    @FXML private TextField verificationCodeField;
    @FXML private Button verifyButton;
    @FXML private Button resendCodeButton;
    @FXML private Label verificationLabel;
    @FXML private VBox verificationBox;
    @FXML private Label nomError;
    @FXML private Label prenomError;
    @FXML private Label emailError;
    @FXML private Label passwordError;
    @FXML private Label telError;
    @FXML private Label cinError;
    @FXML private Label roleError;
    @FXML private Label disponibilityError;
    @FXML private Label locationError;
    private String generatedVerificationCode;
    private User pendingUser;
    private final EmailService emailService = new EmailService();

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

        verificationBox.setVisible(false);
        verificationBox.setManaged(false);

        // Set up verification button
        verifyButton.setOnAction(event -> handleVerification());

        // Set up resend code button
        resendCodeButton.setOnAction(event -> resendVerificationCode());
    }
    private void clearAllErrors() {
        nomError.setVisible(false);
        prenomError.setVisible(false);
        emailError.setVisible(false);
        passwordError.setVisible(false);
        telError.setVisible(false);
        cinError.setVisible(false);
        roleError.setVisible(false);
        disponibilityError.setVisible(false);
        locationError.setVisible(false);
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
        if (!validateInputs()) {
            return;
        }

        // Create user object but don't save yet
        pendingUser = new User();
        pendingUser.setName(nomField.getText().trim());
        pendingUser.setLastName(prenomField.getText().trim());
        pendingUser.setEmail(emailField.getText().trim().toLowerCase());

        PasswordHasher ps = new PasswordHasher();
        String encryptedPassword = ps.hashPassword(passwordField.getText());
        pendingUser.setPassword(encryptedPassword);

        pendingUser.setPhoneNumber(telField.getText().trim());
        pendingUser.setRole(roleComboBox.getValue());
        pendingUser.setCarteIdentite(cinField.getText().trim());
        pendingUser.setActive(false); // Don't activate until verified

        if ("Ouvrier".equals(pendingUser.getRole())) {
            if (disponibilityField.getValue() != null) {
                pendingUser.setDisponibility(disponibilityField.getValue().atStartOfDay());
            }
            pendingUser.setLocation(locationField.getText().trim());
            pendingUser.setExperience(experienceField.getText().trim());
        }

        try {
            // Check if email or CIN already exists
            if (userService.emailExists(emailField.getText().trim())) {
                emailError.setText("Cet email est déjà utilisé");
                emailError.setVisible(true);
                return;
            }

            if (userService.carteIdentiteExists(cinField.getText().trim())) {
                cinError.setText("Ce numéro de carte d'identité est déjà utilisé");
                cinError.setVisible(true);
                return;
            }

            // Generate and send verification code
            generatedVerificationCode = VerificationCodeGenerator.generateCode();
            emailService.sendVerificationEmail(pendingUser.getEmail(), generatedVerificationCode);

            // Show verification UI
            signupButton.setVisible(false);
            signupButton.setManaged(false);
            verificationBox.setVisible(true);
            verificationBox.setManaged(true);
            verificationLabel.setText("Un code de vérification a été envoyé à " + pendingUser.getEmail());

        } catch (SQLException e) {
            showAlert("Erreur", "Erreur lors de la vérification des données: " + e.getMessage(), Alert.AlertType.ERROR);
            e.printStackTrace();
        } catch (MessagingException e) {
            showInlineError("Erreur lors de la vérification des données");
            e.printStackTrace();
        }
    }

    private void showInlineError(String message) {
        // You can add a general error label in your FXML if needed
        // Or use one of the existing error labels
        emailError.setText(message);
        emailError.setVisible(true);
    }

    private void handleVerification() {
        String enteredCode = verificationCodeField.getText().trim();

        if (enteredCode.isEmpty()) {
            showAlert("Validation", "Veuillez entrer le code de vérification", Alert.AlertType.ERROR);
            return;
        }

        if (!enteredCode.equals(generatedVerificationCode)) {
            showAlert("Erreur", "Code de vérification incorrect", Alert.AlertType.ERROR);
            return;
        }

        try {
            // Code matches - create the user
            userService.add(pendingUser);

            // Show success message
            showAlert("Succès", "Compte créé et vérifié avec succès!", Alert.AlertType.INFORMATION);

            // Redirect to login page
            loadScene("/views/User/Authentication/login.fxml");

        } catch (SQLException e) {
            showAlert("Erreur", "Erreur lors de la création du compte: " + e.getMessage(), Alert.AlertType.ERROR);
            e.printStackTrace();
        }
    }

    private void resendVerificationCode() {
        try {
            generatedVerificationCode = VerificationCodeGenerator.generateCode();
            emailService.sendVerificationEmail(pendingUser.getEmail(), generatedVerificationCode);
            showAlert("Succès", "Nouveau code envoyé avec succès", Alert.AlertType.INFORMATION);
        } catch (MessagingException e) {
            showAlert("Erreur", "Échec d'envoi du nouveau code", Alert.AlertType.ERROR);
            e.printStackTrace();
        }
    }

    private boolean validateInputs() {
        clearAllErrors();
        boolean isValid = true;

        // Name validation
        if (nomField.getText().trim().isEmpty()) {
            nomError.setText("Veuillez entrer votre nom");
            nomError.setVisible(true);
            isValid = false;
        }

        // Last name validation
        if (prenomField.getText().trim().isEmpty()) {
            prenomError.setText("Veuillez entrer votre prénom");
            prenomError.setVisible(true);
            isValid = false;
        }

        // Email validation
        if (emailField.getText().trim().isEmpty()) {
            emailError.setText("Veuillez entrer une adresse email");
            emailError.setVisible(true);
            isValid = false;
        } else if (!EMAIL_PATTERN.matcher(emailField.getText().trim()).matches()) {
            emailError.setText("Format d'email invalide");
            emailError.setVisible(true);
            isValid = false;
        }

        // Password validation
        if (passwordField.getText().isEmpty()) {
            passwordError.setText("Veuillez entrer un mot de passe");
            passwordError.setVisible(true);
            isValid = false;
        } else if (passwordField.getText().length() < 8) {
            passwordError.setText("Le mot de passe doit contenir au moins 8 caractères");
            passwordError.setVisible(true);
            isValid = false;
        }

        // Phone validation
        if (telField.getText().trim().isEmpty()) {
            telError.setText("Veuillez entrer un numéro de téléphone");
            telError.setVisible(true);
            isValid = false;
        } else if (!PHONE_PATTERN.matcher(telField.getText().trim()).matches()) {
            telError.setText("Numéro de téléphone invalide (8 chiffres)");
            telError.setVisible(true);
            isValid = false;
        }

        // CIN validation
        if (cinField.getText().trim().isEmpty()) {
            cinError.setText("Veuillez entrer un numéro de carte d'identité");
            cinError.setVisible(true);
            isValid = false;
        } else if (!CIN_PATTERN.matcher(cinField.getText().trim()).matches()) {
            cinError.setText("Numéro de carte d'identité invalide (8 chiffres)");
            cinError.setVisible(true);
            isValid = false;
        }

        // Role validation
        if (roleComboBox.getValue() == null) {
            roleError.setText("Veuillez sélectionner un rôle");
            roleError.setVisible(true);
            isValid = false;
        }

        if ("Ouvrier".equals(roleComboBox.getValue())) {
            if (disponibilityField.getValue() == null) {
                disponibilityError.setText("Veuillez sélectionner une date de disponibilité");
                disponibilityError.setVisible(true);
                isValid = false;
            }
            if (locationField.getText().trim().isEmpty()) {
                locationError.setText("Veuillez entrer votre localisation");
                locationError.setVisible(true);
                isValid = false;
            }
        }

        return isValid;
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