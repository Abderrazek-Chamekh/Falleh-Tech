package tn.esprit.fx;

import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.stage.Stage;

public class AddWidgetDialogController {

    @FXML
    private ComboBox<String> widgetTypeCombo;

    private AccueilController accueilController;

    public void setAccueilController(AccueilController controller) {
        this.accueilController = controller;
    }

    @FXML
    public void initialize() {
        widgetTypeCombo.getItems().addAll(
                "🛒 Produits",
                "🌾 Offres",
                "📰 Blog",
                "📊 Stats"
        );
        widgetTypeCombo.getSelectionModel().selectFirst();
    }

    @FXML
    private void onAdd() {
        String selectedType = widgetTypeCombo.getValue();
        if (selectedType != null && accueilController != null) {
            System.out.println("🔁 Adding widget: " + selectedType);
           // accueilController.addWidgetByType(selectedType); // ✅ Call the method in AccueilController
        } else {
            System.out.println("❌ Either no type selected or controller is null");
        }

        closeDialog();
    }

    @FXML
    private void onCancel() {
        closeDialog();
    }

    private void closeDialog() {
        Stage stage = (Stage) widgetTypeCombo.getScene().getWindow();
        stage.close();
    }
}
