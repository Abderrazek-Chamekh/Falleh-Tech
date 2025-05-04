package tn.esprit.controllers.farmer;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.*;
import javafx.scene.layout.StackPane;
import javafx.scene.Parent;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ScrollPane;
import tn.esprit.entities.OffreEmploi;
import tn.esprit.services.ServiceCandidature;
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

    private final ServiceOffreEmploi service = new ServiceOffreEmploi();

    // 🔧 Setter for external pane
    public void setContentPaneRef(StackPane pane) {
        this.contentPaneRef = pane;
    }

    // 🔧 Setter for offre (also updates labels)
    public void setOffre(OffreEmploi offre) {
        this.offre = offre;
        if (lblTitre != null) lblTitre.setText(offre.getTitre());
        if (lblDescription != null) lblDescription.setText(offre.getDescription());
        if (lblSalaire != null) lblSalaire.setText(String.format("%.2f TND", offre.getSalaire()));
        if (lblLocation != null) lblLocation.setText(offre.getLieu());
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
        if (contentPaneRef == null || offre == null) {
            System.out.println("❌ contentPaneRef or offre is NULL");
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

    private void editOffer() {
        if (offre == null) return;

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/front/farmer_front/EditOffreDialog.fxml"));
            ScrollPane content = loader.load();

            EditOffreDialogController controller = loader.getController();
            controller.setOffre(offre);
            controller.setContentPaneRef(contentPaneRef);

            Dialog<ButtonType> dialog = new Dialog<>();
            dialog.setTitle("Modifier l'offre");
            dialog.getDialogPane().setContent(content);
            dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
            dialog.getDialogPane().getStylesheets().add(getClass().getResource("/styles/add_offre_dialog.css").toExternalForm());

            dialog.setResultConverter(result -> {
                if (result == ButtonType.OK) {
                    controller.handleSave();
                }
                return null;
            });

            dialog.showAndWait();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void deleteOffer() {
        if (offre == null) return;

        service.supprimer(offre);
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Suppression");
        alert.setContentText("✅ Offre supprimée avec succès !");
        alert.showAndWait();

        if (contentPaneRef != null) contentPaneRef.getChildren().clear();
    }
    public void setData(OffreEmploi offre) {
        lblTitre.setText(offre.getTitre());
        lblDescription.setText(offre.getDescription());
        lblSalaire.setText(String.valueOf(offre.getSalaire()));
        lblLocation.setText(offre.getLieu());
    }
}
