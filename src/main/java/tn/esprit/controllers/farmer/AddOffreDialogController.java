package tn.esprit.controllers.farmer;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;
import tn.esprit.entities.OffreEmploi;
import tn.esprit.services.ServiceOffreEmploi;

import java.time.LocalDate;

public class AddOffreDialogController {

    @FXML private TextField txtTitre;
    @FXML private TextArea txtDescription;
    @FXML private TextField txtSalaire;
    @FXML private TextField txtLieu;
    @FXML private DatePicker dpStartDate;
    @FXML private DatePicker dpEndDate;

    private final ServiceOffreEmploi service = new ServiceOffreEmploi();

    @FXML
    private void handleAjouter() {
        String titre = txtTitre.getText();
        String desc = txtDescription.getText();
        String salaireStr = txtSalaire.getText();
        String lieu = txtLieu.getText();
        LocalDate start = dpStartDate.getValue();
        LocalDate end = dpEndDate.getValue();

        // Basic validation
        if (titre.isEmpty() || desc.isEmpty() || salaireStr.isEmpty() || lieu.isEmpty() || start == null || end == null) {
            new Alert(Alert.AlertType.ERROR, "Veuillez remplir tous les champs.").show();
            return;
        }

        try {
            float salaire = Float.parseFloat(salaireStr);

            OffreEmploi offre = new OffreEmploi();
            offre.setTitre(titre);
            offre.setDescription(desc);
            offre.setSalaire(salaire);
            offre.setLieu(lieu);
            offre.setStartDate(start);
            offre.setDateExpiration(end);

            // Insert into DB
            service.ajouter(offre);

            Alert alert = new Alert(Alert.AlertType.INFORMATION, "✅ Offre ajoutée avec succès !");
            alert.showAndWait();

            ((Stage) txtTitre.getScene().getWindow()).close();

        } catch (NumberFormatException e) {
            new Alert(Alert.AlertType.ERROR, "Salaire invalide.").show();
        } catch (Exception ex) {
            ex.printStackTrace();
            new Alert(Alert.AlertType.ERROR, "Erreur lors de l'ajout: " + ex.getMessage()).show();
        }
    }

    @FXML
    private void handleCancel() {
        ((Stage) txtTitre.getScene().getWindow()).close();
    }
}
