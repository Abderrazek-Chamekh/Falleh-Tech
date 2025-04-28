package tn.esprit.Controllers.E_Commerce;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.event.ActionEvent;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;
import tn.esprit.entities.Commande;
import tn.esprit.entities.User;
import tn.esprit.services.ServiceCommande;
import tn.esprit.services.UserService;
import tn.esprit.tools.SessionManager;

import java.io.IOException;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeParseException;
import java.util.List;

public class ModifierCommandeController {

    private final ServiceCommande serviceCommande = new ServiceCommande();
    public Button validerButton;

    @FXML private TextField totalField, adresseLivraisonField, heureCreationField;
    @FXML private ComboBox<String> statusCombo, modePaiementCombo, statusPaiementCombo;
    @FXML private ComboBox<User> userCombo;
    @FXML private DatePicker dateCreationPicker;
    @FXML private BorderPane borderPane;

    // Error labels
    @FXML private Label totalErrorLabel;
    @FXML private Label adresseErrorLabel;
    @FXML private Label dateErrorLabel;
    @FXML private Label heureErrorLabel;
    @FXML private Label statusErrorLabel;
    @FXML private Label paiementErrorLabel;
    @FXML private Label statusPaiementErrorLabel;
    @FXML private Label userErrorLabel;
    @FXML private Label globalErrorLabel;

    private Commande commande;
    private final UserService serviceUser = new UserService();

    public void initialize() throws SQLException {
        // Initialize error labels
        initializeErrorLabels();

        // Setup validation listeners
        setupFieldValidation();

        // Fill ComboBox values
        statusCombo.getItems().addAll("En Attente", "Confirmée", "Annulée", "Remboursée");
        modePaiementCombo.getItems().addAll("Espèces", "Carte_Bancaire", "e_DINAR");
        statusPaiementCombo.getItems().addAll("En Attente", "Payé", "Échoué", "Remboursé");

        // Load users
        List<User> users = serviceUser.getAll();
        userCombo.getItems().addAll(users);
    }

    private void initializeErrorLabels() {
        if (totalErrorLabel != null) totalErrorLabel.setVisible(false);
        if (adresseErrorLabel != null) adresseErrorLabel.setVisible(false);
        if (dateErrorLabel != null) dateErrorLabel.setVisible(false);
        if (heureErrorLabel != null) heureErrorLabel.setVisible(false);
        if (statusErrorLabel != null) statusErrorLabel.setVisible(false);
        if (paiementErrorLabel != null) paiementErrorLabel.setVisible(false);
        if (statusPaiementErrorLabel != null) statusPaiementErrorLabel.setVisible(false);
        if (userErrorLabel != null) userErrorLabel.setVisible(false);
        if (globalErrorLabel != null) {
            globalErrorLabel.setVisible(false);
            globalErrorLabel.setStyle("-fx-text-fill: #ff0000; -fx-font-size: 14px;");
        }
    }

    private void setupFieldValidation() {
        totalField.textProperty().addListener((obs, oldVal, newVal) -> validateTotalField());
        adresseLivraisonField.textProperty().addListener((obs, oldVal, newVal) -> validateAdresseField());
        dateCreationPicker.valueProperty().addListener((obs, oldVal, newVal) -> validateDatePicker());
        heureCreationField.textProperty().addListener((obs, oldVal, newVal) -> validateHeureField());
        statusCombo.valueProperty().addListener((obs, oldVal, newVal) -> validateStatusCombo());
        modePaiementCombo.valueProperty().addListener((obs, oldVal, newVal) -> validatePaiementCombo());
        statusPaiementCombo.valueProperty().addListener((obs, oldVal, newVal) -> validateStatusPaiementCombo());
        userCombo.valueProperty().addListener((obs, oldVal, newVal) -> validateUserCombo());
    }

    private boolean validateTotalField() {
        boolean isValid = !totalField.getText().trim().isEmpty() &&
                totalField.getText().matches("^\\d+(\\.\\d{1,2})?$");
        setFieldStyle(totalField, isValid);
        if (totalErrorLabel != null) {
            totalErrorLabel.setVisible(!isValid);
            totalErrorLabel.setText(isValid ? "" : "Montant invalide (ex: 99.99)");
        }
        return isValid;
    }

    private boolean validateAdresseField() {
        boolean isValid = !adresseLivraisonField.getText().trim().isEmpty();
        setFieldStyle(adresseLivraisonField, isValid);
        if (adresseErrorLabel != null) {
            adresseErrorLabel.setVisible(!isValid);
            adresseErrorLabel.setText(isValid ? "" : "Adresse requise");
        }
        return isValid;
    }

    private boolean validateDatePicker() {
        boolean isValid = dateCreationPicker.getValue() != null;
        setFieldStyle(dateCreationPicker, isValid);
        if (dateErrorLabel != null) {
            dateErrorLabel.setVisible(!isValid);
            dateErrorLabel.setText(isValid ? "" : "Date requise");
        }
        return isValid;
    }

    private boolean validateHeureField() {
        try {
            LocalTime.parse(heureCreationField.getText());
            setFieldStyle(heureCreationField, true);
            if (heureErrorLabel != null) {
                heureErrorLabel.setVisible(false);
            }
            return true;
        } catch (DateTimeParseException e) {
            setFieldStyle(heureCreationField, false);
            if (heureErrorLabel != null) {
                heureErrorLabel.setVisible(true);
                heureErrorLabel.setText("Format heure invalide");
            }
            return false;
        }
    }

    private boolean validateStatusCombo() {
        boolean isValid = statusCombo.getValue() != null;
        setFieldStyle(statusCombo, isValid);
        if (statusErrorLabel != null) {
            statusErrorLabel.setVisible(!isValid);
            statusErrorLabel.setText(isValid ? "" : "Statut requis");
        }
        return isValid;
    }

    private boolean validatePaiementCombo() {
        boolean isValid = modePaiementCombo.getValue() != null;
        setFieldStyle(modePaiementCombo, isValid);
        if (paiementErrorLabel != null) {
            paiementErrorLabel.setVisible(!isValid);
            paiementErrorLabel.setText(isValid ? "" : "Mode de paiement requis");
        }
        return isValid;
    }

    private boolean validateStatusPaiementCombo() {
        boolean isValid = statusPaiementCombo.getValue() != null;
        setFieldStyle(statusPaiementCombo, isValid);
        if (statusPaiementErrorLabel != null) {
            statusPaiementErrorLabel.setVisible(!isValid);
            statusPaiementErrorLabel.setText(isValid ? "" : "Statut de paiement requis");
        }
        return isValid;
    }

    private boolean validateUserCombo() {
        boolean isValid = userCombo.getValue() != null;
        setFieldStyle(userCombo, isValid);
        if (userErrorLabel != null) {
            userErrorLabel.setVisible(!isValid);
            userErrorLabel.setText(isValid ? "" : "Utilisateur requis");
        }
        return isValid;
    }

    private void setFieldStyle(Control field, boolean isValid) {
        if (isValid) {
            field.getStyleClass().removeAll("error-field");
        } else if (!field.getStyleClass().contains("error-field")) {
            field.getStyleClass().add("error-field");
        }
    }

    private void showGlobalError(String message) {
        if (globalErrorLabel != null) {
            globalErrorLabel.setText(message);
            globalErrorLabel.setVisible(true);
        }
    }

    private void hideGlobalError() {
        if (globalErrorLabel != null) {
            globalErrorLabel.setVisible(false);
        }
    }

    private boolean validateAllFields() {
        boolean totalValid = validateTotalField();
        boolean adresseValid = validateAdresseField();
        boolean dateValid = validateDatePicker();
        boolean heureValid = validateHeureField();
        boolean statusValid = validateStatusCombo();
        boolean paiementValid = validatePaiementCombo();
        boolean statusPaiementValid = validateStatusPaiementCombo();
        boolean userValid = validateUserCombo();

        return totalValid && adresseValid && dateValid && heureValid &&
                statusValid && paiementValid && statusPaiementValid && userValid;
    }

    public void setCommande(Commande commande) {
        this.commande = commande;

        // Fill fields
        totalField.setText(String.valueOf(commande.getTotal()));
        adresseLivraisonField.setText(commande.getAdresseLivraison());
        statusCombo.setValue(commande.getStatus());
        modePaiementCombo.setValue(commande.getModePaiement());
        statusPaiementCombo.setValue(commande.getStatusPaiement());
        userCombo.setValue(commande.getUser());

        LocalDateTime dateTime = commande.getDateCreation();
        dateCreationPicker.setValue(dateTime.toLocalDate());
        heureCreationField.setText(dateTime.toLocalTime().toString());
    }

    @FXML
    private void validerModifications(ActionEvent event) {
        if (!validateAllFields()) {
            showGlobalError("Veuillez corriger les champs invalides");
            return;
        }

        hideGlobalError();

        try {
            LocalDate date = dateCreationPicker.getValue();
            LocalTime heure = LocalTime.parse(heureCreationField.getText());
            commande.setDateCreation(LocalDateTime.of(date, heure));

            commande.setTotal(Float.parseFloat(totalField.getText()));
            commande.setStatus(statusCombo.getValue());
            commande.setAdresseLivraison(adresseLivraisonField.getText());
            commande.setModePaiement(modePaiementCombo.getValue());
            commande.setStatusPaiement(statusPaiementCombo.getValue());
            commande.setUser(userCombo.getValue());

            serviceCommande.modifier(commande);
            ((Stage) totalField.getScene().getWindow()).close();
        } catch (Exception e) {
            showGlobalError("Erreur lors de la modification: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    private void loadDashboard() { loadScene("/views/User/Admin/Dashboard.fxml"); }

    @FXML
    private void handleProfile() { loadScene("/views/User/Profile/Profile.fxml"); }

    @FXML
    private void handleLogout() {
        SessionManager.getInstance().invalidateCurrentSession();
        loadScene("/views/User/Authentication/Login.fxml");
    }

    private void loadScene(String fxmlPath) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource(fxmlPath));
            Scene scene = new Scene(root,950,800);
            Stage stage = (Stage) borderPane.getScene().getWindow();
            stage.setScene(scene);
            stage.show();
        } catch (IOException e) {
            showGlobalError("Erreur lors du chargement de la page");
            e.printStackTrace();
        }
    }
}