package tn.esprit.Controllers.front.farmer;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;
import tn.esprit.entities.OffreEmploi;
import tn.esprit.entities.User;
import tn.esprit.services.ServiceOffreEmploi;

import java.time.LocalDate;

public class AddOffreDialogController {

    @FXML private TextField txtTitre;
    @FXML private TextArea txtDescription;
    @FXML private TextField txtSalaire;
    @FXML private TextField txtLieu;
    @FXML private DatePicker dpStartDate;
    @FXML private DatePicker dpEndDate;
    @FXML private Label errorTitre;
    @FXML private Label errorLieu;
    @FXML private Label errorStart;
    @FXML private Label errorEnd;
    @FXML private Label errorDesc;
    @FXML private Label errorSalaire;

    private final ServiceOffreEmploi service = new ServiceOffreEmploi();
    private User currentUser; // ✅ logged-in user

    // ✅ Setter for current user (called from MesOffresController)
    public void setCurrentUser(User user) {
        this.currentUser = user;
    }

    public boolean validateAndSubmit() {
        clearErrors();

        String titre = txtTitre.getText();
        String desc = txtDescription.getText();
        String salaireStr = txtSalaire.getText();
        String lieu = txtLieu.getText();
        LocalDate start = dpStartDate.getValue();
        LocalDate end = dpEndDate.getValue();

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

        if (!valid) return false;

        try {
            if (currentUser == null) {
                System.err.println("❌ currentUser is null in AddOffreDialogController");
                return false;
            }

            OffreEmploi offre = new OffreEmploi();
            offre.setTitre(titre);
            offre.setDescription(desc);
            offre.setSalaire(salaire);
            offre.setLieu(lieu);
            offre.setStartDate(start);
            offre.setDateExpiration(end);
            offre.setIdEmployeur(currentUser); // ✅ assign user

            service.ajouterFromFront(currentUser.getId(), offre);
            return true;

        } catch (Exception ex) {
            ex.printStackTrace();
            errorTitre.setText("Erreur lors de l'ajout.");
            return false;
        }
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
    public void handleCancel() {
        txtTitre.getScene().getWindow().hide();
    }
}
