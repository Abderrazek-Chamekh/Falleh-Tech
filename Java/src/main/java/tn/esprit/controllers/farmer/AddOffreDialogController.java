package tn.esprit.controllers.farmer;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import tn.esprit.entities.OffreEmploi;
import tn.esprit.services.ServiceOffreEmploi;

import java.time.LocalDate;

public class AddOffreDialogController {

    @FXML private TextField txtTitre;
    @FXML private TextArea txtDescription;
    @FXML private TextField txtSalaire;
    @FXML private ComboBox<String> comboLieu;
    @FXML private DatePicker dpStartDate;
    @FXML private DatePicker dpEndDate;
    @FXML private Label errorTitre;
    @FXML private Label errorLieu;
    @FXML private Label errorStart;
    @FXML private Label errorEnd;
    @FXML private Label errorDesc;
    @FXML private Label errorSalaire;
    @FXML private HBox headerBar;
    @FXML private Button btnClose;

    private final ServiceOffreEmploi service = new ServiceOffreEmploi();

    private double xOffset = 0;
    private double yOffset = 0;

    @FXML
    public void initialize() {
        // ComboBox population
        comboLieu.getItems().addAll(
                "Tunis", "Ariana", "Ben Arous", "Manouba", "Nabeul", "Zaghouan", "Bizerte", "Béja",
                "Jendouba", "Le Kef", "Siliana", "Kairouan", "Kasserine", "Sidi Bouzid", "Sousse", "Monastir",
                "Mahdia", "Sfax", "Gafsa", "Tozeur", "Kebili", "Gabès", "Tataouine", "Médenine"
        );

        // Dragging logic
        headerBar.setOnMousePressed(e -> {
            xOffset = e.getSceneX();
            yOffset = e.getSceneY();
        });

        headerBar.setOnMouseDragged(e -> {
            headerBar.getScene().getWindow().setX(e.getScreenX() - xOffset);
            headerBar.getScene().getWindow().setY(e.getScreenY() - yOffset);
        });

        // Close dialog
        btnClose.setOnAction(e -> btnClose.getScene().getWindow().hide());
    }

    public boolean validateAndSubmit() {
        clearErrors();

        String titre = txtTitre.getText().trim();
        String desc = txtDescription.getText().trim();
        String salaireStr = txtSalaire.getText().trim();
        String lieu = comboLieu.getValue();
        LocalDate start = dpStartDate.getValue();
        LocalDate end = dpEndDate.getValue();

        boolean valid = true;

        if (titre.isEmpty()) {
            errorTitre.setText("Titre requis.");
            valid = false;
        }
        if (lieu == null || lieu.isEmpty()) {
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
            OffreEmploi offre = new OffreEmploi();
            offre.setTitre(titre);
            offre.setDescription(desc);
            offre.setSalaire(salaire);
            offre.setLieu(lieu);
            offre.setStartDate(start);
            offre.setDateExpiration(end);

            service.ajouterFromFront(16, offre);
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
