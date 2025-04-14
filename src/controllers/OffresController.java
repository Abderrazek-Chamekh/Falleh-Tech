package controllers;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.MouseEvent;
import javafx.stage.Stage;
import tn.esprit.entities.OffreEmploi;
import tn.esprit.services.ServiceOffreEmploi;

import java.io.IOException;

public class OffresController {

    @FXML private TableView<OffreEmploi> offreTable;
    @FXML private TableColumn<OffreEmploi, Integer> colId;
    @FXML private TableColumn<OffreEmploi, String> colTitre;
    @FXML private TableColumn<OffreEmploi, String> colDescription;
    @FXML private TableColumn<OffreEmploi, Float> colSalaire;
    @FXML private TableColumn<OffreEmploi, String> colLieu;

    @FXML private TextField titreField;
    @FXML private TextField descriptionField;
    @FXML private TextField salaireField;
    @FXML private TextField lieuField;
    @FXML private TextField idField;

    @FXML private TabPane tabPane;
    @FXML private Tab ajoutTab;

    private final ServiceOffreEmploi service = new ServiceOffreEmploi();

    @FXML
    public void initialize() {
        // Bind columns to entity properties
        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colTitre.setCellValueFactory(new PropertyValueFactory<>("titre"));
        colDescription.setCellValueFactory(new PropertyValueFactory<>("description"));
        colSalaire.setCellValueFactory(new PropertyValueFactory<>("salaire"));
        colLieu.setCellValueFactory(new PropertyValueFactory<>("lieu"));

        // Set row click event
        offreTable.setRowFactory(tv -> {
            TableRow<OffreEmploi> row = new TableRow<>();
            row.setOnMouseClicked((MouseEvent event) -> {
                if (!row.isEmpty() && event.getClickCount() == 1) {
                    OffreEmploi selectedOffre = row.getItem();
                    fillForm(selectedOffre);
                    tabPane.getSelectionModel().select(ajoutTab); // Switch to the "Ajouter / Modifier" tab
                }
            });
            return row;
        });

        // Load data into the table
        rafraichir();
    }

    private void rafraichir() {
        offreTable.getItems().setAll(service.getAll());
    }

    private void fillForm(OffreEmploi offre) {
        idField.setText(String.valueOf(offre.getId()));
        titreField.setText(offre.getTitre());
        descriptionField.setText(offre.getDescription());
        salaireField.setText(String.valueOf(offre.getSalaire()));
        lieuField.setText(offre.getLieu());
    }

    @FXML
    private void ajouterOffre() {
        try {
            OffreEmploi o = new OffreEmploi();
            o.setTitre(titreField.getText());
            o.setDescription(descriptionField.getText());
            o.setSalaire(Float.parseFloat(salaireField.getText()));
            o.setLieu(lieuField.getText());
            service.ajouter(o);
            rafraichir();
            clearForm();
        } catch (Exception e) {
            showAlert("Erreur", "Veuillez remplir tous les champs correctement.");
        }
    }

    @FXML
    private void modifierOffre() {
        try {
            OffreEmploi o = new OffreEmploi();
            o.setId(Integer.parseInt(idField.getText()));
            o.setTitre(titreField.getText());
            o.setDescription(descriptionField.getText());
            o.setSalaire(Float.parseFloat(salaireField.getText()));
            o.setLieu(lieuField.getText());
            service.modifier(o);
            rafraichir();
            clearForm();
        } catch (Exception e) {
            showAlert("Erreur", "Veuillez sélectionner une offre et remplir les champs.");
        }
    }

    @FXML
    private void supprimerOffre() {
        String idText = idField.getText();
        if (idText == null || idText.isEmpty()) {
            showAlert("Erreur", "Veuillez sélectionner une offre à supprimer.");
            return;
        }

        int id = Integer.parseInt(idText);
        OffreEmploi offre = new OffreEmploi();
        offre.setId(id);

        service.supprimer(offre);
        rafraichir();
        clearForm();
    }

    private void clearForm() {
        titreField.clear();
        descriptionField.clear();
        salaireField.clear();
        lieuField.clear();
        idField.clear();
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    @FXML
    private void goBackToMain(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("../main/resources/fxml/UI.fxml"));
            Parent mainPage = loader.load();
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(mainPage));
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
