package tn.esprit.fx;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Circle;
import javafx.stage.Stage;
import tn.esprit.controllers.farmer.MesOffresController;
import tn.esprit.entities.User;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

public class FrontViewController implements Initializable {

    @FXML private ImageView logoImage;
    @FXML private ImageView menuAvatar;
    @FXML private Label profileNameLabel;

    @FXML private StackPane contentPane;
    @FXML private Label activePageLabel;

    @FXML private Button accueilButton;
    @FXML private Button produitsButton;
    @FXML private Button panierButton;
    @FXML private Button commandesButton;
    @FXML private Button offresButton;
    @FXML private Button blogButton;
    @FXML private Button logoutButton;

    @FXML private VBox categorieTreeContainer;
    @FXML private TreeView<String> categorieTree;

    private Button currentActiveButton;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        logoImage.setImage(loadImage("photos/logo.png"));
        menuAvatar.setImage(loadImage("photos/avatar.jpg"));
        setupClip(menuAvatar);
        profileNameLabel.setText("Sarah"); // TODO: Replace with dynamic username
        hideCategorieTree();
        goToAccueil(); // Load home on startup
    }

    private Image loadImage(String path) {
        File file = new File(path);
        return file.exists() ? new Image(file.toURI().toString()) : null;
    }

    private void setupClip(ImageView imageView) {
        Circle clip = new Circle();
        clip.radiusProperty().bind(imageView.fitWidthProperty().divide(2));
        clip.centerXProperty().bind(imageView.fitWidthProperty().divide(2));
        clip.centerYProperty().bind(imageView.fitHeightProperty().divide(2));
        imageView.setClip(clip);
    }

    private void loadView(String fxmlPath) {
        try {
            // Auto-prefix front/ unless absolute
            String fullPath = fxmlPath.startsWith("/") ? fxmlPath : "/front/" + fxmlPath;
            URL resource = getClass().getResource(fullPath);
            if (resource == null) {
                System.err.println("❌ FXML not found: " + fullPath);
                return;
            }
            Parent view = FXMLLoader.load(resource);
            contentPane.getChildren().setAll(view);
        } catch (IOException e) {
            System.err.println("❌ Failed to load: " + fxmlPath);
            e.printStackTrace();
        }
    }

    private void setActiveButton(Button newActiveButton) {
        if (newActiveButton == null) return;
        if (currentActiveButton != null) {
            currentActiveButton.getStyleClass().remove("active-button");
        }
        if (!newActiveButton.getStyleClass().contains("active-button")) {
            newActiveButton.getStyleClass().add("active-button");
        }
        currentActiveButton = newActiveButton;
    }

    private void hideCategorieTree() {
        if (categorieTreeContainer != null) {
            categorieTreeContainer.setVisible(false);
            categorieTreeContainer.setManaged(false);
        }
    }

    // =====================
    // Navigation methods
    // =====================

    @FXML
    private void goToAccueil() {
        setActiveButton(accueilButton);
        activePageLabel.setText("Accueil");
        hideCategorieTree();
        loadView("AccueilView.fxml");
    }

    @FXML
    private void goToProduits() {
        setActiveButton(produitsButton);
        activePageLabel.setText("Produits");
        loadView("ProduitFrontView.fxml");
    }

    @FXML
    private void goToPanier() {
        setActiveButton(panierButton);
        activePageLabel.setText("Panier");
        hideCategorieTree();
        loadView("PanierView.fxml");
    }

    @FXML
    private void goToCommandes() {
        setActiveButton(commandesButton);
        activePageLabel.setText("Commandes");
        hideCategorieTree();
        loadView("CommandesView.fxml");
    }

    @FXML
    private void goToOffres() {
        setActiveButton(offresButton);
        activePageLabel.setText("Offres de travail");
        hideCategorieTree();
        loadView("OffresView.fxml");
    }

    @FXML
    private void goToMesOffres() {
        setActiveButton(offresButton);
        activePageLabel.setText("Mes Offres");
        hideCategorieTree();

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/front/farmer_front/MesOffresView.fxml"));
            Parent mesOffresView = loader.load();

            // 💡 Set the contentPaneRef so MesOffresController can swap views
            MesOffresController.setContentPaneRef(contentPane);

            contentPane.getChildren().setAll(mesOffresView);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void goToBlog() {
        setActiveButton(blogButton);
        activePageLabel.setText("Blog");
        hideCategorieTree();
        loadView("BlogView.fxml");
    }

    @FXML
    private void openProfile() {
        System.out.println("Redirection vers le profil...");
        loadView("ProfileView.fxml");
    }

    @FXML
    private void logout() {
        try {
            Parent landingRoot = FXMLLoader.load(getClass().getResource("/fxml/landing.fxml"));
            Scene landingScene = new Scene(landingRoot);
            landingScene.getStylesheets().add(getClass().getResource("/styles/main.css").toExternalForm());
            landingScene.getStylesheets().add(getClass().getResource("/styles/sidebar_front.css").toExternalForm());
            Stage stage = (Stage) contentPane.getScene().getWindow();
            stage.setScene(landingScene);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private User currentUser;


    public void setCurrentUser(User user) {
        this.currentUser = user;
        System.out.println("🎯 setCurrentUser() called with role: " + user.getRole());
        initializeViewForRole(user.getRole());
    }

    @FXML
    private Label debugRoleLabel;

    @FXML
    private VBox offresBox; // The section showing job offers

    @FXML
    private VBox statistiquesBox; // Stock stats

    @FXML
    private VBox blogBox; // Blog posts

    @FXML
    private VBox produitsBox; // Featured products
    @FXML
    private Label roleLabel;

    public void initializeViewForRole(String role) {
        System.out.println("🔍 Initializing UI for role: " + role);

        if (roleLabel != null) {
            roleLabel.setText("Connecté en tant que: " + role);
        } else {
            System.err.println("❌ roleLabel is null!");
        }

        switch (role) {
            case "client" -> {
                panierButton.setVisible(true);
                panierButton.setManaged(true);
            }
            case "agriculteur" -> {
                panierButton.setVisible(true);
                panierButton.setManaged(true);
            }
            case "ouvrier" -> {
                // ❌ Hide the "Panier" section
                panierButton.setVisible(false);
                panierButton.setManaged(false);
            }
        }
    }



}



