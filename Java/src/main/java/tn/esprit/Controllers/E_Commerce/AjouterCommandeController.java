package tn.esprit.Controllers.E_Commerce;

import javafx.beans.binding.Bindings;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.event.ActionEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;
import tn.esprit.entities.Commande;
import tn.esprit.entities.User;
import tn.esprit.services.ServiceCommande;
import tn.esprit.services.UserService;
import tn.esprit.tools.SessionManager;
import tn.esprit.utils.SessionUtilisateur;

import java.io.IOException;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

public class AjouterCommandeController {

    @FXML private Button addCommandeButton;
    @FXML private TextField totalField, adresseLivraisonField, heureCreationField;
    @FXML private ComboBox<String> statusCombo, modePaiementCombo, statusPaiementCombo;
    @FXML private ComboBox<User> userCombo;
    @FXML private DatePicker dateCreationPicker;
    @FXML private Label userLabel, statusLabel, statusPaiementLabel;
    @FXML private TableColumn<Commande, String> actionsColumn;
    @FXML private Label userRoleLabel;
    @FXML private Button dashboardBtn;
    @FXML private HBox adminControls;
    @FXML private BorderPane borderPane;

    // Error labels
    @FXML private Label userErrorLabel;
    @FXML private Label dateErrorLabel;
    @FXML private Label totalErrorLabel;
    @FXML private Label statusErrorLabel;
    @FXML private Label adresseErrorLabel;
    @FXML private Label paiementErrorLabel;
    @FXML private Label statusPaiementErrorLabel;
    @FXML private Label globalErrorLabel;  // Added for global error messages

    private UserService userService = new UserService();
    private final ServiceCommande serviceCommande = new ServiceCommande();
    private final UserService serviceUser = new UserService();

    @FXML
    public void initialize() throws SQLException {
        String role = SessionUtilisateur.getRole();
        if (!"ROLE_ADMIN".equals(role)) {
            userCombo.setVisible(false);
            userLabel.setVisible(false);
            statusCombo.setVisible(false);
            statusLabel.setVisible(false);
            statusPaiementCombo.setVisible(false);
            statusPaiementLabel.setVisible(false);
            totalField.setDisable(true);
        }

        // Initialize error labels
        initializeErrorLabels();
        setupFieldValidation();

        // Set current time
        heureCreationField.setText(LocalTime.now().toString());
        heureCreationField.setEditable(false);

        // Set default status values
        statusCombo.setItems(FXCollections.observableArrayList("En Attente", "Confirmée", "Annulée", "Remboursée"));
        statusCombo.setValue("En Attente");

        modePaiementCombo.setItems(FXCollections.observableArrayList("Espèces", "Carte_Bancaire", "e_DINAR"));
        statusPaiementCombo.setItems(FXCollections.observableArrayList("En Attente", "Payé", "Échoué", "Remboursé"));
        statusPaiementCombo.setValue("En Attente");

        // Load users
        List<User> users = serviceUser.getAll();
        ObservableList<User> userList = FXCollections.observableArrayList(users);
        userCombo.setItems(userList);

        userCombo.setCellFactory(param -> new ListCell<>() {
            @Override
            protected void updateItem(User user, boolean empty) {
                super.updateItem(user, empty);
                setText(empty || user == null ? null : user.getName());
            }
        });

        userCombo.setButtonCell(new ListCell<>() {
            @Override
            protected void updateItem(User user, boolean empty) {
                super.updateItem(user, empty);
                setText(empty || user == null ? null : user.getName());
            }
        });
    }

    private void initializeErrorLabels() {
        userErrorLabel.setVisible(false);
        dateErrorLabel.setVisible(false);
        totalErrorLabel.setVisible(false);
        statusErrorLabel.setVisible(false);
        adresseErrorLabel.setVisible(false);
        paiementErrorLabel.setVisible(false);
        statusPaiementErrorLabel.setVisible(false);
        if (globalErrorLabel != null) {
            globalErrorLabel.setVisible(false);
            globalErrorLabel.setStyle("-fx-text-fill: #ff0000; -fx-font-size: 14px;");
        }
    }

    private void setupFieldValidation() {
        userCombo.valueProperty().addListener((obs, oldVal, newVal) -> validateUserCombo());
        dateCreationPicker.valueProperty().addListener((obs, oldVal, newVal) -> validateDatePicker());
        totalField.textProperty().addListener((obs, oldVal, newVal) -> validateTotalField());
        statusCombo.valueProperty().addListener((obs, oldVal, newVal) -> validateStatusCombo());
        adresseLivraisonField.textProperty().addListener((obs, oldVal, newVal) -> validateAdresseField());
        modePaiementCombo.valueProperty().addListener((obs, oldVal, newVal) -> validatePaiementCombo());
        statusPaiementCombo.valueProperty().addListener((obs, oldVal, newVal) -> validateStatusPaiementCombo());
    }

    private boolean validateUserCombo() {
        boolean isValid = userCombo.getValue() != null;
        setFieldStyle(userCombo, isValid);
        userErrorLabel.setVisible(!isValid);
        userErrorLabel.setText(isValid ? "" : "Veuillez sélectionner un utilisateur");
        return isValid;
    }

    private boolean validateDatePicker() {
        boolean isValid = dateCreationPicker.getValue() != null;
        setFieldStyle(dateCreationPicker, isValid);
        dateErrorLabel.setVisible(!isValid);
        dateErrorLabel.setText(isValid ? "" : "Veuillez sélectionner une date");
        return isValid;
    }

    private boolean validateTotalField() {
        boolean isValid = !totalField.getText().trim().isEmpty() &&
                totalField.getText().matches("^\\d+(\\.\\d{1,2})?$");
        setFieldStyle(totalField, isValid);
        totalErrorLabel.setVisible(!isValid);
        totalErrorLabel.setText(isValid ? "" : "Veuillez entrer un montant valide (ex: 99.99)");
        return isValid;
    }

    private boolean validateStatusCombo() {
        boolean isValid = statusCombo.getValue() != null;
        setFieldStyle(statusCombo, isValid);
        statusErrorLabel.setVisible(!isValid);
        statusErrorLabel.setText(isValid ? "" : "Veuillez sélectionner un statut");
        return isValid;
    }

    private boolean validateAdresseField() {
        boolean isValid = !adresseLivraisonField.getText().trim().isEmpty();
        setFieldStyle(adresseLivraisonField, isValid);
        adresseErrorLabel.setVisible(!isValid);
        adresseErrorLabel.setText(isValid ? "" : "Veuillez entrer une adresse");
        return isValid;
    }

    private boolean validatePaiementCombo() {
        boolean isValid = modePaiementCombo.getValue() != null;
        setFieldStyle(modePaiementCombo, isValid);
        paiementErrorLabel.setVisible(!isValid);
        paiementErrorLabel.setText(isValid ? "" : "Veuillez sélectionner un mode de paiement");
        return isValid;
    }

    private boolean validateStatusPaiementCombo() {
        boolean isValid = statusPaiementCombo.getValue() != null;
        setFieldStyle(statusPaiementCombo, isValid);
        statusPaiementErrorLabel.setVisible(!isValid);
        statusPaiementErrorLabel.setText(isValid ? "" : "Veuillez sélectionner un statut de paiement");
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
        boolean userValid = validateUserCombo();
        boolean dateValid = validateDatePicker();
        boolean totalValid = validateTotalField();
        boolean statusValid = validateStatusCombo();
        boolean adresseValid = validateAdresseField();
        boolean paiementValid = validatePaiementCombo();
        boolean statusPaiementValid = validateStatusPaiementCombo();

        return userValid && dateValid && totalValid && statusValid &&
                adresseValid && paiementValid && statusPaiementValid;
    }

    @FXML
    private void ajouterCommande(ActionEvent event) {
        if (!validateAllFields()) {
            showGlobalError("Veuillez corriger les champs invalides");
            return;
        }

        hideGlobalError();

        try {
            Commande commande = new Commande();

            // Date + heure sélectionnées
            LocalDate date = dateCreationPicker.getValue();
            LocalTime heure = LocalTime.parse(heureCreationField.getText());
            commande.setDateCreation(date != null && heure != null ?
                    LocalDateTime.of(date, heure) : LocalDateTime.now());

            commande.setTotal(Float.parseFloat(totalField.getText()));
            commande.setStatus(statusCombo.getValue());
            commande.setAdresseLivraison(adresseLivraisonField.getText());
            commande.setModePaiement(modePaiementCombo.getValue());
            commande.setStatusPaiement(statusPaiementCombo.getValue());

            User selectedUser = userCombo.getValue();
            if (selectedUser == null) {
                showGlobalError("Aucun utilisateur sélectionné");
                return;
            }
            commande.setUser(selectedUser);

            serviceCommande.ajouter(commande);

            // Close the window on success
            ((Stage) addCommandeButton.getScene().getWindow()).close();

        } catch (Exception e) {
            showGlobalError("Erreur lors de l'ajout de la commande: " + e.getMessage());
            e.printStackTrace();
        }
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