package tn.esprit.Controllers.front;

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
import tn.esprit.Controllers.front.farmer.MesOffresController;
import tn.esprit.Controllers.ouvrier.CandidatureHistoryController;
import tn.esprit.Controllers.ouvrier.OffresOuvrierController;
import tn.esprit.entities.User;
import tn.esprit.services.UserService;

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
    @FXML private Button candidatures;
    @FXML private Button blogButton;

    @FXML private VBox categorieTreeContainer;
    @FXML private TreeView<String> categorieTree;

    @FXML private MenuButton profileMenu;
    @FXML private Label roleLabel;
    @FXML private VBox offresBox;
    @FXML private VBox statistiquesBox;
    @FXML private VBox blogBox;
    @FXML private VBox produitsBox;

    private Button currentActiveButton;
    private final UserService serviceUser = new UserService();
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

        profileNameLabel.setText(currentUser.getName() + " " + currentUser.getLastName());
        roleLabel.setText("Connecté en tant que: " + currentUser.getRole());
        initializeViewForRole(currentUser.getRole());

        switch (currentUser.getRole()) {
            case "Ouvrier" -> goToOffres();
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

                candidatures.setVisible(false);  // ❌ hide
                candidatures.setManaged(false);  // ❌ hide from layout
            }
            case "Ouvrier" -> {
                panierButton.setVisible(false);
                panierButton.setManaged(false);

                candidatures.setVisible(true);   // ✅ show
                candidatures.setManaged(true);   // ✅ show in layout
            }
            default -> {
                candidatures.setVisible(false);
                candidatures.setManaged(false);
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
        if (currentUser == null) {
            System.err.println("❌ currentUser is null!");
            return;
        }

        setActiveButton(offresButton);
        activePageLabel.setText("Offres de travail");
        hideCategorieTree();

        try {
            FXMLLoader loader;
            Parent view;

            switch (currentUser.getRole()) {
                case "Ouvrier" -> {
                    loader = new FXMLLoader(getClass().getResource("/front/ouvrier_front/OffresOuvrierView.fxml"));
                    view = loader.load();

                    OffresOuvrierController controller = loader.getController();
                    controller.setCurrentUser(currentUser);
                    controller.setContentPaneRef(contentPane);

                    contentPane.getChildren().setAll(view);
                }
                case "agriculteur" -> {
                    loader = new FXMLLoader(getClass().getResource("/front/farmer_front/MesOffresView.fxml"));
                    view = loader.load();

                    MesOffresController controller = loader.getController();
                    controller.setCurrentUser(currentUser);
                    controller.setContentPaneRef(contentPane);

                    contentPane.getChildren().setAll(view);
                }
                default -> {
                    showError("Rôle non pris en charge : " + currentUser.getRole());
                }
            }

        } catch (IOException e) {
            e.printStackTrace();
            showError("Erreur lors du chargement de la page Offres");
        }
    }

    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Erreur");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    @FXML
    private void goToMesOffres() {
        setActiveButton(offresButton);
        activePageLabel.setText("Mes Offres");
        hideCategorieTree();

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/front/farmer_front/MesOffresView.fxml"));
            Parent view = loader.load();

            MesOffresController controller = loader.getController();
            controller.setContentPaneRef(contentPane);
            controller.setCurrentUser(currentUser);

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
            Parent root = FXMLLoader.load(getClass().getResource("/views/User/Authentication/Login.fxml"));
            Scene scene = new Scene(root);
            scene.getStylesheets().add(getClass().getResource("/styles/main.css").toExternalForm());
            scene.getStylesheets().add(getClass().getResource("/styles/dashboard.css").toExternalForm());
            scene.getStylesheets().add(getClass().getResource("/styles/sidebar.css").toExternalForm());
            Stage stage = (Stage) contentPane.getScene().getWindow();
            stage.setScene(scene);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
