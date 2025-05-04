package tn.esprit.controllers.ouvrier;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;
import javafx.scene.layout.FlowPane;
import tn.esprit.entities.Candidature;
import tn.esprit.entities.OffreEmploi;
import tn.esprit.entities.StatutCandidature;
import tn.esprit.entities.User;
import tn.esprit.entities.GeneralNotification;
import tn.esprit.services.ServiceCandidature;
import tn.esprit.services.ServiceOffreEmploi;
import tn.esprit.services.ServiceGeneralNotification;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

public class OffresOuvrierController {

    @FXML private FlowPane cardContainer;
    @FXML private TextField searchField;
    @FXML private ComboBox<String> statusFilter;

    private final ServiceOffreEmploi serviceOffre = new ServiceOffreEmploi();
    private final ServiceCandidature serviceCandidature = new ServiceCandidature();
    private final ServiceGeneralNotification notificationService = new ServiceGeneralNotification();

    private User currentUser;
    private List<OffreEmploi> allOffers;

    public void setCurrentUser(User user) {
        this.currentUser = user;
        loadOffers();
        setupSearchAndFilter();
    }

    private void loadOffers() {
        allOffers = serviceOffre.getAll();
        displayOffers(allOffers);
    }

    private void displayOffers(List<OffreEmploi> offers) {
        cardContainer.getChildren().clear();
        for (OffreEmploi offre : offers) {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/front/ouvrier_front/card_offre_ouvrier.fxml"));
                Node card = loader.load();

                OffreCardOuvrierController controller = loader.getController();
                controller.setData(offre, currentUser);
                cardContainer.getChildren().add(card);

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void setupSearchAndFilter() {
        statusFilter.setItems(FXCollections.observableArrayList("All", "Disponibles", "EN_ATTENTE", "ACCEPTEE", "REFUSEE", "TERMINEE"));
        statusFilter.setValue("All");

        searchField.textProperty().addListener((obs, oldVal, newVal) -> filterOffers());
        statusFilter.valueProperty().addListener((obs, oldVal, newVal) -> filterOffers());
    }

    private void filterOffers() {
        String keyword = searchField.getText().toLowerCase().trim();
        String selected = statusFilter.getValue();

        List<OffreEmploi> filtered = allOffers.stream().filter(o -> {
            boolean matchesTitle = o.getTitre().toLowerCase().contains(keyword);
            Candidature c = serviceCandidature.getCandidatureByUserAndOffre(currentUser.getId(), o.getId());

            if ("All".equals(selected)) {
                return matchesTitle;
            } else if ("Disponibles".equals(selected)) {
                return matchesTitle && c == null;
            } else if (c != null) {
                return matchesTitle && c.getStatut().name().equalsIgnoreCase(selected);
            } else {
                return false;
            }
        }).collect(Collectors.toList());

        displayOffers(filtered);

        // ✅ Send a notification if new offers are found
        if (!filtered.isEmpty()) {
            GeneralNotification notif = new GeneralNotification(currentUser.getId(), "📢 Nouvelle(s) offre(s) disponible(s) !");
            notificationService.add(notif);
        }
    }
}
