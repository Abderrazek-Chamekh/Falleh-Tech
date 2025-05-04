package tn.esprit.controllers;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.SimpleObjectProperty;

import java.io.IOException;
import java.time.LocalDate;

import tn.esprit.entities.Candidature;
import tn.esprit.entities.OffreEmploi;
import tn.esprit.services.ServiceCandidature;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.control.ContentDisplay;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class CandidatureController {

    @FXML private TableView<Candidature> tableCandidature;
    @FXML private TableColumn<Candidature, Integer> colId;
    @FXML private TableColumn<Candidature, String> colNom;
    @FXML private TableColumn<Candidature, String> colEmail;
    @FXML private TableColumn<Candidature, String> colOffre;
    @FXML private TableColumn<Candidature, LocalDate> colDate;
    @FXML private TableColumn<Candidature, Void> colActions;
    @FXML private TableColumn<Candidature, String> colStatut;

    private final ServiceCandidature service = new ServiceCandidature();

    @FXML
    public void initialize() {
        colId.setCellValueFactory(data -> new SimpleObjectProperty<>(data.getValue().getId()));
        colNom.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getNom()));
        colEmail.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getEmail()));
        colStatut.setCellValueFactory(data ->
                new SimpleStringProperty(data.getValue().getStatut() != null ? data.getValue().getStatut().name() : "N/A")
        );

        // ✅ Safe null check for OffreEmploi
        colOffre.setCellValueFactory(data -> {
            OffreEmploi offre = data.getValue().getOffre();
            if (offre != null) {
                return new SimpleStringProperty(offre.getTitre());
            } else {
                System.out.println("⚠️ OffreEmploi null pour candidature ID = " + data.getValue().getId());
                return new SimpleStringProperty("Aucune offre");
            }
        });

        colDate.setCellValueFactory(data -> new SimpleObjectProperty<>(data.getValue().getDateCandidature()));

        setupActionsColumn();
        loadCandidatures();
    }

    private void loadCandidatures() {
        ObservableList<Candidature> list = FXCollections.observableArrayList(service.getAll());
        tableCandidature.setItems(list);
    }

    private void setupActionsColumn() {
        colActions.setCellFactory(param -> new TableCell<>() {
            private final Button btnEdit = new Button();
            private final Button btnDelete = new Button();
            private final HBox box = new HBox(10, btnEdit, btnDelete);

            {
                ImageView editIcon = new ImageView(new Image(getClass().getResourceAsStream("/images/edit-button.png")));
                ImageView deleteIcon = new ImageView(new Image(getClass().getResourceAsStream("/images/delete.png")));

                editIcon.setFitHeight(16); editIcon.setFitWidth(16);
                deleteIcon.setFitHeight(16); deleteIcon.setFitWidth(16);

                btnEdit.setGraphic(editIcon);
                btnDelete.setGraphic(deleteIcon);

                btnEdit.setText("");
                btnDelete.setText("");

                btnEdit.setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
                btnDelete.setContentDisplay(ContentDisplay.GRAPHIC_ONLY);

                btnEdit.getStyleClass().add("btn-edit");
                btnDelete.getStyleClass().add("btn-delete");

                btnEdit.setTooltip(new Tooltip("Modifier la candidature"));
                btnDelete.setTooltip(new Tooltip("Supprimer la candidature"));

                box.setAlignment(Pos.CENTER);

                btnEdit.setOnAction(e -> {
                    Candidature c = getTableView().getItems().get(getIndex());
                    System.out.println("✏️ Modifier: " + c.getId());
                    try {
                        FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/edit_candidature.fxml"));
                        Parent root = loader.load();

                        EditCandidatureController ctrl = loader.getController();
                        ctrl.setCandidature(c);

                        Stage popup = new Stage();
                        popup.setTitle("Modifier Candidature");
                        popup.setScene(new Scene(root));
                        popup.showAndWait();

                        loadCandidatures(); // refresh table after editing
                    } catch (IOException ex) {
                        ex.printStackTrace();
                    }

                });

                btnDelete.setOnAction(e -> {
                    Candidature c = getTableView().getItems().get(getIndex());
                    Alert confirm = new Alert(Alert.AlertType.CONFIRMATION, "Supprimer cette candidature ?", ButtonType.YES, ButtonType.NO);
                    confirm.showAndWait().ifPresent(response -> {
                        if (response == ButtonType.YES) {
                            service.supprimer(c);
                            loadCandidatures();
                        }
                    });
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : box);
            }
        });
    }
    @FXML
    private void onAddCandidature() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/add_candidature.fxml"));
            Parent root = loader.load();

            Stage popup = new Stage();
            popup.setTitle("Ajouter une Candidature");
            popup.setScene(new Scene(root));
            popup.showAndWait();

            loadCandidatures(); // refresh table
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


}
