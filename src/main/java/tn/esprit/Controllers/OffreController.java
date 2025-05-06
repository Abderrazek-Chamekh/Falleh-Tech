package tn.esprit.Controllers;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import tn.esprit.entities.OffreEmploi;
import tn.esprit.services.ServiceOffreEmploi;

public class OffreController {

    @FXML private TextField titreField;
    @FXML private TextField descriptionField;
    @FXML private TextField salaireField;
    @FXML private TextField lieuField;
    @FXML private TableView<OffreEmploi> offreTable;
    @FXML private TableColumn<OffreEmploi, String> titreCol;
    @FXML private TableColumn<OffreEmploi, String> descriptionCol;
    @FXML private TableColumn<OffreEmploi, String> lieuCol;

    private final ServiceOffreEmploi service = new ServiceOffreEmploi();

    @FXML
    public void initialize() {
        titreCol.setCellValueFactory(new PropertyValueFactory<>("titre"));
        descriptionCol.setCellValueFactory(new PropertyValueFactory<>("description"));
        lieuCol.setCellValueFactory(new PropertyValueFactory<>("lieu"));
        refreshTable();
    }

    @FXML
    private void ajouterOffre() {
        OffreEmploi o = new OffreEmploi();
        o.setTitre(titreField.getText());
        o.setDescription(descriptionField.getText());
        o.setSalaire(Float.parseFloat(salaireField.getText()));
        o.setLieu(lieuField.getText());
        service.ajouter(o);
        refreshTable();
    }

    private void refreshTable() {
        offreTable.getItems().setAll(service.getAll());
    }
}
