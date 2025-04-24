package tn.esprit.controllers.farmer;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import tn.esprit.entities.OffreEmploi;
import tn.esprit.entities.User;
import tn.esprit.services.ServiceOffreEmploi;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;

import java.io.IOException;
import java.time.LocalDate;

public class EditOffreDialogController {

    @FXML private TextField txtTitre;
    @FXML private TextField txtLieu;
    @FXML private DatePicker dpStartDate;
    @FXML private DatePicker dpEndDate;
    @FXML private TextArea txtDescription;
    @FXML private TextField txtSalaire;

    @FXML private Label errorTitre;
    @FXML private Label errorLieu;
    @FXML private Label errorStart;
    @FXML private Label errorEnd;
    @FXML private Label errorDesc;
    @FXML private Label errorSalaire;

    private OffreEmploi currentOffre;
    private final ServiceOffreEmploi service = new ServiceOffreEmploi();
    private StackPane contentPaneRef;

    public void setOffre(OffreEmploi offre) {
        this.currentOffre = offre;
        txtTitre.setText(offre.getTitre());
        txtLieu.setText(offre.getLieu());
        dpStartDate.setValue(offre.getStartDate());
        dpEndDate.setValue(offre.getDateExpiration());
        txtDescription.setText(offre.getDescription());
        txtSalaire.setText(String.valueOf(offre.getSalaire()));
    }

    @FXML
    public void handleSave() {
        clearErrors();

        String titre = txtTitre.getText();
        String lieu = txtLieu.getText();
        LocalDate start = dpStartDate.getValue();
        LocalDate end = dpEndDate.getValue();
        String desc = txtDescription.getText();
        String salaireStr = txtSalaire.getText();

        boolean valid = true;

        if (titre.isEmpty()) {
            errorTitre.setText("Titre requis.");
            valid = false;
        }
        if (lieu.isEmpty()) {
            errorLieu.setText("Lieu requis.");
            valid = false;
        }
        if (start == null) {
            errorStart.setText("Date début requise.");
            valid = false;
        }
        if (end == null) {
            errorEnd.setText("Date fin requise.");
            valid = false;
        }
        if (desc.isEmpty()) {
            errorDesc.setText("Description requise.");
            valid = false;
        }
        if (salaireStr.isEmpty()) {
            errorSalaire.setText("Salaire requis.");
            valid = false;
        }

        float salaire = 0;
        if (!salaireStr.isEmpty()) {
            try {
                salaire = Float.parseFloat(salaireStr);
            } catch (NumberFormatException e) {
                errorSalaire.setText("Salaire invalide.");
                valid = false;
            }
        }

        if (!valid) return;

        currentOffre.setTitre(titre);
        currentOffre.setLieu(lieu);
        currentOffre.setStartDate(start);
        currentOffre.setDateExpiration(end);
        currentOffre.setDescription(desc);
        currentOffre.setSalaire(salaire);

        if (currentOffre.getIdEmployeur() == null) {
            User staticUser = new User();
            staticUser.setId(16); // Employer ID temporaire jusqu'à intégration login
            currentOffre.setIdEmployeur(staticUser);
        }

        service.modifierFromFront(currentOffre);

        ((Stage) txtTitre.getScene().getWindow()).close();
    }

    private void clearErrors() {
        errorTitre.setText("");
        errorLieu.setText("");
        errorStart.setText("");
        errorEnd.setText("");
        errorDesc.setText("");
        errorSalaire.setText("");
    }

    @FXML
    private void handleCancel() {
        try {
            // Reload MesOffresView.fxml
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/front/farmer_front/MesOffresView.fxml"));
            Parent refreshedView = loader.load();

            // Pass contentPane reference and user ID (optional if needed)
            MesOffresController.setContentPaneRef(contentPaneRef);
            loader.<MesOffresController>getController().setCurrentUserId(currentOffre.getIdEmployeur().getId());

            contentPaneRef.getChildren().setAll(refreshedView);
        } catch (IOException e) {
            e.printStackTrace();
        }

// Close the popup
        ((Stage) txtTitre.getScene().getWindow()).close();

    }

    public void setContentPaneRef(StackPane contentPaneRef) {
        this.contentPaneRef = contentPaneRef;
    }
}