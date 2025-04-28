package tn.esprit.Controllers.E_Commerce;

import javafx.beans.property.SimpleFloatProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.FileChooser;
import tn.esprit.entities.Commande;
import tn.esprit.entities.CommandeProduit;
import tn.esprit.entities.Produit;
import tn.esprit.entities.User;
import tn.esprit.services.FacturePDFService;
import tn.esprit.tools.SessionManager;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class FactureController {

    // UI Components
    @FXML private Label commandeIdLabel;
    @FXML private TableView<CommandeProduit> tableProduits;
    @FXML private TableColumn<CommandeProduit, String> colProduit;
    @FXML private TableColumn<CommandeProduit, Integer> colQuantite;
    @FXML private TableColumn<CommandeProduit, Float> colPrixUnitaire;
    @FXML private TableColumn<CommandeProduit, Float> colTotal;
    @FXML private Label totalLabel;
    @FXML private Label subtotalLabel;
    @FXML private Label tvaLabel;
    @FXML private Label clientNameLabel;
    @FXML private Label dateLabel;

    private final User currentUser = SessionManager.getInstance().getCurrentUser();
    private Commande commande;
    private final FacturePDFService pdfService = new FacturePDFService();

    @FXML
    public void initialize() {
        configureTableColumns();
    }

    private void configureTableColumns() {
        colProduit.setCellValueFactory(cellData -> {
            Produit produit = cellData.getValue().getProduit();
            return new SimpleStringProperty(produit != null ? produit.getNom() : "N/A");
        });

        colQuantite.setCellValueFactory(cellData ->
                new SimpleIntegerProperty(cellData.getValue().getQuantite()).asObject());

        colPrixUnitaire.setCellValueFactory(cellData ->
                new SimpleFloatProperty(cellData.getValue().getPrixUnitaire()).asObject());

        colTotal.setCellValueFactory(cellData ->
                new SimpleFloatProperty(calculateLineTotal(cellData.getValue())).asObject());
    }

    private float calculateLineTotal(CommandeProduit cp) {
        if (cp == null) return 0f;
        return (cp.getQuantite() != null ? cp.getQuantite() : 0) *
                (cp.getPrixUnitaire() != null ? cp.getPrixUnitaire() : 0f);
    }

    public void setCommande(Commande commande) {
        if (commande == null) {
            System.err.println("Attempted to set null commande");
            return;
        }
        this.commande = commande;
        updateFactureView();
    }

    private void updateFactureView() {
        if (commande == null) {
            showAlert("Erreur", "Aucune donnée de commande disponible", Alert.AlertType.ERROR);
            return;
        }

        commandeIdLabel.setText("Facture #" + commande.getId());
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        dateLabel.setText(LocalDate.now().format(formatter));
        clientNameLabel.setText(currentUser.getLastName() + " " + currentUser.getName());

        loadAndDisplayProducts();
        calculateAndDisplayTotals();
    }

    private void loadAndDisplayProducts() {
        if (commande.getCommandeProduits() == null || commande.getCommandeProduits().isEmpty()) {
            tableProduits.setItems(FXCollections.emptyObservableList());
            return;
        }

        ObservableList<CommandeProduit> items = FXCollections.observableArrayList(commande.getCommandeProduits());
        tableProduits.setItems(items);
    }

    private void calculateAndDisplayTotals() {
        float subtotal = commande.getCommandeProduits().stream()
                .map(this::calculateLineTotal)
                .reduce(0f, Float::sum);

        float tva = subtotal * 0.19f;
        float total = subtotal + tva;

        subtotalLabel.setText(String.format("%.2f TND", subtotal));
        tvaLabel.setText(String.format("%.2f TND", tva));
        totalLabel.setText(String.format("%.2f TND", total));
    }

    private void showAlert(String title, String message, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    public byte[] generateFacturePDF(Commande commande) throws IOException {
        return pdfService.generateFacturePDF(commande);
    }

    @FXML
    private void handleDownloadPDF() {
        try {
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Enregistrer la facture");
            fileChooser.setInitialFileName("facture_commande_" + commande.getId() + ".pdf");
            fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("PDF Files", "*.pdf"));

            File file = fileChooser.showSaveDialog(tableProduits.getScene().getWindow());

            if (file != null) {
                byte[] pdfBytes = pdfService.generateFacturePDF(commande);
                Files.write(file.toPath(), pdfBytes);
                showAlert("Succès", "Facture générée avec succès", Alert.AlertType.INFORMATION);
            }
        } catch (Exception e) {
            showAlert("Erreur", "Échec de génération du PDF: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }
}