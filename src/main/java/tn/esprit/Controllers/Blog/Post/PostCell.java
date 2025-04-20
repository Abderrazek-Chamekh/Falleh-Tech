package tn.esprit.Controllers.Blog.Post;

import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.stage.Modality;
import javafx.stage.Stage;
import tn.esprit.Controllers.Blog.Comment.CommentaireController;
import tn.esprit.entities.Comment;
import tn.esprit.entities.Like;
import tn.esprit.entities.Post;
import tn.esprit.entities.User;
import tn.esprit.services.ServiceComment;
import tn.esprit.services.ServiceLike;
import tn.esprit.services.ServicePost;
import tn.esprit.services.UserService;
import tn.esprit.tools.SessionManager;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

public class PostCell extends ListCell<Post> {

    private VBox content;
    private ImageView imageView;
    private Text contenuText;
    private Text descriptionText;
    private Label dateLabel;
    private Label userNameLabel;
    private Button btnLike;
    private Label lblLikes;
    private VBox commentSection;
    private Label categoryLabel;
    private ListView<Comment> commentList;
    private TextField commentField;
    private Button addCommentButton;
    private final ServiceLike reactionService = new ServiceLike();
    private final ServiceComment commentService = new ServiceComment();
    private ServicePost po = new ServicePost();
    private User currentUser = SessionManager.getInstance().getCurrentUser();
    private static final String DEFAULT_IMAGE_PATH = System.getProperty("user.home") + "/Falleh-TechImages/default.png";

    public PostCell() throws SQLException {
        imageView = new ImageView();
        imageView.setFitWidth(300);
        imageView.setFitHeight(200);
        imageView.setPreserveRatio(true);
        imageView.setStyle("-fx-border-radius: 15px; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.15), 8, 0, 4, 4);");

        contenuText = new Text();
        contenuText.setStyle("-fx-font-weight: bold; -fx-font-size: 16px; -fx-fill: #2C3E50;");

        descriptionText = new Text();
        descriptionText.setStyle("-fx-font-size: 14px; -fx-fill: #555555;");

        categoryLabel = new Label();
        categoryLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #FFFFFF; -fx-background-color: #3498DB; " +
                "-fx-padding: 3px 8px; -fx-background-radius: 10px;");


        dateLabel = new Label();
        dateLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #7D7D7D; -fx-font-style: italic;");

        userNameLabel = new Label();
        userNameLabel.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: #3498DB;");

        btnLike = new Button("👍");
        lblLikes = new Label("0");

        btnLike.setStyle("-fx-background-color: #27AE60; -fx-text-fill: white; -fx-background-radius: 15;");
        lblLikes.setStyle("-fx-font-size: 14px; -fx-font-weight: bold;");

        HBox reactionBox = new HBox(15, btnLike, lblLikes);
        reactionBox.setPadding(new Insets(10, 0, 0, 0));
        reactionBox.setStyle("-fx-alignment: center-left;");

        commentList = new ListView<>();
        commentList.setPrefHeight(100);
        commentList.setStyle("-fx-background-radius: 10px; -fx-background-color: #F0F0F0;");

        commentField = new TextField();
        commentField.setPromptText("Add a comment...");
        commentField.setStyle("-fx-background-radius: 10px; -fx-padding: 5px;");

        addCommentButton = new Button("➕");
        addCommentButton.setStyle("-fx-background-color: #2980B9; -fx-text-fill: white; -fx-background-radius: 10px;");

        addCommentButton.setOnAction(e -> {
            try {
                handleAddComment();
            } catch (Exception ex) {
                throw new RuntimeException(ex);
            }
        });

        HBox commentInputBox = new HBox(10, commentField, addCommentButton);
        commentInputBox.setPadding(new Insets(5));
        commentInputBox.setStyle("-fx-alignment: center-left;");

        commentSection = new VBox(5, new Label("💬 Comments:"), commentList, commentInputBox);
        commentSection.setPadding(new Insets(10));
        commentSection.setStyle("-fx-background-color: #F9F9F9; -fx-background-radius: 10px;");

        content = new VBox(10, userNameLabel, imageView, contenuText, descriptionText, categoryLabel, dateLabel, reactionBox, commentSection);
        content.setPadding(new Insets(15));
        content.setStyle("-fx-background-color: #FFFFFF; -fx-background-radius: 20; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.12), 10, 0, 5, 5);");

        btnLike.setOnAction(event -> {
            try {
                handleReaction();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });
    }

    @Override
    protected void updateItem(Post pub, boolean empty) {
        super.updateItem(pub, empty);

        if (empty || pub == null) {
            setGraphic(null);
        } else {
            boolean isAdmin = currentUser.getRole().equalsIgnoreCase("ROLE_ADMIN");
            commentField.setVisible(!isAdmin);
            addCommentButton.setVisible(!isAdmin);


            String imagePath = pub.getImage();
            Image image = loadImage(imagePath);
            imageView.setImage(image);
            contenuText.setText("📝 " + pub.getTitre());
            descriptionText.setText("📜 " + pub.getContenu());
            dateLabel.setText("📅 " + pub.getDate());

            if (pub.getUser() != null) {
                userNameLabel.setText("👤 " + pub.getUser().getFullName());
            } else {
                userNameLabel.setText("👤 Utilisateur inconnu");
            }

            if (pub.getCategory() != null && !pub.getCategory().isEmpty()) {
                categoryLabel.setText("🏷️ " + pub.getCategory());
            } else {
                categoryLabel.setText("🏷️ Uncategorized");
            }


            try {
                updateComments(pub.getId());
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
            updateReactions(pub.getId());
            setGraphic(content);
        }
    }

    private void handleReaction() throws Exception {
        if (getItem() != null) {
            // Check if user already liked this post
            if (reactionService.hasUserLikedPost(currentUser.getId(), getItem().getId())) {
                showInfo("Already Liked", "You've already liked this post");
                return;
            }

            Like l = new Like();
            l.setPost(po.getById(getItem().getId()));
            l.setUser(currentUser);
            reactionService.ajouter(l);
            updateReactions(getItem().getId());
        }
    }

    private void updateReactions(int publicationId) {
        int likes = reactionService.countReactions(publicationId);
        lblLikes.setText(String.valueOf(likes));
    }

    private void updateComments(int publicationId) throws Exception {
        List<Comment> comments = commentService.getCommentsByPost(publicationId);

        commentList.setCellFactory(param -> new ListCell<>() {
            @Override
            protected void updateItem(Comment comment, boolean empty) {
                super.updateItem(comment, empty);

                if (empty || comment == null) {
                    setText(null);
                    setContextMenu(null);
                } else {
                    // Display comment with user's name
                    String userName = (comment.getUser() != null)
                            ? comment.getUser().getFullName()
                            : "Utilisateur inconnu";
                    setText("👤 " + userName + ": " + comment.getContenu());

                    // Create the context menu
                    ContextMenu contextMenu = new ContextMenu();

                    // ✏️ Update Option
                    MenuItem editItem = new MenuItem("✏️ Update");
                    editItem.setOnAction(e -> openCommentEditPage(comment));

                    // 🗑️ Delete Option
                    MenuItem deleteItem = new MenuItem("🗑️ Delete");
                    deleteItem.setOnAction(e -> {handleDeleteComment(comment);
                    });

                    // Add options to context menu
                    contextMenu.getItems().addAll(editItem, deleteItem);
                    setContextMenu(contextMenu);
                }
            }
        });

        commentList.getItems().setAll(comments); // Populate the comment list
    }

    private void handleAddComment() throws Exception {
        if (currentUser.getRole().equalsIgnoreCase("ROLE_ADMIN")) {
            showError("Permission Denied", "Admins cannot add comments");
            return;
        }

        if (!commentField.getText().trim().isEmpty() && getItem() != null) {
            Comment c = new Comment();
            c.setPost(po.getById(getItem().getId()));
            c.setContenu(commentField.getText().trim());
            c.setUser(currentUser);
            c.setDate(LocalDateTime.now().toLocalDate());
            commentService.ajouter(c);
            commentField.clear();
            updateComments(getItem().getId());
        }
    }



    private void handleDeleteComment(Comment comment) {
        User currentUser = SessionManager.getInstance().getCurrentUser();
        System.out.println(currentUser.getFullName());
        // Check permissions
        if (!currentUser.getRole().equalsIgnoreCase("ROLE_ADMIN") &&
                (comment.getUser() == null || !Objects.equals(comment.getUser().getId(), currentUser.getId()))) {
            showError("Permission Denied", "You can only delete your own comments");
            return;
        }
        System.out.println(comment.getUser().getFullName());
        System.out.println("dddddd");
        Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION);
        confirmation.setTitle("Confirm Deletion");
        confirmation.setHeaderText("Are you sure you want to delete this comment?");
        confirmation.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                try {
                    commentService.supprimer(comment);
                    updateComments(getItem().getId());
                    showInfo("Deleted", "Comment deleted successfully.");
                } catch (Exception ex) {
                    showError("Error", "Failed to delete the comment: " + ex.getMessage());
                }
            }
        });
    }

    private void openCommentEditPage(Comment comment) {
        User currentUser = SessionManager.getInstance().getCurrentUser();

        // Check if current user is not admin and not the comment owner
        if (!currentUser.getRole().equalsIgnoreCase("ROLE_ADMIN") &&
                (comment.getUser() == null || comment.getUser().getId() != currentUser.getId())) {
            showError("Permission Denied", "You can only edit your own comments");
            return;
        }
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/Blog/commentaire.fxml"));
            Parent root = loader.load();

            CommentaireController controller = loader.getController();
            controller.setCommentAndPublication(comment);

            Stage stage = new Stage();
            stage.setScene(new Scene(root));
            stage.setTitle("Manage Comment");

            // Modal setup
            stage.initModality(Modality.APPLICATION_MODAL);
            Stage parentStage = (Stage) this.getScene().getWindow();
            stage.initOwner(parentStage);

            // Size constraints
            stage.setMinWidth(400);
            stage.setMinHeight(300);

            // Center on parent
            stage.setOnShown(event -> {
                stage.setX(parentStage.getX() + parentStage.getWidth() / 2 - stage.getWidth() / 2);
                stage.setY(parentStage.getY() + parentStage.getHeight() / 2 - stage.getHeight() / 2);
            });

            stage.showAndWait();
        } catch (IOException e) {
            showError("Error", "Failed to open comment editor: " + e.getMessage());
        }
    }

    private Image loadImage(String imagePath) {
        File file = new File(imagePath);
        return file.exists() ? new Image(file.toURI().toString()) : new Image(new File(DEFAULT_IMAGE_PATH).toURI().toString());
    }


    private void showError(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
    private void showInfo(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

}
