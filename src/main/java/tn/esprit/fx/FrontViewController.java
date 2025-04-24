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
import tn.esprit.controllers.MessagingController;
import tn.esprit.controllers.farmer.MesOffresController;
import tn.esprit.controllers.ouvrier.CandidatureHistoryController;
import tn.esprit.controllers.ouvrier.OffresOuvrierController;
import tn.esprit.entities.User;
import tn.esprit.services.ServiceUser;

import java.awt.event.ActionEvent;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

public class FrontViewController implements Initializable {

    @FXML
    private ImageView logoImage;
    @FXML
    private ImageView menuAvatar;
    @FXML
    private Label profileNameLabel;
    @FXML
    private StackPane contentPane;
    @FXML
    private Label activePageLabel;

    @FXML
    private Button accueilButton;
    @FXML
    private Button produitsButton;
    @FXML
    private Button panierButton;
    @FXML
    private Button commandesButton;
    @FXML
    private Button offresButton;
    @FXML
    private Button candidatures;
    @FXML
    private Button blogButton;
    @FXML
    private Button btnCalendrier;
    @FXML
    private VBox categorieTreeContainer;
    @FXML
    private TreeView<String> categorieTree;

    @FXML
    private MenuButton profileMenu;
    @FXML
    private Label roleLabel;
    @FXML
    private VBox offresBox;
    @FXML
    private VBox statistiquesBox;
    @FXML
    private VBox blogBox;
    @FXML
    private VBox produitsBox;
    @FXML
    private Button addMapButton;

    private Button currentActiveButton;
    private final ServiceUser serviceUser = new ServiceUser();
    private User currentUser;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        logoImage.setImage(loadImage("photos/logo.png"));
        menuAvatar.setImage(loadImage("photos/avatar.jpg"));
        setupClip(menuAvatar);
        hideCategorieTree();
    }

    public void setCurrentUser(User user) {
        this.currentUser = serviceUser.findById(user.getId());
        if (this.currentUser == null) this.currentUser = user;
        if (this.currentUser.getRole() == null) this.currentUser.setRole(user.getRole());

        // 👤 UI Update
        profileNameLabel.setText(currentUser.getName() + " " + currentUser.getLastName());
        roleLabel.setText("Connecté en tant que: " + currentUser.getRole());

        // 🧭 Init common UI (if you have it)
        initializeViewForRole(currentUser.getRole());

        // ✅ Hide sidebar buttons based on role
        String role = currentUser.getRole();
        if ("agriculteur".equalsIgnoreCase(role)) {
            if (candidatures != null) {
                candidatures.setVisible(false);
                candidatures.setManaged(false);
            }
            if (btnCalendrier != null) {
                btnCalendrier.setVisible(false);
                btnCalendrier.setManaged(false);
            }
            if (addMapButton != null) {
                addMapButton.setVisible(true);
                addMapButton.setManaged(true);
            }
        }
        if ("ouvrier".equalsIgnoreCase(role)) {
            if (candidatures != null) {
                candidatures.setVisible(false);
                candidatures.setManaged(false);
            }
            if (btnCalendrier != null) {
                btnCalendrier.setVisible(true);
                btnCalendrier.setManaged(true);
            }
            if (addMapButton != null) {
                addMapButton.setVisible(false);
                addMapButton.setManaged(false);
            }
        }

        // 🚀 Auto redirect based on role
        switch (role) {
            case "ouvrier" -> goToOffres();
            case "agriculteur" -> goToMesOffres();
            case "client" -> goToAccueil();
            default -> goToAccueil();
        }
    }


    public User getCurrentUser() {
        return currentUser;
    }

    private void loadView(String fxmlPath) {
        try {
            String fullPath = fxmlPath.startsWith("/") ? fxmlPath : "/front/" + fxmlPath;
            URL resource = getClass().getResource(fullPath);
            if (resource == null) return;
            Parent view = FXMLLoader.load(resource);
            contentPane.getChildren().setAll(view);
        } catch (IOException e) {
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
        categorieTreeContainer.setVisible(false);
        categorieTreeContainer.setManaged(false);
    }

    public void initializeViewForRole(String role) {
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

        switch (currentUser.getRole()) {
            case "ouvrier" -> {
                try {
                    FXMLLoader loader = new FXMLLoader(getClass().getResource("/front/ouvrier_front/OffresOuvrierView.fxml"));
                    Parent view = loader.load();
                    loader.<OffresOuvrierController>getController().setCurrentUser(currentUser);
                    contentPane.getChildren().setAll(view);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            case "agriculteur" -> {
                try {
                    FXMLLoader loader = new FXMLLoader(getClass().getResource("/front/farmer_front/MesOffresView.fxml"));
                    Parent view = loader.load();
                    MesOffresController.setContentPaneRef(contentPane);
                    loader.<MesOffresController>getController().setCurrentUserId(currentUser.getId());
                    contentPane.getChildren().setAll(view);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            default -> loadView("OffresView.fxml");
        }
    }

    @FXML
    private void goToMesOffres() {
        setActiveButton(offresButton);
        activePageLabel.setText("Mes Offres");
        hideCategorieTree();
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/front/farmer_front/MesOffresView.fxml"));
            Parent view = loader.load();
            MesOffresController.setContentPaneRef(contentPane);

            // ✅ FIX: Inject the current user's ID so it filters only their offers
            loader.<MesOffresController>getController().setCurrentUserId(currentUser.getId());

            contentPane.getChildren().setAll(view);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void goToCandidatureHistory() {
        activePageLabel.setText("Mes Candidatures");
        setActiveButton(candidatures);
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/front/ouvrier_front/CandidatureHistoryView.fxml"));
            Parent view = loader.load();
            loader.<CandidatureHistoryController>getController().setCurrentUser(currentUser);
            contentPane.getChildren().setAll(view);
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
            Parent root = FXMLLoader.load(getClass().getResource("/fxml/landing.fxml"));
            Scene scene = new Scene(root);
            scene.getStylesheets().add(getClass().getResource("/styles/main.css").toExternalForm());
            scene.getStylesheets().add(getClass().getResource("/styles/sidebar_front.css").toExternalForm());
            Stage stage = (Stage) contentPane.getScene().getWindow();
            stage.setScene(scene);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void openCalendrierView() {
        setActiveButton(btnCalendrier);
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/front/CalendrierOuvrier.fxml"));
            Parent root = loader.load();
            contentPane.getChildren().setAll(root);  // Make sure contentPane is defined with @FXML
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private Button btnMessages;

    @FXML
    private void openMessagingPage() {
        setActiveButton(btnMessages);
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/front/MessagingView.fxml"));
            Parent messagingRoot = loader.load();

            MessagingController controller = loader.getController();
            controller.setCurrentUser(currentUser); // Pass current logged-in user

            contentPane.getChildren().setAll(messagingRoot);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    public void verifyAccount() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/verifyaccount.fxml"));
            Parent verifyView = loader.load();
            contentPane.getChildren().setAll(verifyView);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void openLandDrawPage() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/front/land/LandDrawView.fxml"));
            Parent landView = loader.load();
            contentPane.getChildren().setAll(landView);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private StackPane contentArea;


    @FXML
    private void goToMap() {


        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/front/ouvrier_front/TunisiaMapView.fxml"));
            Parent verifyView = loader.load();
            contentPane.getChildren().setAll(verifyView);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void goToHeatmap() {


        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/front/heatmap.fxml"));
            Parent verifyView = loader.load();
            contentPane.getChildren().setAll(verifyView);
        } catch (IOException e) {
            e.printStackTrace();
        }


    }

}