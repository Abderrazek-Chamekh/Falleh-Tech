package tn.esprit.Controllers.Blog.Post;

import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.stage.Modality;
import javafx.stage.Stage;
import tn.esprit.Controllers.Blog.Comment.BadWordFilter;
import tn.esprit.Controllers.Blog.Comment.CommentaireController;
import tn.esprit.Controllers.Blog.Comment.SpeechRecognitionUtils;
import tn.esprit.Controllers.Blog.Comment.TextToSpeechUtils;
import tn.esprit.entities.Comment;
import tn.esprit.entities.Like;
import tn.esprit.entities.Post;
import tn.esprit.entities.User;
import tn.esprit.services.*;
import tn.esprit.tools.SessionManager;
import tn.esprit.utils.ImageUtils;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

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
    private TextArea commentField;
    private Button addCommentButton;
    private final ServiceLike reactionService = new ServiceLike();
    private final ServiceComment commentService = new ServiceComment();
    private ServicePost po = new ServicePost();
    private User currentUser = SessionManager.getInstance().getCurrentUser();
    private boolean alertShownForBadWords = false;

    private final SpeechRecognitionUtils speechRecognition = new SpeechRecognitionUtils();
    private final TextToSpeechUtils textToSpeech = new TextToSpeechUtils();
    private final ServiceNotification notificationService = new ServiceNotification();

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

        Button btnReadPost = new Button("🔊 Read Post");
        btnReadPost.setStyle("-fx-background-color: #8E44AD; -fx-text-fill: white; -fx-background-radius: 10px;");
        btnReadPost.setOnAction(e -> {
            String fullText = contenuText.getText() + ". " + descriptionText.getText();
            textToSpeech.speak(fullText);
        });

        HBox assistiveTools = new HBox(10, btnReadPost);
        assistiveTools.setPadding(new Insets(5));
        assistiveTools.setStyle("-fx-alignment: center-left;");

        HBox reactionBox = new HBox(15, btnLike, lblLikes);
        reactionBox.setPadding(new Insets(10, 0, 0, 0));
        reactionBox.setStyle("-fx-alignment: center-left;");

        commentList = new ListView<>();
        commentList.setPrefHeight(100);
        commentList.setStyle("-fx-background-radius: 10px; -fx-background-color: #F0F0F0;");

        commentField = new TextArea();
        commentField.setPromptText("Add a comment...");
        commentField.setWrapText(true); // allows multi-line text wrapping
        commentField.setPrefRowCount(3); // makes it taller
        commentField.setStyle("-fx-background-radius: 10px; -fx-padding: 8px; -fx-font-size: 13px;");

        commentField.setMaxWidth(Double.MAX_VALUE);
        HBox.setHgrow(commentField, Priority.ALWAYS);

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


        content = new VBox(10, userNameLabel, imageView, contenuText, descriptionText, categoryLabel, dateLabel, reactionBox, assistiveTools, commentSection);
        content.setPadding(new Insets(15));
        content.setStyle("-fx-background-color: #FFFFFF; -fx-background-radius: 20; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.12), 10, 0, 5, 5);");

        btnLike.setOnAction(event -> {
            try {
                handleReaction();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });

        commentField.textProperty().addListener((obs, oldVal, newVal) -> {
            List<String> badWords = BadWordFilter.detectBadWords(newVal);

            if (!badWords.isEmpty()) {
                commentField.setStyle("-fx-border-color: #FF4444; -fx-border-width: 2px;");
                addCommentButton.setDisable(true);

                Tooltip tooltip = new Tooltip("Inappropriate words detected: " +
                        String.join(", ", badWords));
                tooltip.setStyle("-fx-font-size: 12px; -fx-text-fill: #FF4444;");
                commentField.setTooltip(tooltip);

                if (!alertShownForBadWords) {
                    alertShownForBadWords = true;
                    Alert alert = new Alert(Alert.AlertType.WARNING);
                    alert.setTitle("Bad Word Detected");
                    alert.setHeaderText("Please remove inappropriate language");
                    alert.setContentText("Your comment contains the following inappropriate words:\n" +
                            String.join(", ", badWords));
                    alert.showAndWait();
                }
            } else {
                commentField.setStyle("");
                commentField.setTooltip(null);
                addCommentButton.setDisable(false);
                alertShownForBadWords = false; // Reset when clean
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

            imageView.setImage(ImageUtils.chargerDepuisNom(pub.getImage()));
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

            // Create notification
            String message = currentUser.getFullName() + " liked your post: " + getItem().getTitre();
            notificationService.addNotification(message, getItem().getUser(), getItem());
        }
    }

    // Update handleAddComment method
    private void handleAddComment() throws Exception {
        if (currentUser.getRole().equalsIgnoreCase("ROLE_ADMIN")) {
            showError("Permission Denied", "Admins cannot add comments");
            return;
        }

        String commentText = commentField.getText().trim();

        if (!commentText.isEmpty() && getItem() != null) {
            // Check for bad words
            List<String> badWords = BadWordFilter.detectBadWords(commentText);

            if (!badWords.isEmpty()) {
                String message = String.format(
                        "Your comment contains inappropriate language (%s). " +
                                "Please revise your comment.",
                        String.join(", ", badWords)
                );

                Alert alert = new Alert(Alert.AlertType.WARNING);
                alert.setTitle("Inappropriate Content");
                alert.setHeaderText("Comment contains bad words");
                alert.setContentText(message);

                // Add custom buttons
                ButtonType replaceButton = new ButtonType("Replace and Post");
                ButtonType cancelButton = new ButtonType("Cancel", ButtonBar.ButtonData.CANCEL_CLOSE);
                alert.getButtonTypes().setAll(replaceButton, cancelButton);

                Optional<ButtonType> result = alert.showAndWait();
                if (result.isPresent() && result.get() == replaceButton) {
                    // User chose to replace bad words
                    commentText = BadWordFilter.filter(commentText);
                } else {
                    // User chose to cancel
                    return;
                }
            }

            Comment c = new Comment();
            c.setPost(po.getById(getItem().getId()));
            c.setContenu(commentText);
            c.setUser(currentUser);
            c.setDate(LocalDateTime.now().toLocalDate());
            commentService.ajouter(c);
            commentField.clear();
            updateComments(getItem().getId());

            // Create notification
            String message = currentUser.getFullName() + " commented on your post: " + getItem().getTitre();
            notificationService.addNotification(message, getItem().getUser(), getItem());

            showInfo("Success", "Comment added successfully!");
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
                    setGraphic(null);
                    setContextMenu(null);
                } else {
                    // Create the main container for the comment
                    HBox commentContainer = new HBox(5);
                    commentContainer.setPadding(new Insets(5));
                    commentContainer.setStyle("-fx-background-color: #f5f5f5; -fx-background-radius: 10px;");

                    // User emoji on the left
                    Label userEmoji = new Label("👤");
                    userEmoji.setStyle("-fx-font-size: 14px;");

                    // Comment text in the middle
                    String userName = (comment.getUser() != null)
                            ? comment.getUser().getFullName()
                            : "Utilisateur inconnu";
                    Label commentText = new Label(userName + ": " + comment.getContenu());
                    commentText.setStyle("-fx-font-size: 14px; -fx-padding: 0 5 0 5;");
                    commentText.setMaxWidth(Double.MAX_VALUE);
                    HBox.setHgrow(commentText, Priority.ALWAYS);

                    // Speaker button on the right
                    Button speakerButton = new Button("🔊");
                    speakerButton.setStyle("-fx-background-color: transparent; -fx-border-color: #3498DB;");
                    speakerButton.setOnAction(e -> textToSpeech.speak(comment.getContenu()));

                    // Add all elements to the container
                    commentContainer.getChildren().addAll(userEmoji, commentText, speakerButton);

                    // Create the context menu
                    ContextMenu contextMenu = new ContextMenu();

                    // ✏️ Update Option (only show if user owns comment or is admin)
                    if (currentUser.getId() == comment.getUser().getId() ||
                            currentUser.getRole().equalsIgnoreCase("ROLE_ADMIN")) {
                        MenuItem editItem = new MenuItem("✏️ Update");
                        editItem.setOnAction(e -> openCommentEditPage(comment));
                        contextMenu.getItems().add(editItem);
                    }

                    // 🗑️ Delete Option (only show if user owns comment or is admin)
                    if (currentUser.getId() == comment.getUser().getId() ||
                            currentUser.getRole().equalsIgnoreCase("ROLE_ADMIN")) {
                        MenuItem deleteItem = new MenuItem("🗑️ Delete");
                        deleteItem.setOnAction(e -> handleDeleteComment(comment));
                        contextMenu.getItems().add(deleteItem);
                    }

                    setContextMenu(contextMenu);
                    setGraphic(commentContainer);
                }
            }
        });

        commentList.getItems().setAll(comments);
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
