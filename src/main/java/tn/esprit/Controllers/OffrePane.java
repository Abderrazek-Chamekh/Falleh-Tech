package tn.esprit.Controllers;

import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import tn.esprit.entities.OffreEmploi;
import tn.esprit.services.ServiceOffreEmploi;

public class OffrePane extends VBox {

    private final ServiceOffreEmploi service = new ServiceOffreEmploi();
    private final TableView<OffreEmploi> table = new TableView<>();

    public OffrePane() {
        setPadding(new Insets(10));
        setSpacing(10);

        // === Form Fields ===
        TextField titre = new TextField();
        titre.setPromptText("Titre");

        TextField description = new TextField();
        description.setPromptText("Description");

        TextField salaire = new TextField();
        salaire.setPromptText("Salaire");

        TextField lieu = new TextField();
        lieu.setPromptText("Lieu");

        Button add = new Button("Ajouter");
        add.setOnAction(e -> {
            OffreEmploi o = new OffreEmploi();
            o.setTitre(titre.getText());
            o.setDescription(description.getText());
            o.setSalaire(Float.parseFloat(salaire.getText()));
            o.setLieu(lieu.getText());

            service.ajouter(o);
            loadOffres();
        });

        HBox form = new HBox(10, titre, description, salaire, lieu, add);

        // === Table Columns ===
        TableColumn<OffreEmploi, String> titreCol = new TableColumn<>("Titre");
        titreCol.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(data.getValue().getTitre()));

        TableColumn<OffreEmploi, String> descCol = new TableColumn<>("Description");
        descCol.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(data.getValue().getDescription()));

        TableColumn<OffreEmploi, String> lieuCol = new TableColumn<>("Lieu");
        lieuCol.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(data.getValue().getLieu()));

        table.getColumns().addAll(titreCol, descCol, lieuCol);

        getChildren().addAll(form, table);
        loadOffres();
    }

    private void loadOffres() {
        table.getItems().setAll(service.getAll());
    }
}
