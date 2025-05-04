package tn.esprit.fx;

import javafx.animation.FadeTransition;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.util.Duration;
import tn.esprit.entities.User;

import java.io.IOException;

public class LandingController {

    @FXML private Button btnFrontOffice;
    @FXML private Button btnBackOffice;
    @FXML private VBox roleBox;

    @FXML
    private void toggleRoleSelection() {
        boolean showing = roleBox.isVisible();
        roleBox.setVisible(!showing);
        roleBox.setManaged(!showing);
    }

    @FXML
    private void toggleRoleBox() {
        boolean isVisible = roleBox.isVisible();
        roleBox.setVisible(!isVisible);
        roleBox.setManaged(!isVisible);
    }

    @FXML private void goToFrontOffice() {
        loadFrontOfficeWithRole("ouvrier");
    }

    @FXML private void goToBackOffice() {
        loadSceneWithFade("/fxml/main_layout.fxml", btnBackOffice);
    }

    @FXML private void goToClient() {
        loadFrontOfficeWithRole("client");
    }

    @FXML private void goToAgriculteur() {
        loadFrontOfficeWithRole("agriculteur");
    }

    @FXML private void goToOuvrier() {
        loadFrontOfficeWithRole("ouvrier");
    }

    private void loadSceneWithFade(String fxmlPath, Button sourceButton) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Parent nextRoot = loader.load();

            Scene scene = new Scene(nextRoot);
            scene.getStylesheets().addAll(
                    getClass().getResource("/styles/main.css").toExternalForm(),
                    getClass().getResource("/styles/dashboard.css").toExternalForm(),
                    getClass().getResource("/styles/offre.css").toExternalForm(),
                    getClass().getResource("/styles/popup.css").toExternalForm(),
                    getClass().getResource("/styles/sidebar.css").toExternalForm(),
                    getClass().getResource("/styles/job-cards.css").toExternalForm() // ✅ added here too
            );

            Stage stage = (Stage) sourceButton.getScene().getWindow();
            stage.setScene(scene);

            FadeTransition fadeIn = new FadeTransition(Duration.millis(400), nextRoot);
            fadeIn.setFromValue(0);
            fadeIn.setToValue(1);
            fadeIn.play();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void loadFrontOfficeWithRole(String role) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/front/frontview.fxml"));
            Parent root = loader.load();

            User user = new User();
            user.setRole(role);

            switch (role) {
                case "agriculteur" -> user.setId(16);
                case "ouvrier"     -> user.setId(21);
                case "client"      -> user.setId(26);
                default             -> user.setId(0);
            }

            FrontViewController controller = loader.getController();
            controller.setCurrentUser(user);

            Scene scene = new Scene(root);
            scene.getStylesheets().addAll(
                    getClass().getResource("/styles/main.css").toExternalForm(),
                    getClass().getResource("/styles/dashboard.css").toExternalForm(),
                    getClass().getResource("/styles/offre.css").toExternalForm(),
                    getClass().getResource("/styles/popup.css").toExternalForm(),
                    getClass().getResource("/styles/sidebar.css").toExternalForm(),
                    getClass().getResource("/styles/job-cards.css").toExternalForm(),
                            getClass().getResource("/styles/offre_card.css").toExternalForm()

                                    ,
                    getClass().getResource("/styles/candidatures.css").toExternalForm()
                    , getClass().getResource("/styles/add_offre_dialog.css").toExternalForm()
                    , getClass().getResource("/styles/edit_offre_dialog.css").toExternalForm()
                    // ✅ crucial for offer card styling
            );

            Stage stage = (Stage) btnFrontOffice.getScene().getWindow();
            stage.setScene(scene);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML private void goToFrontOfficeClient() { loadFrontOfficeWithRole("client"); }
    @FXML private void goToFrontOfficeAgriculteur() { loadFrontOfficeWithRole("agriculteur"); }
    @FXML private void goToFrontOfficeOuvrier() { loadFrontOfficeWithRole("ouvrier"); }
}
