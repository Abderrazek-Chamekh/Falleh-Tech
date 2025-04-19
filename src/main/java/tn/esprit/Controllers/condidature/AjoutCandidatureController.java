package tn.esprit.Controllers.condidature;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;
import tn.esprit.entities.Candidature;
import tn.esprit.entities.OffreEmploi;
import tn.esprit.entities.StatutCandidature;
import tn.esprit.entities.User;
import tn.esprit.services.ServiceCandidature;
import tn.esprit.services.ServiceOffreEmploi;
import tn.esprit.services.UserService;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AjoutCandidatureController {

    @FXML private ComboBox<String> comboOffre;
    @FXML private ComboBox<String> comboTravailleur;
    @FXML private ComboBox<StatutCandidature> comboStatut;
    @FXML private TextArea noteField;
    @FXML private Button btnAjouter;

    @FXML private Label offreError;
    @FXML private Label travailleurError;
    @FXML private Label statutError;

    private final ServiceCandidature serviceCandidature = new ServiceCandidature();
    private final ServiceOffreEmploi serviceOffre = new ServiceOffreEmploi();
    private final UserService serviceUser = new UserService();

    private final Map<String, Integer> offreMap = new HashMap<>();
    private final Map<String, Integer> travailleurMap = new HashMap<>();

    @FXML
    public void initialize() {
        loadOffres();
        loadTravailleurs();
        comboStatut.setItems(FXCollections.observableArrayList(StatutCandidature.values()));
        comboStatut.setValue(StatutCandidature.EN_ATTENTE);

        btnAjouter.setOnAction(e -> ajouterCandidature());
    }

    private void loadOffres() {
        List<OffreEmploi> offres = serviceOffre.getAll();
        for (OffreEmploi o : offres) {
            String label = o.getTitre() + " (ID: " + o.getId() + ")";
            comboOffre.getItems().add(label);
            offreMap.put(label, o.getId());
        }
    }

    private void loadTravailleurs() {
        List<User> ouvriers = serviceUser.getAllOuvriers();
        for (User u : ouvriers) {
            String label = u.getName() + " " + u.getLastName() + " (ID: " + u.getId() + ")";
            comboTravailleur.getItems().add(label);
            travailleurMap.put(label, u.getId());
        }
    }

    @FXML
    public void ajouterCandidature() {
        // Clear error labels
        offreError.setText("");
        travailleurError.setText("");
        statutError.setText("");

        String offreLabel = comboOffre.getValue();
        String travailleurLabel = comboTravailleur.getValue();
        StatutCandidature statut = comboStatut.getValue();

        boolean hasError = false;

        if (offreLabel == null || !offreMap.containsKey(offreLabel)) {
            offreError.setText("Veuillez choisir une offre.");
            hasError = true;
        }

        if (travailleurLabel == null || !travailleurMap.containsKey(travailleurLabel)) {
            travailleurError.setText("Veuillez choisir un travailleur.");
            hasError = true;
        }

        if (statut == null) {
            statutError.setText("Veuillez choisir un statut.");
            hasError = true;
        }

        if (hasError) return;

        int offreId = offreMap.get(offreLabel);
        int travailleurId = travailleurMap.get(travailleurLabel);

        Candidature c = new Candidature();
        c.setStatut(statut);
        c.setDateApplied(LocalDateTime.now());

        serviceCandidature.ajouter(c, offreId, travailleurId);

        showAlert("✅ Candidature ajoutée avec succès !");
        Stage stage = (Stage) btnAjouter.getScene().getWindow();
        stage.close();
    }

    private void showAlert(String msg) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION, msg, ButtonType.OK);
        alert.showAndWait();
    }
}
