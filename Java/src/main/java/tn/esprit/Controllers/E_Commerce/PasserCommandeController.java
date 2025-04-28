package tn.esprit.Controllers.E_Commerce;

import javafx.application.HostServices;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;
import tn.esprit.entities.Commande;
import tn.esprit.entities.User;
import tn.esprit.services.*;
import tn.esprit.tests.DashboardApp;
import tn.esprit.tools.SessionManager;
import tn.esprit.utils.SessionUtilisateur;

import java.io.IOException;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

public class PasserCommandeController {

    @FXML private TextField totalField;
    @FXML private DatePicker dateCreationPicker;
    @FXML private TextField heureCreationField;
    @FXML private ComboBox<String> statusCombo;
    @FXML private ComboBox<String> statusPaiementCombo;
    @FXML private TextField adresseLivraisonField;
    @FXML private ComboBox<String> modePaiementCombo;

    // Error labels
    @FXML private Label totalErrorLabel;
    @FXML private Label adresseErrorLabel;
    @FXML private Label paiementErrorLabel;
    @FXML private Label globalErrorLabel;  // Added for global error messages

    private final PanierService panierService = PanierService.getInstance();
    private HostServices hostServices;

    public void initialize() {
        // Initialize error labels
        initializeErrorLabels();

        // Set current date
        this.hostServices = DashboardApp.getAppHostServices();
        dateCreationPicker.setValue(LocalDate.now());

        // Set current time
        DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm");
        heureCreationField.setText(LocalTime.now().format(timeFormatter));

        // Set default status values
        statusCombo.setValue("En Attente");
        statusPaiementCombo.setValue("En Attente");

        double total = panierService.getTotal();
        totalField.setText(String.format("%.2f DT", total));

        // Setup validation listeners
        setupFieldValidation();
    }

    private void initializeErrorLabels() {
        if (totalErrorLabel != null) totalErrorLabel.setVisible(false);
        if (adresseErrorLabel != null) adresseErrorLabel.setVisible(false);
        if (paiementErrorLabel != null) paiementErrorLabel.setVisible(false);
        if (globalErrorLabel != null) {
            globalErrorLabel.setVisible(false);
            globalErrorLabel.setStyle("-fx-text-fill: #ff0000; -fx-font-size: 14px;");
        }
    }

    private void setupFieldValidation() {
        // Address field validation
        adresseLivraisonField.textProperty().addListener((obs, oldVal, newVal) -> {
            validateAdresseField();
        });

        // Payment method validation
        modePaiementCombo.valueProperty().addListener((obs, oldVal, newVal) -> {
            validatePaiementCombo();
        });
    }

    private boolean validateAdresseField() {
        boolean isValid = !adresseLivraisonField.getText().trim().isEmpty();
        setFieldStyle(adresseLivraisonField, isValid);
        if (adresseErrorLabel != null) {
            adresseErrorLabel.setVisible(!isValid);
            adresseErrorLabel.setText(isValid ? "" : "Veuillez entrer une adresse");
        }
        return isValid;
    }

    private boolean validatePaiementCombo() {
        boolean isValid = modePaiementCombo.getValue() != null;
        setFieldStyle(modePaiementCombo, isValid);
        if (paiementErrorLabel != null) {
            paiementErrorLabel.setVisible(!isValid);
            paiementErrorLabel.setText(isValid ? "" : "Veuillez sélectionner un mode de paiement");
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

    @FXML
    public void passerCommande(ActionEvent event) {
        // Validate all fields
        boolean adresseValid = validateAdresseField();
        boolean paiementValid = validatePaiementCombo();

        if (!adresseValid || !paiementValid) {
            showGlobalError("Veuillez corriger les champs invalides");
            return;
        }

        hideGlobalError();

        try {
            User currentUser = getCurrentUser();
            if (currentUser == null) {
                showGlobalError("Utilisateur non connecté");
                return;
            }

            String paymentMethod = modePaiementCombo.getValue();

            if ("Carte_Bancaire".equals(paymentMethod)) {
                handleStripePayment(currentUser);
            } else {
                handleRegularPayment(currentUser, paymentMethod);
            }

        } catch (Exception e) {
            showGlobalError("Erreur lors du traitement: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void handleStripePayment(User user) throws Exception {
        ServiceCommande commandeService = new ServiceCommande();
        StripeCheckoutService stripeService = new StripeCheckoutService();
        EmailService emailService = new EmailService();
        FacturePDFService pdfService = new FacturePDFService();

        int commandeId = commandeService.createPendingOrder(
                user.getId(),
                adresseLivraisonField.getText()
        );

        Commande commande = commandeService.getCommandeById(commandeId);
        double total = PanierService.getInstance().getTotal();
        String checkoutUrl = null;

        try {
            byte[] facturePDF = pdfService.generateFacturePDF(commande);
            checkoutUrl = stripeService.createCheckoutSession(
                    commandeId,
                    total,
                    PanierService.getInstance().getPanier(),
                    user.getEmail()
            );

            String emailBody = "Bonjour " + user.getName() + ",\n\n" +
                    "Votre commande #" + commandeId + " est en attente de paiement.\n" +
                    "Montant total: " + String.format("%.2f TND", total) + "\n\n" +
                    "Cliquez sur le lien suivant pour compléter le paiement : " + checkoutUrl + "\n\n" +
                    "Votre facture est jointe à cet email.\n\n" +
                    "Cordialement,\nL'équipe de votre application";

            emailService.sendFactureEmail(
                    user.getEmail(),
                    "Paiement en attente - Commande #" + commandeId,
                    emailBody,
                    facturePDF,
                    "facture_commande_" + commandeId + ".pdf"
            );
        } catch (Exception e) {
            System.err.println("Échec d'envoi d'email: " + e.getMessage());
        }

        PanierService.getInstance().vider();

        if (checkoutUrl != null) {
            if (hostServices != null) {
                hostServices.showDocument(checkoutUrl);
            } else {
                openBrowserManually(checkoutUrl);
            }
        }

        ((Stage) adresseLivraisonField.getScene().getWindow()).close();
    }

    private void handleRegularPayment(User user, String paymentMethod) throws SQLException {
        ServiceCommande commandeService = new ServiceCommande();
        EmailService emailService = new EmailService();
        FacturePDFService pdfService = new FacturePDFService();

        int commandeId = commandeService.createOrderFromCart(
                user.getId(),
                adresseLivraisonField.getText(),
                paymentMethod
        );

        Commande commande = commandeService.getCommandeById(commandeId);
        double total = PanierService.getInstance().getTotal();

        try {
            byte[] facturePDF = pdfService.generateFacturePDF(commande);
            String emailBody = "Bonjour " + user.getName() + ",\n\n" +
                    "Merci pour votre commande #" + commandeId + ".\n" +
                    "Montant total: " + String.format("%.2f TND", total) + "\n\n" +
                    "Votre facture est jointe à cet email.\n\n" +
                    "Cordialement,\nL'équipe de votre application";

            emailService.sendFactureEmail(
                    user.getEmail(),
                    "Facture pour commande #" + commandeId,
                    emailBody,
                    facturePDF,
                    "facture_commande_" + commandeId + ".pdf"
            );
        } catch (Exception e) {
            System.err.println("Échec d'envoi d'email: " + e.getMessage());
        }

        PanierService.getInstance().vider();
        ((Stage) adresseLivraisonField.getScene().getWindow()).close();
    }

    private void openBrowserManually(String url) {
        String os = System.getProperty("os.name").toLowerCase();
        Runtime rt = Runtime.getRuntime();

        try {
            if (os.contains("win")) {
                rt.exec("rundll32 url.dll,FileProtocolHandler " + url);
            } else if (os.contains("mac")) {
                rt.exec("open " + url);
            } else if (os.contains("nix") || os.contains("nux")) {
                rt.exec("xdg-open " + url);
            }
        } catch (IOException e) {
            showGlobalError("Impossible d'ouvrir le navigateur: " + e.getMessage());
        }
    }

    private User getCurrentUser() {
        User sessionUser = SessionManager.getInstance().getCurrentUser();
        if (sessionUser != null) {
            return sessionUser;
        }

        if (SessionUtilisateur.getUserId() != 0) {
            User user = new User();
            user.setId(SessionUtilisateur.getUserId());
            user.setName(SessionUtilisateur.getUsername());
            SessionManager.getInstance().createSession(user);
            return user;
        }

        showGlobalError("Aucun utilisateur connecté");
        return null;
    }
}