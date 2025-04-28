package tn.esprit.Controllers.E_Commerce;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;
import javafx.util.StringConverter;
import tn.esprit.entities.Commande;
import tn.esprit.entities.Livraison;
import tn.esprit.services.ServiceCommande;
import tn.esprit.services.ServiceLivraison;

import java.io.IOException;
import java.time.LocalDate;
import java.util.List;

public class AjouterLivraisonController {

    @FXML private BorderPane borderPane;
    @FXML private ComboBox<Commande> commandeCombo;
    @FXML private ComboBox<String> statutCombo;
    @FXML private TextField transporteurField;
    @FXML private TextField numTelField;
    @FXML private DatePicker dateLivraisonPicker;
    @FXML private Button ajouterButton;

    // Error labels
    @FXML private Label commandeErrorLabel;
    @FXML private Label statutErrorLabel;
    @FXML private Label transporteurErrorLabel;
    @FXML private Label numTelErrorLabel;
    @FXML private Label dateErrorLabel;
    @FXML private Label globalErrorLabel;

    private final ServiceLivraison serviceLivraison = new ServiceLivraison();
    private final ServiceCommande serviceCommande = new ServiceCommande();
    private Commande selectedCommande;

    @FXML
    public void initialize() {
        // Initialize error labels
        initializeErrorLabels();

        // Setup validation listeners
        setupFieldValidation();

        // Fill status ComboBox
        statutCombo.getItems().addAll("En Cours", "Livrée", "Annulée");

        // Fill commandes ComboBox
        List<Commande> commandes = serviceCommande.getAll();
        commandeCombo.getItems().addAll(commandes);

        commandeCombo.setConverter(new StringConverter<Commande>() {
            @Override
            public String toString(Commande commande) {
                return (commande != null) ? String.valueOf(commande.getId()) : "";
            }

            @Override
            public Commande fromString(String string) {
                return null;
            }
        });
    }

    private void initializeErrorLabels() {
        if (commandeErrorLabel != null) commandeErrorLabel.setVisible(false);
        if (statutErrorLabel != null) statutErrorLabel.setVisible(false);
        if (transporteurErrorLabel != null) transporteurErrorLabel.setVisible(false);
        if (numTelErrorLabel != null) numTelErrorLabel.setVisible(false);
        if (dateErrorLabel != null) dateErrorLabel.setVisible(false);
        if (globalErrorLabel != null) {
            globalErrorLabel.setVisible(false);
            globalErrorLabel.setStyle("-fx-text-fill: #ff0000; -fx-font-size: 14px;");
        }
    }

    private void setupFieldValidation() {
        // Setup listeners for real-time validation (optional)
        statutCombo.valueProperty().addListener((obs, oldVal, newVal) -> validateStatutCombo());
        transporteurField.textProperty().addListener((obs, oldVal, newVal) -> validateTransporteurField());
        numTelField.textProperty().addListener((obs, oldVal, newVal) -> validateNumTelField());
        dateLivraisonPicker.valueProperty().addListener((obs, oldVal, newVal) -> validateDatePicker());
    }

    private boolean validateStatutCombo() {
        boolean isValid = statutCombo.getValue() != null;
        setFieldStyle(statutCombo, isValid);
        if (statutErrorLabel != null) {
            statutErrorLabel.setVisible(!isValid);
            statutErrorLabel.setText(isValid ? "" : "Statut requis");
        }
        return isValid;
    }

    private boolean validateTransporteurField() {
        boolean isValid = !transporteurField.getText().trim().isEmpty();
        setFieldStyle(transporteurField, isValid);
        if (transporteurErrorLabel != null) {
            transporteurErrorLabel.setVisible(!isValid);
            transporteurErrorLabel.setText(isValid ? "" : "Transporteur requis");
        }
        return isValid;
    }

    private boolean validateNumTelField() {
        boolean isValid = !numTelField.getText().trim().isEmpty() &&
                numTelField.getText().matches("^[0-9]{8,15}$");
        setFieldStyle(numTelField, isValid);
        if (numTelErrorLabel != null) {
            numTelErrorLabel.setVisible(!isValid);
            numTelErrorLabel.setText(isValid ? "" : "Numéro invalide (8-15 chiffres)");
        }
        return isValid;
    }

    private boolean validateDatePicker() {
        boolean isValid = dateLivraisonPicker.getValue() != null;
        setFieldStyle(dateLivraisonPicker, isValid);
        if (dateErrorLabel != null) {
            dateErrorLabel.setVisible(!isValid);
            dateErrorLabel.setText(isValid ? "" : "Date requise");
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
        // Force validation of all fields when button is clicked
        boolean statutValid = validateStatutCombo();
        boolean transporteurValid = validateTransporteurField();
        boolean numTelValid = validateNumTelField();
        boolean dateValid = validateDatePicker();

        return statutValid && transporteurValid && numTelValid && dateValid;
    }

    public void setCommande(Commande commande) {
        this.selectedCommande = commande;
        commandeCombo.getSelectionModel().select(commande);
    }

    @FXML
    private void ajouterLivraison() {
        // First validate all fields
        if (!validateAllFields()) {
            showGlobalError("Veuillez corriger les champs invalides");
            return;
        }

        hideGlobalError();

        try {
            Livraison livraison = new Livraison();
            livraison.setCommande(commandeCombo.getValue());
            livraison.setStatut(statutCombo.getValue());
            livraison.setTransporteur(transporteurField.getText());
            livraison.setNumTelTransporteur(numTelField.getText());

            LocalDate selectedDate = dateLivraisonPicker.getValue();
            if (selectedDate != null) {
                livraison.setDateLivraison(selectedDate.atStartOfDay());
            }

            serviceLivraison.ajouter(livraison);
            loadScene("/views/Livraison/AfficherLivraison.fxml");
        } catch (Exception e) {
            showGlobalError("Erreur lors de l'ajout: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void loadScene(String fxmlPath) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource(fxmlPath));
            Scene scene = new Scene(root, 1150, 800);
            Stage stage = (Stage) borderPane.getScene().getWindow();
            stage.setScene(scene);
            stage.show();
        } catch (IOException e) {
            showGlobalError("Erreur lors du chargement de la page");
            e.printStackTrace();
        }
    }

    private void resetForm() {
        commandeCombo.getSelectionModel().clearSelection();
        statutCombo.getSelectionModel().clearSelection();
        transporteurField.clear();
        numTelField.clear();
        dateLivraisonPicker.setValue(null);
    }
}