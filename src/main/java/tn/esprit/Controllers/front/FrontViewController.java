package tn.esprit.Controllers.front;
import tn.esprit.utils.LocalFlammeServer;

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
import javafx.event.ActionEvent;
import tn.esprit.entities.Flamme;
import tn.esprit.services.FlammeService;
import java.util.List;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

public class FrontViewController implements Initializable {

    @FXML private ImageView logoImage, menuAvatar;
    @FXML private Label profileNameLabel, activePageLabel, roleLabel;
    @FXML private StackPane contentPane;
    @FXML private VBox categorieTreeContainer;
    @FXML private Button btnPanier;
    @FXML private Label flammeCountLabel;

    @FXML private Button accueilButton, produitsButton, panierButton, commandesButton,
            offresButton, candidatures, blogButton, btnFavoris, prodagriculteur;

    @FXML private TreeView<String> categorieTree;
    @FXML private MenuButton profileMenu;
    private static FrontViewController instance;
    public static FrontViewController getInstance() { return instance; }

    private Button currentActiveButton;
    private final UserService serviceUser = new UserService();
    private User currentUser;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        logoImage.setImage(loadImage("photos/logo.png"));
        menuAvatar.setImage(loadImage("photos/avatar.jpg"));
        setupClip(menuAvatar);
        hideCategorieTree();
        instance = this;
        // 🔽 Appelle ici la méthode avec conversion :
        if (currentUser != null) {
            updateFlammeCount(Long.valueOf(currentUser.getId()));
        }
    }
    public void updateFlammeCount(Long userId) {
        List<Flamme> flammes = FlammeService.getInstance().getFlammesByUser(userId);
        int total = flammes.stream().mapToInt(Flamme::getCount).sum();
        flammeCountLabel.setText(String.valueOf(total));
    }




    public void setCurrentUser(User user) {
        this.currentUser = serviceUser.findById(user.getId());
        if (this.currentUser == null) this.currentUser = user;
        if (this.currentUser.getRole() == null) this.currentUser.setRole(user.getRole());

        profileNameLabel.setText(currentUser.getName() + " " + currentUser.getLastName());
        roleLabel.setText("Connecté en tant que: " + currentUser.getRole());
        initializeViewForRole(currentUser.getRole());
        LocalFlammeServer.startServer(Long.valueOf(currentUser.getId()));

        // ✅ Ajoute ceci pour mettre à jour les flammes
        updateFlammeCount(Long.valueOf(currentUser.getId()));

        switch (currentUser.getRole()) {
            case "Ouvrier" -> goToOffres();
            case "agriculteur" -> goToMesOffres();
            case "client" -> goToAccueil();
            default -> goToAccueil();
        }
    }



    private void loadView(String fxmlPath) {
        try {
            String fullPath = fxmlPath.startsWith("/") ? fxmlPath : "/front/" + fxmlPath;
            URL resource = getClass().getResource(fullPath);
            if (resource == null) {
                System.err.println("❌ Fichier FXML non trouvé : " + fullPath);
                return;
            }
            Parent view = FXMLLoader.load(resource);
            contentPane.getChildren().setAll(view);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void setActiveButton(Button newActiveButton) {
        if (newActiveButton != null) {
            if (currentActiveButton != null)
                currentActiveButton.getStyleClass().remove("active-button");
            if (!newActiveButton.getStyleClass().contains("active-button"))
                newActiveButton.getStyleClass().add("active-button");
            currentActiveButton = newActiveButton;
        }
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
        role = role.toLowerCase(); // pour éviter les problèmes de casse

        switch (role) {
            case "client" -> {
                panierButton.setVisible(true);
                panierButton.setManaged(true);
                btnFavoris.setVisible(true);
                btnFavoris.setManaged(true);
                btnPanier.setVisible(true);
                btnPanier.setManaged(true);
                candidatures.setVisible(false);
                candidatures.setManaged(false);
            }
            case "agriculteur" -> {
                panierButton.setVisible(false);
                panierButton.setManaged(false);
                btnFavoris.setVisible(false);
                btnFavoris.setManaged(false);
                btnPanier.setVisible(false);
                btnPanier.setManaged(false);
                candidatures.setVisible(false);
                candidatures.setManaged(false);
            }
            case "ouvrier" -> {
                panierButton.setVisible(false);
                panierButton.setManaged(false);
                btnFavoris.setVisible(false);
                btnFavoris.setManaged(false);

                candidatures.setVisible(true);
                candidatures.setManaged(true);
            }
            default -> {
                // Masquer tout par défaut si rôle inconnu
                panierButton.setVisible(false);
                panierButton.setManaged(false);
                btnFavoris.setVisible(false);
                btnFavoris.setManaged(false);
                candidatures.setVisible(false);
                candidatures.setManaged(false);
            }
        }
    }


    // ------------------- NAVIGATION -------------------

    @FXML private void goToAccueil() {
        setActiveButton(accueilButton);
        activePageLabel.setText("Accueil");
        hideCategorieTree();
        loadView("AccueilView.fxml");
    }

    @FXML public void goToFavoris() {
        setActiveButton(btnFavoris);
        activePageLabel.setText("Favoris ❤️");
        hideCategorieTree();
        loadView("/front/produit/FavorisView.fxml");
    }

    @FXML public void goToPanier() {
        setActiveButton(panierButton);
        activePageLabel.setText("🛒 Mon Panier");
        hideCategorieTree();
        loadView("/front/produit/Panier.fxml");
    }

    @FXML
    public void goToProduits() {
        if (currentUser == null) {
            System.err.println("❌ currentUser is null!");
            return;
        }

        setActiveButton(produitsButton);
        activePageLabel.setText("Produits");
        hideCategorieTree();

        String role = currentUser.getRole();

        if ("client".equalsIgnoreCase(role)) {
            loadView("/front/produit/ProduitFrontView.fxml");
        } else if ("agriculteur".equalsIgnoreCase(role)) {
            loadView("/front/produit/Agriculteur.fxml");
        } else {
            showError("Rôle non pris en charge : " + role);
        }
    }





    @FXML private void goToCommandes() {
        setActiveButton(commandesButton);
        activePageLabel.setText("Commandes");
        hideCategorieTree();
        loadView("CommandesView.fxml");
    }

    @FXML private void goToOffres() {
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
                default -> showError("Rôle non pris en charge : " + currentUser.getRole());
            }

        } catch (IOException e) {
            e.printStackTrace();
            showError("Erreur lors du chargement de la page Offres");
        }
    }

    @FXML private void goToMesOffres() {
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

    @FXML private void goToCandidatureHistory() {
        setActiveButton(candidatures);
        activePageLabel.setText("Mes Candidatures");

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/front/ouvrier_front/CandidatureHistoryView.fxml"));
            Parent view = loader.load();
            loader.<CandidatureHistoryController>getController().setCurrentUser(currentUser);
            contentPane.getChildren().setAll(view);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML private void goToBlog() {
        setActiveButton(blogButton);
        activePageLabel.setText("Blog");
        hideCategorieTree();
        loadView("BlogView.fxml");
    }

    @FXML private void openProfile() {
        loadView("ProfileView.fxml");
    }

    @FXML private void logout() {
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

    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Erreur");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
