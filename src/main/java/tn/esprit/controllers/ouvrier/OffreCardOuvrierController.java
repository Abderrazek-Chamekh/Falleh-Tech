package tn.esprit.controllers.ouvrier;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import tn.esprit.entities.OffreEmploi;
import tn.esprit.entities.User;

public class OffreCardOuvrierController {

    @FXML private Label lblTitre;
    @FXML private Label lblDescription;
    @FXML private Label lblSalaire;
    @FXML private Label lblLocation;
    @FXML private Button btnApply;
    @FXML private Button btnContact;

    private OffreEmploi offre;
    private User currentUser;

    public void setData(OffreEmploi offre, User user) {
        this.offre = offre;
        this.currentUser = user;

        lblTitre.setText(offre.getTitre());
        lblDescription.setText(offre.getDescription());
        lblSalaire.setText(offre.getSalaire() + " DT");
        lblLocation.setText(offre.getLieu());

        btnApply.setOnAction(e -> applyToJob());
        btnContact.setOnAction(e -> contactFarmer());
    }

    private void applyToJob() {
        System.out.println("📩 " + currentUser.getName() + " applied to: " + offre.getTitre());
        // TODO: Logic to insert into candidature table
    }

    private void contactFarmer() {
        System.out.println("📞 Contacting farmer for: " + offre.getTitre());
        // TODO: Messaging system or popup
    }
}
