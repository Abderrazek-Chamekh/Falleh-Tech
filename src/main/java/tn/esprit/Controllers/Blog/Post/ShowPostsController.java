package tn.esprit.Controllers.Blog.Post;

import javafx.beans.binding.Bindings;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;
import tn.esprit.entities.Post;
import tn.esprit.entities.User;
import tn.esprit.services.ServicePost;
import tn.esprit.tools.SessionManager;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class ShowPostsController {

    @FXML private HBox adminControls;
    @FXML private BorderPane borderPane;
    @FXML
    private TextField searchField; // Search Field for searching publications
    @FXML
    private ListView<Post> All; // ListView to display publications
    @FXML
    private Button mod, supp, ajouter, statsBtn; // Action Buttons
    @FXML
    private ComboBox<String> categoryCombo;
    @FXML private Label userRoleLabel;
    @FXML private Button dashboardBtn;
    private final Map<String, String> categoryMap = Map.of(
            "All", "All",
            "Agriculture", "agriculture_news",
            "Technology", "technology",
            "Recipes", "recipes",
            "Urban Farming", "urban_farming"
    );
    private final ServicePost pubService = new ServicePost();
    private ObservableList<Post> publicationList; // Holds publication items

    // 📚 Initialize Method (Runs when the controller is loaded)
    public void initialize() {

        User currentUser = SessionManager.getInstance().getCurrentUser();
        userRoleLabel.setText(currentUser.getRole());

        // Show/hide admin-only elements
        boolean isAdmin = "ROLE_ADMIN".equalsIgnoreCase(currentUser.getRole());

        configureDashboardButton(isAdmin);

        ajouter.setVisible(isAdmin);
        mod.setVisible(isAdmin);
        supp.setVisible(isAdmin);
        statsBtn.setVisible(isAdmin);

        adminControls.visibleProperty().bind(
                Bindings.createBooleanBinding(
                        () -> "ROLE_ADMIN".equalsIgnoreCase(currentUser.getRole()),
                        userRoleLabel.textProperty()
                )
        );
        adminControls.managedProperty().bind(adminControls.visibleProperty());
        loadPublications();
        setupCategoryCombo();

        loadPublications();
        setupCategoryCombo();
        loadPublications();

        // Enable/Disable buttons based on selection
        All.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            boolean isDisabled = (newValue == null);
            mod.setDisable(isDisabled);
            supp.setDisable(isDisabled);
        });
    }

    private void configureDashboardButton(boolean isAdmin) {
        if (isAdmin) {
            dashboardBtn.setText("Dashboard");
            dashboardBtn.setOnAction(e -> loadScene("/views/User/Admin/Dashboard.fxml"));
        } else {
            dashboardBtn.setText("Home");
            dashboardBtn.setOnAction(e -> loadScene("/views/Home.fxml")); // Change to your home page path
        }
        dashboardBtn.setVisible(true); // Always visible, but functionality changes
    }
    private void setupCategoryCombo() {
        categoryCombo.setItems(FXCollections.observableArrayList(
                "All", "Agriculture", "Technology", "Recipes", "Urban Farming"
        ));
        categoryCombo.getSelectionModel().select(0);

        categoryCombo.valueProperty().addListener((obs, oldVal, newVal) -> {
            try {
                filterPosts();
            } catch (SQLException e) {
                showError("Filter Error", "Failed to filter by category: " + e.getMessage());
            }
        });
    }

    private void filterPosts() throws SQLException {
        String searchText = searchField.getText().trim().toLowerCase();
        String selectedCategory = categoryCombo.getValue();
        String categoryValue = categoryMap.get(selectedCategory);

        List<Post> filteredList = pubService.getPostsByCategory(categoryValue);

        // Apply search filter if there's search text
        if (!searchText.isEmpty()) {
            filteredList = filteredList.stream()
                    .filter(post ->
                            post.getTitre().toLowerCase().contains(searchText) ||
                                    post.getContenu().toLowerCase().contains(searchText))
                    .collect(Collectors.toList());
        }

        publicationList.setAll(filteredList);
        //applySort();
    }

    @FXML
    private void handleProfile() { loadScene("/views/User/Profile/Profile.fxml"); }

    @FXML
    private void handleLogout() {
        SessionManager.getInstance().invalidateCurrentSession();
        loadScene("/views/User/Authentication/Login.fxml");
    }


    private void loadScene(String fxmlPath) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource(fxmlPath));
            Scene scene = new Scene(root,950,800);
            Stage stage = (Stage) borderPane.getScene().getWindow();
            stage.setScene(scene);
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // 🔄 Load and Display Publications
    private void loadPublications() {
        try {
            publicationList = FXCollections.observableArrayList(pubService.getAll());
            All.setItems(publicationList);
            All.setCellFactory(param -> {
                try {
                    return new PostCell();
                } catch (SQLException e) {
                    throw new RuntimeException(e);
                }
            });
            //applySort();
        } catch (Exception e) {
            showError("Error", "Failed to load publications: " + e.getMessage());
        }

    }
    @FXML
    private void handleRefresh(ActionEvent event) {
        try {
            loadPublications();
            showInfo("Refreshed", "Posts list has been refreshed");
        } catch (Exception e) {
            showError("Refresh Error", "Failed to refresh posts: " + e.getMessage());
        }
    }
    // 🔍 Search Publications by Content
    @FXML
    void handleRecherche(ActionEvent event) throws Exception {
        String keyword = searchField.getText().trim();

        if (!keyword.isEmpty()) {
            List<Post> filteredList = pubService.searchPosts(keyword);
            publicationList.setAll(filteredList);
        } else {
            loadPublications(); // Reload original list if search is empty
        }
    }

    // ✏️ Edit Selected Publication
    @FXML
    private void handleModifier(ActionEvent event) {
        Post selectedPub = All.getSelectionModel().getSelectedItem();
        if (selectedPub == null) {
            showError("Error", "Please select a publication to edit.");
            return;
        }

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/Blog/AddPost.fxml"));
            BorderPane root = loader.load();

            AddPostController controller = loader.getController();
            controller.setPublication(selectedPub);

            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            showError("Error", "Failed to open the edit page: " + e.getMessage());
        }
    }

    // 🗑️ Delete Selected Publication
    @FXML
    private void handleSupprimer(ActionEvent event) {
        Post selectedPub = All.getSelectionModel().getSelectedItem();
        if (selectedPub == null) {
            showError("Error", "Please select a publication to delete.");
            return;
        }

        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Delete Confirmation");
        alert.setHeaderText("Are you sure you want to delete this publication?");
        alert.setContentText("This action cannot be undone.");

        alert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                try {
                    pubService.supprimer(selectedPub);
                    publicationList.remove(selectedPub);
                    showInfo("Success", "Publication deleted successfully.");
                } catch (Exception e) {
                    showError("Error", "Failed to delete the publication: " + e.getMessage());
                }
            }
        });
    }

    @FXML
    private void handleShowStats(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/Blog/Statistiques.fxml"));
            Parent root = loader.load();

            Stage statsStage = new Stage();
            statsStage.setTitle("Post Statistics");
            statsStage.setScene(new Scene(root));
            statsStage.initModality(javafx.stage.Modality.WINDOW_MODAL);
            statsStage.initOwner(borderPane.getScene().getWindow()); // Set owner to block main window
            statsStage.show();

        } catch (IOException e) {
            showError("Error", "Could not load the statistics window: " + e.getMessage());
        }
    }


    // ➕ Navigate to Add Publication Page
    @FXML
    private void handleAjouter(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/Blog/AddPost.fxml"));
            Parent root = loader.load();

            Stage stage = (Stage) ajouter.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("Add Publication");
            stage.show();
        } catch (IOException e) {
            showError("Error", "Failed to open the add publication page.");
        }
    }

    // ❌ Helper Method: Display Error Alert
    private void showError(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    // ✅ Helper Method: Display Success Alert
    private void showInfo(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
