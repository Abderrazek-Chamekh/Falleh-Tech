package tn.esprit.controllers;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;
import tn.esprit.entities.OffreEmploi;
import tn.esprit.entities.User;
import tn.esprit.services.ServiceOffreEmploi;

import java.time.LocalDate;

public class AjoutOffreController {

    @FXML private TextField titreField;
    @FXML private TextField lieuField;
    @FXML private TextField salaireField;
    @FXML private DatePicker dateExpirationPicker;
    @FXML private TextArea descriptionArea;
    @FXML private Button submitButton;
    @FXML private Label popupTitle;

    private final ServiceOffreEmploi service = new ServiceOffreEmploi();
    private OffreEmploi offreToEdit = null;
    private boolean isEditMode = false;

    // Called from OffreTableController
    public void setEditMode(boolean isEdit) {
        this.isEditMode = isEdit;
        if (isEdit) {
            popupTitle.setText("Modifier une Offre");
            submitButton.setText("Modifier");
        } else {
            popupTitle.setText("Ajouter une Offre");
            submitButton.setText("Ajouter");
        }
    }

    // Optional: called from edit to fill fields
    public void setOffreToEdit(OffreEmploi offre) {
        this.offreToEdit = offre;

        titreField.setText(offre.getTitre());
        lieuField.setText(offre.getLieu());
        salaireField.setText(String.valueOf(offre.getSalaire()));
        dateExpirationPicker.setValue(offre.getDateExpiration());
        descriptionArea.setText(offre.getDescription());
    }

    @FXML
    private void handleAjouter() {
        String titre = titreField.getText().trim();
        String lieu = lieuField.getText().trim();
        String salaireText = salaireField.getText().trim();
        LocalDate expiration = dateExpirationPicker.getValue();
        String description = descriptionArea.getText().trim();

        if (titre.isEmpty() || lieu.isEmpty() || salaireText.isEmpty() || expiration == null || description.isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Champs manquants", "Tous les champs sont obligatoires !");
            return;
        }

        float salaire;
        try {
            salaire = Float.parseFloat(salaireText);
        } catch (NumberFormatException e) {
            showAlert(Alert.AlertType.ERROR, "Salaire invalide", "Le salaire doit être un nombre valide.");
            return;
        }

        if (expiration.isBefore(LocalDate.now())) {
            showAlert(Alert.AlertType.WARNING, "Date invalide", "La date d'expiration doit être future.");
            return;
        }

        try {
            if (offreToEdit != null) {
                // Modifier
                offreToEdit.setTitre(titre);
                offreToEdit.setLieu(lieu);
                offreToEdit.setSalaire(salaire);
                offreToEdit.setDateExpiration(expiration);
                offreToEdit.setDescription(description);
                service.modifier(offreToEdit);
            } else {
                // Ajouter
                OffreEmploi newOffre = new OffreEmploi();
                newOffre.setTitre(titre);
                newOffre.setLieu(lieu);
                newOffre.setSalaire(salaire);
                newOffre.setDateExpiration(expiration);
                newOffre.setDescription(description);

                User user = new User();
                user.setId(30); // TODO: Replace with actual user later
                newOffre.setIdEmployeur(user);

                service.ajouter(newOffre);
            }

            closeWindow();

        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Une erreur s'est produite :\n" + e.getMessage());
        }
    }

    @FXML
    private void handleClose() {
        closeWindow();
    }

    private void closeWindow() {
        ((Stage) submitButton.getScene().getWindow()).close();
    }

    private void showAlert(Alert.AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}
