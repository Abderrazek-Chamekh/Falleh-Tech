package tn.esprit.controllers.farmer;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import tn.esprit.entities.OffreEmploi;
import tn.esprit.services.ServiceOffreEmploi;

public class OffreCardController {

    @FXML private Label lblTitre;
    @FXML private Label lblDescription;
    @FXML private Label lblSalaire;
    @FXML private Label lblLocation;

    @FXML private Button btnView;    // Only present in All Offers card
    @FXML private Button btnEdit;    // Only present in My Offers card
    @FXML private Button btnDelete;  // Only present in My Offers card

    private OffreEmploi offre;
    private final ServiceOffreEmploi service = new ServiceOffreEmploi();

    public void setData(OffreEmploi offre) {
        this.offre = offre;

        if (lblTitre != null) lblTitre.setText(offre.getTitre());
        if (lblDescription != null) lblDescription.setText(offre.getDescription());
        if (lblSalaire != null) lblSalaire.setText(offre.getSalaire() + " DT");
        if (lblLocation != null) lblLocation.setText(offre.getLieu());

        if (btnView != null) {
            btnView.setOnAction(e -> viewDetails());
        }

        if (btnEdit != null) {
            btnEdit.setOnAction(e -> editOffre());
        }

        if (btnDelete != null) {
            btnDelete.setOnAction(e -> deleteOffre());
        }
    }

    private void viewDetails() {
        System.out.println("📄 Viewing details for: " + offre.getTitre());
        // TODO: Switch to OffreDetailsView
    }

    private void editOffre() {
        System.out.println("✏️ Editing offer ID: " + offre.getId());
        // TODO: Open edit dialog and reload view after save
    }

    private void deleteOffre() {
        System.out.println("🗑️ Deleting offer ID: " + offre.getId());
        service.supprimer(offre);
        // Optional: refresh offers list from MesOffresController if accessible
    }
}
