package tn.esprit.Controllers.ouvrier;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.Stage;
import tn.esprit.entities.User;

import java.io.File;

public class UserProfilePopupController {

    @FXML private Label lblName;
    @FXML private Label lblEmail;
    @FXML private Label lblPhone;
    @FXML private Label lblAddress;
    @FXML private ImageView profileImage;


    public void setUser(User user) {
        lblName.setText("👤 " + safe(user.getName()) + " " + safe(user.getLastName()));
        lblEmail.setText("📧 " + safe(user.getEmail()));
        lblPhone.setText("📞 " + safe(user.getPhoneNumber()));
        lblAddress.setText("📍 " + safe(user.getLocation()));

        File imageFile = new File("/images/default_profile.png");
        if (imageFile.exists()) {
            profileImage.setImage(new Image(imageFile.toURI().toString()));
        } else {
            profileImage.setImage(new Image("/images/default_profile.png"));        }
    }
    private String safe(String val) {
        return (val == null || val.trim().isEmpty()) ? "non fourni" : val;
    }

    private String defaultText(String value) {
        return value == null || value.trim().isEmpty() ? "non fourni" : value;
    }

    @FXML
    private void handleClose() {
        ((Stage) lblName.getScene().getWindow()).close();
    }
}
