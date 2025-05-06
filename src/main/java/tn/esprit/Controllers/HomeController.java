package tn.esprit.Controllers;

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
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import tn.esprit.Controllers.Blog.Post.PostCell;
import tn.esprit.entities.Post;
import tn.esprit.entities.User;
import tn.esprit.services.ServicePost;
import tn.esprit.tools.SessionManager;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

public class HomeController {
    @FXML private HBox adminControls;
    @FXML private BorderPane borderPane;
    @FXML private VBox topPostsContainer;
    @FXML private Button ajouter; // Action Buttons
    @FXML private Label userRoleLabel;
    private final ServicePost pubService = new ServicePost();

    // 📚 Initialize Method (Runs when the controller is loaded)
    public void initialize() {

        User currentUser = SessionManager.getInstance().getCurrentUser();
        userRoleLabel.setText(currentUser.getRole());

        adminControls.visibleProperty().bind(
                Bindings.createBooleanBinding(
                        () -> "ROLE_ADMIN".equalsIgnoreCase(currentUser.getRole()),
                        userRoleLabel.textProperty()
                )
        );
        loadTopPosts();
    }

    private void loadTopPosts() {
        try {
            // Get top 3 most engaging posts (likes + comments combined)
            List<Post> topEngagedPosts = pubService.getTopEngagedPosts(3);

            // Display them in one section
            createTopPostsSection(" ✨ Top Engaging Posts ✨ ", topEngagedPosts);

        } catch (SQLException e) {
            showError("Error", "Failed to load top posts: " + e.getMessage());
        }
    }

    private void createTopPostsSection(String title, List<Post> posts) {
        if (posts.isEmpty()) return;

        Label sectionTitle = new Label(title);
        sectionTitle.getStyleClass().add("section-title");

        ListView<Post> postsListView = new ListView<>();
        postsListView.setItems(FXCollections.observableArrayList(posts));
        postsListView.setCellFactory(param -> {
            try {
                return new PostCell();
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        });
        postsListView.setPrefHeight(400);
        postsListView.getStyleClass().add("top-posts-list");

        VBox section = new VBox(10, sectionTitle, postsListView);
        section.getStyleClass().add("top-posts-section");

        topPostsContainer.getChildren().add(section);
    }


    @FXML
    private void handleProfile() { loadScene("/views/User/Profile/Profile.fxml"); }

    @FXML
    private void handleLogout() {
        SessionManager.getInstance().invalidateCurrentSession();
        loadScene("/views/User/Authentication/Login.fxml");
    }

    @FXML
    private void loadPosts() { loadScene("/views/Blog/ShowPosts.fxml"); }


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
