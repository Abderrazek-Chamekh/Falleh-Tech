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

    @FXML private Label titreError;
    @FXML private Label lieuError;
    @FXML private Label salaireError;
    @FXML private Label dateError;
    @FXML private Label descriptionError;

    private final ServiceOffreEmploi service = new ServiceOffreEmploi();
    private OffreEmploi offreToEdit = null;
    private boolean isEditMode = false;

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
        // Clear error messages
        titreError.setText("");
        lieuError.setText("");
        salaireError.setText("");
        dateError.setText("");
        descriptionError.setText("");

        String titre = titreField.getText().trim();
        String lieu = lieuField.getText().trim();
        String salaireText = salaireField.getText().trim();
        LocalDate expiration = dateExpirationPicker.getValue();
        String description = descriptionArea.getText().trim();

        boolean hasError = false;

        if (titre.isEmpty()) {
            titreError.setText("Le titre est obligatoire.");
            hasError = true;
        }

        if (lieu.isEmpty()) {
            lieuError.setText("Le lieu est obligatoire.");
            hasError = true;
        }

        if (salaireText.isEmpty()) {
            salaireError.setText("Le salaire est obligatoire.");
            hasError = true;
        }

        float salaire = 0;
        if (!salaireText.isEmpty()) {
            try {
                salaire = Float.parseFloat(salaireText);
            } catch (NumberFormatException e) {
                salaireError.setText("Le salaire doit être un nombre.");
                hasError = true;
            }
        }

        if (expiration == null) {
            dateError.setText("Veuillez choisir une date.");
            hasError = true;
        } else if (expiration.isBefore(LocalDate.now())) {
            dateError.setText("La date doit être future.");
            hasError = true;
        }

        if (description.isEmpty()) {
            descriptionError.setText("La description est obligatoire.");
            hasError = true;
        }

        if (hasError) return;

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
                user.setId(30); // Replace with actual connected user ID
                newOffre.setIdEmployeur(user);

                service.ajouter(newOffre);
            }

            if (parentController != null) {

            }

            closeWindow(); // 👋 Then close the popup
            showSuccessMessage("✅ Offre ajoutée avec succès !");


        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Une erreur est survenue :\n" + e.getMessage());
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
    private void showSuccessMessage(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Succès");
        alert.setHeaderText(null);
        alert.setContentText(message);

        DialogPane dialogPane = alert.getDialogPane();
        dialogPane.getStylesheets().add(getClass().getResource("/styles/popup.css").toExternalForm());
        dialogPane.getStyleClass().add("dialog-pane");

        alert.showAndWait();
    }
    private OffreTableController parentController;

    public void setParentController(OffreTableController controller) {
        this.parentController = controller;
    }


}
