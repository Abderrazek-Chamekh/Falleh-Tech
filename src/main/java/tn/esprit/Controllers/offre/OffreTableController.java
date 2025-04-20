package tn.esprit.Controllers.offre;


import javafx.beans.property.SimpleFloatProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import tn.esprit.Controllers.offre.AjoutOffreController;
import tn.esprit.entities.OffreEmploi;
import tn.esprit.services.ServiceOffreEmploi;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import java.io.IOException;

import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.control.ContentDisplay;





// JavaFX layout and UI
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.StageStyle;

import java.time.LocalDate;

public class OffreTableController {

    @FXML private TableView<OffreEmploi> offreTable;
    @FXML private TableColumn<OffreEmploi, Integer> idColumn;
    @FXML private TableColumn<OffreEmploi, String> titreColumn;
    @FXML private TableColumn<OffreEmploi, String> lieuColumn;
    @FXML private TableColumn<OffreEmploi, Float> salaireColumn;
    @FXML private TableColumn<OffreEmploi, LocalDate> dateExpirationColumn;
    @FXML private TableColumn<OffreEmploi, Void> actionsColumn;

    private final ServiceOffreEmploi service = new ServiceOffreEmploi();

    @FXML
    public void initialize() {
        idColumn.setCellValueFactory(data -> new SimpleIntegerProperty(data.getValue().getId()).asObject());
        titreColumn.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getTitre()));
        lieuColumn.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getLieu()));
        salaireColumn.setCellValueFactory(data -> new SimpleFloatProperty(data.getValue().getSalaire()).asObject());
        dateExpirationColumn.setCellValueFactory(data -> new SimpleObjectProperty<>(data.getValue().getDateExpiration()));

        setupActionsColumn();
        loadOffres();
    }

    private void loadOffres() {
        ObservableList<OffreEmploi> offres = FXCollections.observableArrayList(service.getAll());
        offreTable.setItems(offres);
    }

    private void setupActionsColumn() {
        actionsColumn.setCellFactory(param -> new TableCell<>() {
            private final Button showButton = new Button();
            private final Button editButton = new Button();
            private final Button deleteButton = new Button();
            private final HBox hbox = new HBox(10, showButton, editButton, deleteButton);

            {
                // Load icons
                ImageView eyeIcon = new ImageView(new Image(getClass().getResourceAsStream("/images/view.png")));
                ImageView editIcon = new ImageView(new Image(getClass().getResourceAsStream("/images/edit-button.png")));
                ImageView deleteIcon = new ImageView(new Image(getClass().getResourceAsStream("/images/delete.png")));

                // Set size
                eyeIcon.setFitHeight(16); eyeIcon.setFitWidth(16);
                editIcon.setFitHeight(16); editIcon.setFitWidth(16);
                deleteIcon.setFitHeight(16); deleteIcon.setFitWidth(16);

                // Set icons on buttons
                showButton.setGraphic(eyeIcon);
                editButton.setGraphic(editIcon);
                deleteButton.setGraphic(deleteIcon);

                // No text, graphic only
                showButton.setText("");
                editButton.setText("");
                deleteButton.setText("");

                showButton.setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
                editButton.setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
                deleteButton.setContentDisplay(ContentDisplay.GRAPHIC_ONLY);

                // Add style classes
                showButton.getStyleClass().add("btn-show");
                editButton.getStyleClass().add("btn-edit");
                deleteButton.getStyleClass().add("btn-delete");

                // Tooltips
                showButton.setTooltip(new Tooltip("Voir l'offre"));
                editButton.setTooltip(new Tooltip("Modifier l'offre"));
                deleteButton.setTooltip(new Tooltip("Supprimer l'offre"));

                // Layout
                hbox.setAlignment(Pos.CENTER);

                // Actions
                showButton.setOnAction(e -> {
                    OffreEmploi offre = getTableView().getItems().get(getIndex());
                    showDetailsPopup(offre);
                });

                editButton.setOnAction(e -> {
                    OffreEmploi offre = getTableView().getItems().get(getIndex());
                    try {
                        FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/ajout_offre.fxml"));
                        Parent root = loader.load();

                        AjoutOffreController controller = loader.getController();
                        controller.setOffreToEdit(offre); // pre-fill the form

                        Stage stage = new Stage();
                        stage.setTitle("Modifier l'offre");
                        stage.setScene(new Scene(root));
                        stage.showAndWait();

                        loadOffres(); // refresh table
                    } catch (IOException ex) {
                        ex.printStackTrace();
                    }
                });

                deleteButton.setOnAction(e -> {
                    OffreEmploi offre = getTableView().getItems().get(getIndex());
                    System.out.println("🗑️ Suppression demandée pour l'offre ID: " + offre.getId() + ", Titre: " + offre.getTitre());
                    showCustomConfirmPopup(offre);
                });



            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : hbox);
            }
        });
    }


    private void showDetailsPopup(OffreEmploi offre) {
        Stage popup = new Stage();
        popup.initModality(Modality.APPLICATION_MODAL);
        popup.setTitle("Détails de l'offre");

        VBox content = new VBox(10);
        content.getStyleClass().add("custom-details-popup");
        content.setPadding(new Insets(20));

        Label title = new Label("📌 " + offre.getTitre());
        title.getStyleClass().add("popup-title");

        Label lieu = new Label("📍 Lieu: " + offre.getLieu());
        Label salaire = new Label("💰 Salaire: " + String.format("%.2f DT", offre.getSalaire()));
        Label date = new Label("📅 Date d'expiration: " + offre.getDateExpiration());
        Label descTitle = new Label("📝 Description:");
        Label description = new Label(offre.getDescription());

        lieu.getStyleClass().add("popup-line");
        salaire.getStyleClass().add("popup-line");
        date.getStyleClass().add("popup-line");
        descTitle.getStyleClass().add("popup-line");
        description.getStyleClass().add("popup-desc");

        Button close = new Button("Fermer");
        close.getStyleClass().add("btn-green");
        close.setOnAction(e -> popup.close());

        content.getChildren().addAll(title, lieu, salaire, date, descTitle, description, close);
        content.setAlignment(Pos.CENTER_LEFT);

        Scene scene = new Scene(content, 500, 300);
        scene.getStylesheets().add(getClass().getResource("/styles/popup.css").toExternalForm());

        popup.setScene(scene);
        popup.initStyle(StageStyle.UNDECORATED); // ✅ for custom rounded look
        popup.show();
    }



    @FXML
    private void onAddOffre() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/ajout_offre.fxml"));
            Parent root = loader.load();

            Scene scene = new Scene(root);

            // ✅ Add popup CSS manually
            scene.getStylesheets().add(getClass().getResource("/styles/popup.css").toExternalForm());

            Stage popupStage = new Stage();
            popupStage.setTitle("Modifier une Offre");
            popupStage.setScene(scene);
            popupStage.centerOnScreen(); // Optional
            popupStage.show();

        } catch (IOException ex) {
            ex.printStackTrace();
        }

    }
    private void showCustomConfirmPopup(OffreEmploi offre) {
        Stage confirmStage = new Stage();
        confirmStage.initModality(Modality.APPLICATION_MODAL);
        confirmStage.setTitle("Supprimer Offre");

        VBox container = new VBox(15);
        container.setPadding(new Insets(25));
        container.setAlignment(Pos.CENTER_LEFT);
        container.getStyleClass().add("confirm-popup");

        Label header = new Label("❗ Supprimer l'Offre ?");
        header.getStyleClass().add("confirm-title");

        Label msg = new Label("Voulez-vous vraiment supprimer l'offre : " + offre.getTitre() + " ?");
        msg.getStyleClass().add("confirm-message");

        HBox buttons = new HBox(15);
        buttons.setAlignment(Pos.CENTER_RIGHT);
        buttons.setPadding(new Insets(10, 0, 0, 0));

        Button btnOui = new Button("Oui");
        Button btnNon = new Button("Non");

        btnOui.getStyleClass().add("confirm-btn-green");
        btnNon.getStyleClass().add("confirm-btn-red");

        btnOui.setOnAction(e -> {
            service.supprimer(offre);
            loadOffres();
            confirmStage.close();
        });

        btnNon.setOnAction(e -> confirmStage.close());

        buttons.getChildren().addAll(btnOui, btnNon);
        container.getChildren().addAll(header, msg, buttons);

        Scene scene = new Scene(container, 450, 160);
        scene.getStylesheets().add(getClass().getResource("/styles/confirm.css").toExternalForm());

        confirmStage.setScene(scene);
        confirmStage.initStyle(StageStyle.UNDECORATED); // smooth, clean
        confirmStage.showAndWait();
    }

}