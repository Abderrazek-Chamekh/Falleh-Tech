package tn.esprit.Controllers.front.farmer;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.StackPane;
import tn.esprit.entities.OffreEmploi;
import tn.esprit.entities.User;
import tn.esprit.services.ServiceOffreEmploi;

public class OffreCardController {

    @FXML private Label lblTitre;
    @FXML private Label lblDescription;
    @FXML private Label lblSalaire;
    @FXML private Label lblLocation;
    @FXML private Button btnCandidatures;
    @FXML private Button btnEdit;
    @FXML private Button btnDelete;

    private OffreEmploi offre;
    private StackPane contentPaneRef;
    private User currentUser;
    private final ServiceOffreEmploi service = new ServiceOffreEmploi();

    public void setContentPaneRef(StackPane pane) {
        this.contentPaneRef = pane;
    }

    public void setCurrentUser(User user) {
        this.currentUser = user;
    }

    public void setData(OffreEmploi o) {
        this.offre = o;
        lblTitre.setText(o.getTitre());
        lblDescription.setText(o.getDescription());
        lblSalaire.setText(o.getSalaire() + " TND");
        if (lblLocation != null && o.getLieu() != null) {
            lblLocation.setText(o.getLieu());
        }
    }

    @FXML
    public void initialize() {
        if (btnCandidatures != null) {
            btnCandidatures.setOnAction(e -> openCandidaturesView());
        }

        if (btnEdit != null) {
            btnEdit.setOnAction(e -> editOffer());
        }

        if (btnDelete != null) {
            btnDelete.setOnAction(e -> deleteOffer());
        }
    }

    private void openCandidaturesView() {
        if (contentPaneRef == null) {
            System.out.println("❌ contentPaneRef is NULL");
            return;
        }
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/front/farmer_front/CandidaturesView.fxml"));
            Parent view = loader.load();
            CandidaturesController controller = loader.getController();
            controller.setContentPaneRef(contentPaneRef);
            controller.setOffre(offre);
            contentPaneRef.getChildren().setAll(view);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void deleteOffer() {
        if (offre != null) {
            service.supprimer(offre);

            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Suppression");
            alert.setContentText("✅ Offre supprimée avec succès !");
            alert.showAndWait();

            reloadMesOffresView();
        }
    }

    private void editOffer() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/front/farmer_front/EditOffreDialog.fxml"));
            ScrollPane content = loader.load();

            EditOffreDialogController controller = loader.getController();
            controller.setOffre(offre);
            controller.setCurrentUser(currentUser); // ✅ Pass current user for employer assignment
            controller.setContentPaneRef(contentPaneRef);

            Dialog<ButtonType> dialog = new Dialog<>();
            dialog.setTitle("Modifier l'offre");
            dialog.getDialogPane().setContent(content);
            dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
            dialog.getDialogPane().getStylesheets().add(getClass().getResource("/styles/add_offre_dialog.css").toExternalForm());

            dialog.setResultConverter(result -> {
                if (result == ButtonType.OK) {
                    controller.handleSave(); // the view is refreshed inside
                    reloadMesOffresView();
                }
                return null;
            });

            dialog.showAndWait();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void reloadMesOffresView() {
        if (contentPaneRef != null && currentUser != null) {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/front/farmer_front/MesOffresView.fxml"));
                Parent refreshedView = loader.load();

                MesOffresController controller = loader.getController();
                controller.setCurrentUser(currentUser);
                controller.setContentPaneRef(contentPaneRef);

                contentPaneRef.getChildren().setAll(refreshedView);
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            System.err.println("❌ Cannot reload MesOffresView — contentPaneRef or currentUser is null.");
        }
    }
}
