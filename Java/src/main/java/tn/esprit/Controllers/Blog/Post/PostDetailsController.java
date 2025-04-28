package tn.esprit.Controllers.Blog.Post;

import javafx.animation.FadeTransition;
import javafx.animation.ScaleTransition;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javafx.util.Duration;
import tn.esprit.entities.Comment;
import tn.esprit.entities.Post;
import tn.esprit.services.ServiceComment;
import tn.esprit.utils.ImageUtils;

import java.util.List;

public class PostDetailsController {
    @FXML private ImageView postImage;
    @FXML private Label titleLabel;
    @FXML private Label dateLabel;
    @FXML private Label categoryLabel;
    @FXML private Label likesLabel;
    @FXML private Text contentText;
    @FXML private VBox commentSection;
    @FXML private ListView<Comment> commentsList;
    private ServiceComment commentService = new ServiceComment();

    public void initialize() {
        // Add fade-in animation when loading
        FadeTransition fadeTransition = new FadeTransition(Duration.millis(500), commentSection);
        fadeTransition.setFromValue(0);
        fadeTransition.setToValue(1);
        fadeTransition.play();

        // Add scale animation for the image
        ScaleTransition scaleTransition = new ScaleTransition(Duration.millis(300), postImage);
        scaleTransition.setFromX(0.95);
        scaleTransition.setFromY(0.95);
        scaleTransition.setToX(1);
        scaleTransition.setToY(1);
        scaleTransition.play();

    }

    public void setPost(Post post) {
        titleLabel.setText(post.getTitre());
        contentText.setText(post.getContenu());
        dateLabel.setText("Posted: " + post.getDate().toString());
        categoryLabel.setText(post.getCategory());

        // Load image with fade-in effect
        FadeTransition fadeIn = new FadeTransition(Duration.millis(500), postImage);
        fadeIn.setFromValue(0);
        fadeIn.setToValue(1);

        postImage.setImage(ImageUtils.chargerDepuisNom(post.getImage()));
        System.out.println("Image path: " + post.getImage());

        List<Comment> comments = commentService.getCommentsByPost(post.getId());
        commentsList.getItems().setAll(comments);

        // Simple cell factory for comments
        commentsList.setCellFactory(param -> new ListCell<>() {
            @Override
            protected void updateItem(Comment comment, boolean empty) {
                super.updateItem(comment, empty);
                if (empty || comment == null) {
                    setText(null);
                    setGraphic(null);
                } else {
                    VBox commentBox = new VBox(5);
                    commentBox.setStyle("-fx-background-color: white; -fx-background-radius: 5; -fx-padding: 10; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.05), 3, 0, 0, 1);");

                    Label userLabel = new Label(comment.getUser().getName());
                    userLabel.setStyle("-fx-font-weight: bold; -fx-text-fill: #3498db;");

                    Label commentLabel = new Label(comment.getContenu());
                    commentLabel.setStyle("-fx-text-fill: #333; -fx-wrap-text: true;");

                    Label dateLabel = new Label(comment.getDate().toString());
                    dateLabel.setStyle("-fx-text-fill: #7f8c8d; -fx-font-size: 11px;");

                    commentBox.getChildren().addAll(userLabel, commentLabel, dateLabel);
                    setGraphic(commentBox);
                }
            }
        });
    }

    @FXML
    private void handleClose() {
        Stage stage = (Stage) titleLabel.getScene().getWindow();
        stage.close();
    }
}