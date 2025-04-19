package tn.esprit.Controllers;



import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import tn.esprit.entities.Candidature;
import tn.esprit.entities.OffreEmploi;
import tn.esprit.entities.StatutCandidature;
import tn.esprit.services.ServiceCandidature;
import tn.esprit.services.ServiceOffreEmploi;

public class CandidaturePane extends VBox {

    private final ServiceCandidature service = new ServiceCandidature();
    private final ServiceOffreEmploi offreService = new ServiceOffreEmploi();

    private final TableView<Candidature> table = new TableView<>();
    private final ComboBox<String> offreCombo = new ComboBox<>();
    private final TextField statutField = new TextField("EN_ATTENTE");
    private final TextField ratingField = new TextField();
    private final TextField idField = new TextField();

    public CandidaturePane() {
        setPadding(new Insets(10));
        setSpacing(10);

        // === Form ===
        statutField.setPromptText("Statut (EN_ATTENTE / ACCEPTE / REJETE)");
        ratingField.setPromptText("Note (facultatif)");
        idField.setPromptText("ID (pour suppression)");

        chargerOffres();

        Button add = new Button("Ajouter");
        add.setOnAction(e -> ajouter());

        Button delete = new Button("Supprimer");
        delete.setOnAction(e -> supprimer());

        Button refresh = new Button("Rafraîchir");
        refresh.setOnAction(e -> rafraichir());

        HBox form = new HBox(10, new Label("Offre:"), offreCombo,
                new Label("Statut:"), statutField,
                new Label("Rating:"), ratingField,
                new Label("ID:"), idField,
                add, delete, refresh);

        // === Table ===
        TableColumn<Candidature, String> offreCol = new TableColumn<>("Offre");
        offreCol.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(
              //  data.getValue().getIdOffre() != null ? String.valueOf(data.getValue().getIdOffre().getId()) : "N/A"
        ));

        TableColumn<Candidature, String> statutCol = new TableColumn<>("Statut");
        statutCol.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(
                data.getValue().getStatut().toString()));

        TableColumn<Candidature, String> dateCol = new TableColumn<>("Date");
        dateCol.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(
                data.getValue().getDateApplied().toString()));

        TableColumn<Candidature, String> noteCol = new TableColumn<>("Note");
        noteCol.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(
                data.getValue().getRating() != null ? data.getValue().getRating().toString() : ""));

        table.getColumns().addAll(offreCol, statutCol, dateCol, noteCol);

        getChildren().addAll(form, table);
        rafraichir();
    }

    private void chargerOffres() {
        offreCombo.getItems().clear();
        for (OffreEmploi o : offreService.getAll()) {
            offreCombo.getItems().add(o.getId() + " - " + o.getTitre());
        }
    }

    private void ajouter() {
        try {
            String selected = offreCombo.getValue();
            if (selected == null) {
                showAlert("Veuillez sélectionner une offre.");
                return;
            }

            int offreId = Integer.parseInt(selected.split(" - ")[0]);

            Candidature c = new Candidature();
            c.setStatut(StatutCandidature.valueOf(statutField.getText().toUpperCase()));

            if (!ratingField.getText().isEmpty()) {
                c.setRating(Integer.parseInt(ratingField.getText()));
            }

            service.ajouter(c, offreId);
            rafraichir();
        } catch (Exception e) {
            showAlert("Erreur lors de l'ajout : " + e.getMessage());
        }
    }

    private void supprimer() {
        try {
            int id = Integer.parseInt(idField.getText());
            Candidature c = new Candidature();
            c.setId(id);
            service.supprimer(c);
            rafraichir();
        } catch (Exception e) {
            showAlert("Erreur lors de la suppression : " + e.getMessage());
        }
    }

    private void rafraichir() {
        table.getItems().setAll(service.getAll());
    }

    private void showAlert(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR, message, ButtonType.OK);
        alert.setHeaderText("Erreur");
        alert.showAndWait();
    }
}
