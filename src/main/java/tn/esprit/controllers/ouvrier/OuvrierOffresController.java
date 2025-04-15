package tn.esprit.controllers.ouvrier;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.VBox;
import tn.esprit.entities.OffreEmploi;
import tn.esprit.entities.User;
import tn.esprit.services.ServiceOffreEmploi;

import java.io.IOException;
import java.util.List;

public class OuvrierOffresController {

    @FXML private FlowPane offersGrid;

    private final ServiceOffreEmploi service = new ServiceOffreEmploi();
    private User currentUser;

    public void setCurrentUser(User user) {
        this.currentUser = user;
        loadOffers();
    }

    private void loadOffers() {
        List<OffreEmploi> offres = service.getAll();
        offersGrid.getChildren().clear();

        for (OffreEmploi offre : offres) {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/front/ouvrier_front/card_offre_ouvrier.fxml"));
                VBox card = loader.load();

                OffreCardOuvrierController controller = loader.getController();
                controller.setData(offre, currentUser);

                offersGrid.getChildren().add(card);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
