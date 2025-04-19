package tn.esprit.Controllers.condidature;

import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import tn.esprit.entities.Candidature;
import tn.esprit.entities.StatutCandidature;
import tn.esprit.services.ServiceCandidature;

public class EditCandidatureController {

    @FXML private ComboBox<StatutCandidature> statutCombo;
    @FXML private TextField ratingField;

    private Candidature candidature;
    private final ServiceCandidature service = new ServiceCandidature();

    public void setCandidature(Candidature c) {
        this.candidature = c;
        statutCombo.getItems().setAll(StatutCandidature.values());
        statutCombo.setValue(c.getStatut());

        if (c.getRating() != null) {
            ratingField.setText(c.getRating().toString());
        }
    }

    @FXML
    private void onSave() {
        candidature.setStatut(statutCombo.getValue());
        try {
            int rating = Integer.parseInt(ratingField.getText());
            candidature.setRating(rating);
        } catch (NumberFormatException e) {
            candidature.setRating(null); // Optional rating
        }

        service.modifier(candidature);
        ((Stage) statutCombo.getScene().getWindow()).close();
    }

    @FXML
    private void onCancel() {
        ((Stage) statutCombo.getScene().getWindow()).close();
    }
}
