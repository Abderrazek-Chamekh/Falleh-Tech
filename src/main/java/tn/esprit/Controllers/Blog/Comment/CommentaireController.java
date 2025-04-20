package tn.esprit.Controllers.Blog.Comment;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.TextArea;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;
import tn.esprit.entities.Comment;
import tn.esprit.entities.Post;
import tn.esprit.entities.User;
import tn.esprit.services.ServiceComment;
import tn.esprit.services.UserService;
import tn.esprit.tools.SessionManager;

import java.io.IOException;
import java.sql.SQLException;
import java.util.Objects;

public class CommentaireController {
    @FXML
    private BorderPane borderPane;
    @FXML
    private TextArea txtCommentaire; // Editable comment field
    private Comment selectedComment; // Current comment being updated/deleted

    private final ServiceComment commentService = new ServiceComment();
    public CommentaireController() throws SQLException {
    }

    public void setCommentAndPublication(Comment comment) {
        User currentUser = SessionManager.getInstance().getCurrentUser();

        // Check if current user is not admin and not the comment owner
        if (!currentUser.getRole().equalsIgnoreCase("ROLE_ADMIN") &&
                (comment.getUser() == null || !Objects.equals(comment.getUser().getId(), currentUser.getId()))) {
            showError("Permission Denied", "You can only edit your own comments");

            // Close the window immediately if unauthorized
            Stage stage = (Stage) borderPane.getScene().getWindow();
            stage.close();
            return;
        }

        this.selectedComment = comment;
        if (comment != null) {
            txtCommentaire.setText(comment.getContenu());
        }
    }

    @FXML
    public void supprimerCommentaire(ActionEvent event) {
        if (selectedComment == null) {
            showError("Error", "No comment selected to delete.");
            return;
        }

        User currentUser = SessionManager.getInstance().getCurrentUser();
        if (!currentUser.getRole().equalsIgnoreCase("ROLE_ADMIN") &&
                (selectedComment.getUser() == null || !Objects.equals(selectedComment.getUser().getId(), currentUser.getId()))) {
            showError("Permission Denied", "You can only delete your own comments");
            return;
        }

        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Delete Confirmation");
        alert.setHeaderText("Are you sure you want to delete this comment?");
        alert.setContentText("This action cannot be undone.");

        alert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                try {
                    commentService.supprimer(selectedComment);
                    showSuccess("Success", "Comment deleted successfully.");
                    closeWindow(event);
                } catch (Exception e) {
                    showError("Error", "Failed to delete the comment: " + e.getMessage());
                }
            }
        });
    }
    @FXML
    public void modifierCommentaire(ActionEvent event) {
        if (selectedComment == null) {
            showError("Error", "No comment selected to modify.");
            return;
        }

        User currentUser = SessionManager.getInstance().getCurrentUser();
        if (!currentUser.getRole().equalsIgnoreCase("ROLE_ADMIN") &&
                (selectedComment.getUser() == null || !Objects.equals(selectedComment.getUser().getId(), currentUser.getId()))) {
            showError("Permission Denied", "You can only edit your own comments");
            return;
        }

        String updatedContent = txtCommentaire.getText().trim();
        if (updatedContent.isEmpty()) {
            showError("Error", "The comment cannot be empty.");
            return;
        }

        try {
            selectedComment.setContenu(updatedContent);
            commentService.modifier(selectedComment);
            showSuccess("Success", "Comment updated successfully.");
            closeWindow(event);
        } catch (Exception e) {
            showError("Error", "Failed to update the comment: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    public void handleBack(ActionEvent event) {
        closeWindow(event);
    }

    private void closeWindow(ActionEvent event) {
        Stage stage = (Stage) ((Button) event.getSource()).getScene().getWindow();
        stage.close();
    }
    private void showError(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
    private void showSuccess(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
