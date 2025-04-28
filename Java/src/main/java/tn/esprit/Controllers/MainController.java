package tn.esprit.Controllers;

import javafx.animation.*;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.StageStyle;
import javafx.util.Duration;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import tn.esprit.entities.Notification;
import tn.esprit.services.ServiceNotification;

public class MainController {

    @FXML private BorderPane rootLayout;
    @FXML private VBox sidebar;
    @FXML private AnchorPane mainContent;
    @FXML private ImageView profileImage;
    @FXML private Label navTitle;
    @FXML private Button btnCategorie, btnSousCategorie;
    @FXML private VBox produitsSubmenu;
    @FXML private Button btnToggleProduits;

    @FXML private Button btnDashboard, btnOffres, btnUsers, btnCandidature, btnCommande, btnProduit, btnBlog, btnLogout, toggleSidebarBtn;

    private boolean sidebarVisible = true;
    private Button activeButton = null;
    @FXML private Button notificationBtn;
    private final ServiceNotification notificationService = new ServiceNotification();
    private int notificationCount = 0;

    public void initialize() {
        loadInitialPage();
        loadProfileImage();
        sidebar.setTranslateX(0);

        updateNotificationBadge();

        setupNotificationButton();
    }
    private void updateNotificationBadge() {
        try {
            notificationCount = notificationService.getUnreadNotificationCount();
            if (notificationCount > 0) {
                notificationBtn.setText("🔔 (" + notificationCount + ")");
            } else {
                notificationBtn.setText("🔔");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void setupNotificationButton() {
        // Add style class
        notificationBtn.getStyleClass().add("notification-btn");

        // Add tooltip
        Tooltip tooltip = new Tooltip("Click to view notifications");
        notificationBtn.setTooltip(tooltip);

        // Add animation for new notifications
        notificationBtn.setOnMouseEntered(e -> {
            if (notificationCount > 0) {
                RotateTransition rt = new RotateTransition(Duration.millis(200), notificationBtn);
                rt.setByAngle(15);
                rt.setCycleCount(2);
                rt.setAutoReverse(true);
                rt.play();
            }
        });

        notificationBtn.setOnAction(event -> showNotifications());

        // Add pulse animation when there are unread notifications
        if (notificationCount > 0) {
            ScaleTransition st = new ScaleTransition(Duration.millis(1000), notificationBtn);
            st.setByX(0.1);
            st.setByY(0.1);
            st.setCycleCount(ScaleTransition.INDEFINITE);
            st.setAutoReverse(true);
            st.play();
        }
    }

    private void showNotifications() {
        try {
            List<Notification> notifications = notificationService.getUnreadNotificationsForAdmin();

            // Create a custom dialog with animations
            Dialog<Void> dialog = new Dialog<>();
            dialog.initStyle(StageStyle.TRANSPARENT);
            dialog.getDialogPane().getStylesheets().add(getClass().getResource("/styles/sidebar.css").toExternalForm());
            dialog.getDialogPane().getStyleClass().add("notification-dialog");

            // Custom header with animation
            HBox header = new HBox();
            header.getStyleClass().add("header");
            header.setAlignment(Pos.CENTER_LEFT);
            header.setPadding(new Insets(10));

            ImageView bellIcon = new ImageView(new Image(getClass().getResourceAsStream("/Images/icons/bell.png")));
            bellIcon.setFitHeight(24);
            bellIcon.setFitWidth(24);

            // Add bounce animation to icon
            TranslateTransition bounce = new TranslateTransition(Duration.millis(1000), bellIcon);
            bounce.setByY(-5);
            bounce.setCycleCount(TranslateTransition.INDEFINITE);
            bounce.setAutoReverse(true);
            bounce.play();

            Label headerLabel = new Label("You have " + notifications.size() + " notifications");
            headerLabel.setTextFill(Color.WHITE);
            headerLabel.setStyle("-fx-font-weight: bold;");

            header.getChildren().addAll(bellIcon, headerLabel);
            dialog.getDialogPane().setHeader(header);

            // Create ListView for notifications with custom cell factory
            ListView<Notification> listView = new ListView<>();
            listView.getStyleClass().add("notification-list");
            listView.setCellFactory(param -> new ListCell<>() {
                private final VBox container = new VBox(5);
                private final Label messageLabel = new Label();
                private final Label detailsLabel = new Label();
                private final Label timeLabel = new Label();
                private final HBox contentBox = new HBox(10);
                private final ImageView iconView = new ImageView();

                {
                    container.setPadding(new Insets(10));
                    messageLabel.getStyleClass().add("notification-message");
                    detailsLabel.getStyleClass().add("notification-details");
                    timeLabel.getStyleClass().add("notification-time");

                    iconView.setFitHeight(24);
                    iconView.setFitWidth(24);
                    iconView.setImage(new Image(getClass().getResourceAsStream("/Images/icons/notification.gif")));

                    VBox textBox = new VBox(3, messageLabel, detailsLabel, timeLabel);
                    contentBox.getChildren().addAll(iconView, textBox);
                    container.getChildren().add(contentBox);

                    // Add hover effect
                    this.hoverProperty().addListener((obs, oldVal, isHovering) -> {
                        if (isHovering) {
                            container.setStyle("-fx-background-color: #f5f5f5; -fx-background-radius: 5px;");
                        } else {
                            container.setStyle("");
                        }
                    });
                }
                @Override
                protected void updateItem(Notification notification, boolean empty) {
                    super.updateItem(notification, empty);
                    if (empty || notification == null) {
                        setText(null);
                        setGraphic(null);
                    } else {
                        VBox vbox = new VBox(5);
                        vbox.setPadding(new Insets(5));

                        Label messageLabel = new Label(notification.getMessage());
                        messageLabel.setStyle("-fx-font-weight: bold;");

                        Label detailsLabel = new Label(
                                "From: " + notification.getUser().getName() +
                                        " | Post: " + notification.getPost().getTitre() +
                                        " | " + notification.getCreatedAt().toString()
                        );
                        detailsLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #666;");

                        vbox.getChildren().addAll(messageLabel, detailsLabel);
                        setGraphic(vbox);
                    }
                }
            });
            listView.getItems().setAll(notifications);

            // Set dialog content
            dialog.getDialogPane().setContent(listView);
            dialog.getDialogPane().setPrefSize(400, 300);

            // Add close button
            dialog.getDialogPane().getButtonTypes().add(ButtonType.CLOSE);

            // Show dialog
            dialog.showAndWait();

            // Update badge after viewing
            updateNotificationBadge();
        } catch (SQLException e) {
            e.printStackTrace();
            showAlert("Error", "Failed to load notifications: " + e.getMessage());
        }
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
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

    @FXML private void goToBlogs() {
        setNavTitle("📋 Blog");
        loadPage("/views/Blog/ShowPosts.fxml");
        setActive(btnBlog);
    }
    @FXML private void goToCandidature() {
        setNavTitle("📋 Candidature");
        loadPage("/fxml/candidature.fxml");
        setActive(btnCandidature);
    }

    @FXML private void goToCommande() {
        setNavTitle("📋 Commande");
        loadPage("/views/Commande/AfficherCommandes.fxml");
        setActive(btnCommande);
    }

    @FXML private void goToUsers() {
        setNavTitle("📋 Commande");
        loadPage("/views/User/Admin/Dashboard.fxml");
        setActive(btnUsers);
    }

    @FXML
    private void goToUsesrs() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/User/Admin/Dashboard.fxml"));
            Parent view = loader.load();
            mainContent.getChildren().setAll(view);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void toggleProduitsSubmenu() {
        boolean currentlyVisible = produitsSubmenu.isVisible();
        produitsSubmenu.setVisible(!currentlyVisible);
        produitsSubmenu.setManaged(!currentlyVisible);
    }

    @FXML
    private void goToCategorie() {
        setNavTitle("📁 Catégories");
        loadPage("/views/produitdash/CategorieView.fxml");
        setActive(btnCategorie);
    }

    @FXML
    private void goToSousCategorie() {
        setNavTitle("📂 Sous-catégories");
        loadPage("/views/produitdash/SousCategorieView.fxml");
        setActive(btnSousCategorie);
    }

    @FXML
    private void goToProduit() {
        setNavTitle("🛒 Produits");
        loadPage("/views/produitdash/ProduitView.fxml");
        setActive(btnProduit);
    }


    public AnchorPane getMainContent() {
        return mainContent;
    }

    @FXML
    private void goToProfile() {
        setNavTitle(" Profile");
        loadPage("/views/User/Profile/Profile.fxml");
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


}
