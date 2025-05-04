package controllers;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;
import tn.esprit.entities.Candidature;
import tn.esprit.entities.StatutCandidature;
import tn.esprit.services.ServiceCandidature;

import java.io.IOException;
import java.util.List;

public class CandidatureController {

    // TableView and columns
    @FXML private TableView<Candidature> candidatureTable;
    @FXML private TableColumn<Candidature, Integer> colId;
    @FXML private TableColumn<Candidature, StatutCandidature> colStatut;
    @FXML private TableColumn<Candidature, String> colDate;
    @FXML private TableColumn<Candidature, Integer> colRating;

    // Form inputs
    @FXML private ComboBox<StatutCandidature> statutCombo;
    @FXML private TextField ratingField;
    @FXML private TextField idField;

    // Tab control
    @FXML private TabPane tabPane;
    @FXML private Tab ajoutTab;

    private final ServiceCandidature service = new ServiceCandidature();
    private int offerId;  // Current offer being viewed

    // Initialize controller
    @FXML
    public void initialize() {
        // Setup table columns
        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colStatut.setCellValueFactory(new PropertyValueFactory<>("statut"));
        colDate.setCellValueFactory(new PropertyValueFactory<>("dateApplied"));
        colRating.setCellValueFactory(new PropertyValueFactory<>("rating"));

        // Populate enum values in ComboBox
        statutCombo.getItems().setAll(StatutCandidature.values());

        // Load data initially (in case offerId is preset)
// Load all candidatures on startup
        afficherToutesLesCandidatures();

        // Row double-click event to auto-fill the form for editing
        candidatureTable.setRowFactory(tv -> {
            TableRow<Candidature> row = new TableRow<>();
            row.setOnMouseClicked(event -> {
                if (event.getClickCount() == 2 && (!row.isEmpty())) {
                    Candidature selected = row.getItem();
                    statutCombo.setValue(selected.getStatut());
                    ratingField.setText(selected.getRating() != null ? selected.getRating().toString() : "");
                    idField.setText(selected.getId().toString());
                    tabPane.getSelectionModel().select(ajoutTab);
                }
            });
            return row;
        });
    }

    // Refreshes the table for the current offerId
    private void rafraichir() {
        List<Candidature> candidatures = service.getCandidaturesForOffer(offerId);
        candidatureTable.getItems().setAll(candidatures);
    }

    // Called by parent view to assign the offer ID
    public void setOfferId(int offerId) {
        this.offerId = offerId;
        rafraichir();
    }

    // Adds a new candidature
    @FXML
    private void ajouterCandidature() {
        Candidature c = new Candidature();
        c.setStatut(statutCombo.getValue());
        try {
            c.setRating(Integer.parseInt(ratingField.getText()));
        } catch (NumberFormatException e) {
            c.setRating(null);
        }
        service.ajouter(c, offerId);
        rafraichir();
    }

    // Modifies the selected candidature
    @FXML
    private void modifierCandidature() {
        if (idField.getText().isEmpty()) return;

        Candidature c = new Candidature();
        c.setId(Integer.parseInt(idField.getText()));
        c.setStatut(statutCombo.getValue());
        try {
            c.setRating(Integer.parseInt(ratingField.getText()));
        } catch (NumberFormatException e) {
            c.setRating(null);
        }
        service.modifier(c);
        rafraichir();
    }

    // Deletes the selected candidature
    @FXML
    private void supprimerCandidature() {
        if (idField.getText().isEmpty()) return;

        Candidature c = new Candidature();
        c.setId(Integer.parseInt(idField.getText()));
        service.supprimer(c);
        rafraichir();
    }

    // Displays all candidatures (not just those of the selected offer)
    @FXML
    private void afficherToutesLesCandidatures() {
        List<Candidature> allCandidatures = service.getAll();
        candidatureTable.getItems().setAll(allCandidatures);
    }

    // Navigates to another FXML page
    private void navigateTo(String fxmlFile, ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlFile));
            Parent page = loader.load();
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(page));
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Public methods to go back
    @FXML private void goBackToOffres(ActionEvent event) {
        navigateTo("Offres.fxml", event);
    }

    @FXML private void goBackToMain(ActionEvent event) {
        navigateTo("Offres.fxml", event); // If "Main" is Offres.fxml
    }

    public void goBackToMain1(ActionEvent event) {
        navigateTo("UI.fxml", event); // If "Main1" means another UI
    }
}
