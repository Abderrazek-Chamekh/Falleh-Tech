package tn.esprit.Controllers;

import javafx.animation.FadeTransition;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.util.Duration;

import java.io.IOException;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class MainController {

    @FXML private BorderPane rootLayout;
    @FXML private VBox sidebar;
    @FXML private AnchorPane mainContent;
    @FXML private ImageView profileImage;
    @FXML private Label navTitle;

    @FXML private Button btnDashboard, btnOffres, btnSettings, btnCandidature, btnCommande, btnProduit, btnBlog, btnLogout, toggleSidebarBtn;

    private boolean sidebarVisible = true;
    private Button activeButton = null;

    public void initialize() {
        loadInitialPage();
        loadProfileImage();
        sidebar.setTranslateX(0);
    }

    private void loadInitialPage() {
        setNavTitle("🏠 Dashboard");
        loadPage("/fxml/tajrba file.fxml");
        setActive(btnDashboard);
    }

    private void loadProfileImage() {
        Image img = new Image(getClass().getResource("/images/icons8_james_bond_32px.png").toExternalForm());
        profileImage.setImage(img);
    }

    private void loadPage(String path) {
        try {
            Parent newPage = FXMLLoader.load(getClass().getResource(path));

            FadeTransition fadeOut = new FadeTransition(Duration.millis(150), mainContent);
            fadeOut.setFromValue(1.0);
            fadeOut.setToValue(0.0);

            fadeOut.setOnFinished(event -> {
                mainContent.getChildren().setAll(newPage);
                AnchorPane.setTopAnchor(newPage, 0.0);
                AnchorPane.setBottomAnchor(newPage, 0.0);
                AnchorPane.setLeftAnchor(newPage, 0.0);
                AnchorPane.setRightAnchor(newPage, 0.0);

                FadeTransition fadeIn = new FadeTransition(Duration.millis(200), mainContent);
                fadeIn.setFromValue(0.0);
                fadeIn.setToValue(1.0);
                fadeIn.play();
            });

            fadeOut.play();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void toggleSidebar() {
        double targetWidth = sidebarVisible ? 0 : 240;
        double targetOpacity = sidebarVisible ? 0 : 1;

        Timeline timeline = new Timeline(
                new KeyFrame(Duration.ZERO,
                        new KeyValue(sidebar.prefWidthProperty(), sidebar.getWidth()),
                        new KeyValue(sidebar.opacityProperty(), sidebar.getOpacity())
                ),
                new KeyFrame(Duration.millis(300),
                        new KeyValue(sidebar.prefWidthProperty(), targetWidth),
                        new KeyValue(sidebar.opacityProperty(), targetOpacity)
                )
        );

        if (sidebarVisible) {
            timeline.setOnFinished(e -> rootLayout.setLeft(null));
        } else {
            rootLayout.setLeft(sidebar);
        }

        timeline.play();
        sidebarVisible = !sidebarVisible;
    }

    private void setActive(Button newActive) {
        if (activeButton != null) {
            activeButton.getStyleClass().remove("active");
        }
        activeButton = newActive;
        if (!activeButton.getStyleClass().contains("active")) {
            activeButton.getStyleClass().add("active");
        }
    }

    private void setNavTitle(String title) {
        navTitle.setText(title);
    }

    @FXML private void goToDashboard() {
        setNavTitle("🏠 Dashboard");
        loadPage("/fxml/dashboard.fxml");
        setActive(btnDashboard);
    }

    @FXML private void goToOffres() {
        setNavTitle("📋 Offres");
        loadPage("/fxml/tajrba file.fxml");
        setActive(btnOffres);
    }

    @FXML private void goToSettings() {
        setNavTitle("⚙️ Paramètres");
        loadPage("/fxml/settings.fxml");
        setActive(btnSettings);
    }

    @FXML private void goToCandidature() {
        setNavTitle("📋 Candidature");
        loadPage("/fxml/candidature.fxml");
        setActive(btnCandidature);
    }

    @FXML private void goToCommande() {
        setNavTitle("📋 Commande");
        loadPage("/fxml/commande.fxml");
        setActive(btnCommande);
    }

    @FXML private void goToProduit() {
        setNavTitle("📋 Produit");
        loadPage("/fxml/produit.fxml");
        setActive(btnProduit);
    }

    @FXML private void goToBlog() {
        setNavTitle("📋 Blog");
        loadPage("/fxml/blog.fxml");
        setActive(btnBlog);
    }



    public AnchorPane getMainContent() {
        return mainContent;
    }
    @FXML
    private void goToLogout() {
        try {
            Parent landingRoot = FXMLLoader.load(getClass().getResource("/views/User/Authentication/Login.fxml")); // ✅ adjust path if needed
            Scene landingScene = new Scene(landingRoot);

            // Apply stylesheets like in GoogleDrive.java if needed
            landingScene.getStylesheets().add(getClass().getResource("/styles/main.css").toExternalForm());
            landingScene.getStylesheets().add(getClass().getResource("/styles/dashboard.css").toExternalForm());
            landingScene.getStylesheets().add(getClass().getResource("/styles/sidebar.css").toExternalForm());

            Stage stage = (Stage) mainContent.getScene().getWindow();
            stage.setScene(landingScene);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    @FXML
    private void goToUsers() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/UserListView.fxml"));
            Parent view = loader.load();
            mainContent.getChildren().setAll(view);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


}
