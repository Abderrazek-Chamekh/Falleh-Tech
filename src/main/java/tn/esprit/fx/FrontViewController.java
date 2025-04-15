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
import tn.esprit.controllers.ouvrier.OffresOuvrierController;
import tn.esprit.entities.User;
import tn.esprit.services.ServiceUser;

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

    @FXML private MenuButton profileMenu;
    @FXML private Label roleLabel;
    @FXML private VBox offresBox;
    @FXML private VBox statistiquesBox;
    @FXML private VBox blogBox;
    @FXML private VBox produitsBox;

    private final ServiceUser serviceUser = new ServiceUser();
    private User currentUser;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        logoImage.setImage(loadImage("photos/logo.png"));
        menuAvatar.setImage(loadImage("photos/avatar.jpg"));
        setupClip(menuAvatar);
        hideCategorieTree();
        // don't load any default view until user is assigned
    }
    public void setCurrentUser(User user) {
        this.currentUser = serviceUser.findById(user.getId());

        // ⛔ Fix in case findById returns null or has missing role
        if (this.currentUser == null) {
            this.currentUser = user;
        } else if (this.currentUser.getRole() == null) {
            this.currentUser.setRole(user.getRole());
        }

        if (currentUser != null) {
            profileNameLabel.setText(currentUser.getName() + " " + currentUser.getLastName());
            roleLabel.setText("Connecté en tant que: " + currentUser.getRole());

            initializeViewForRole(currentUser.getRole());

            switch (currentUser.getRole()) {
                case "ouvrier" -> goToOffres();
                case "agriculteur" -> goToMesOffres();
                case "client" -> goToAccueil();
                default -> goToAccueil();
            }

        } else {
            profileNameLabel.setText("Utilisateur inconnu");
            roleLabel.setText("Rôle inconnu");
        }
    }




    public User getCurrentUser() {
        return currentUser;
    }

    private void loadView(String fxmlPath) {
        try {
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

    private void setupClip(ImageView imageView) {
        Circle clip = new Circle();
        clip.radiusProperty().bind(imageView.fitWidthProperty().divide(2));
        clip.centerXProperty().bind(imageView.fitWidthProperty().divide(2));
        clip.centerYProperty().bind(imageView.fitHeightProperty().divide(2));
        imageView.setClip(clip);
    }

    private Image loadImage(String path) {
        File file = new File(path);
        return file.exists() ? new Image(file.toURI().toString()) : null;
    }

    private void hideCategorieTree() {
        if (categorieTreeContainer != null) {
            categorieTreeContainer.setVisible(false);
            categorieTreeContainer.setManaged(false);
        }
    }

    public void initializeViewForRole(String role) {
        System.out.println("🔍 Initializing UI for role: " + role);

        if (roleLabel != null) {
            roleLabel.setText("Connecté en tant que: " + role);
        }

        switch (role) {
            case "client", "agriculteur" -> {
                panierButton.setVisible(true);
                panierButton.setManaged(true);
            }
            case "ouvrier" -> {
                panierButton.setVisible(false);
                panierButton.setManaged(false);
            }
        }
    }

    // Navigation Methods

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

        if (currentUser == null) {
            System.err.println("❌ No user connected in goToOffres()");
            return;
        }

        String role = currentUser.getRole();
        int id = currentUser.getId();
        System.out.println("🔁 [goToOffres] Called. CurrentUser ID: " + id + ", Role: " + role);

        switch (role) {
            case "ouvrier" -> {
                System.out.println("📦 Loading OffresOuvrierView.fxml...");
                try {
                    FXMLLoader loader = new FXMLLoader(getClass().getResource("/front/ouvrier_front/OffresOuvrierView.fxml"));
                    Parent view = loader.load();

                    OffresOuvrierController controller = loader.getController();
                    controller.setCurrentUser(currentUser); // ✅ Ensure user is passed correctly

                    contentPane.getChildren().setAll(view);
                    System.out.println("✅ Ouvrier view loaded");
                } catch (IOException e) {
                    System.err.println("❌ Failed to load OffresOuvrierView.fxml");
                    e.printStackTrace();
                }
            }

            case "agriculteur" -> {
                System.out.println("📦 Loading MesOffresView.fxml for agriculteur...");
                try {
                    FXMLLoader loader = new FXMLLoader(getClass().getResource("/front/farmer_front/MesOffresView.fxml"));
                    Parent mesOffresView = loader.load();

                    MesOffresController.setContentPaneRef(contentPane);
                    MesOffresController mesController = loader.getController();
                    mesController.setCurrentUserId(currentUser.getId());

                    contentPane.getChildren().setAll(mesOffresView);
                    System.out.println("✅ Agricultur view loaded");
                } catch (IOException e) {
                    System.err.println("❌ Failed to load MesOffresView.fxml");
                    e.printStackTrace();
                }
            }

            default -> {
                System.out.println("⚠️ Unknown role, loading fallback OffresView.fxml...");
                loadView("OffresView.fxml");
            }
        }
    }




    @FXML
    private void goToMesOffres() {
        setActiveButton(offresButton);
        activePageLabel.setText("Mes Offres");
        hideCategorieTree();

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/front/farmer_front/MesOffresView.fxml"));
            Parent mesOffresView = loader.load();
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
}