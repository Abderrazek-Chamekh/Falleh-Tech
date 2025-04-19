package tn.esprit.Controllers.User.Authentication;

import javafx.fxml.FXML;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

public class AddAccountDialogController {

    @FXML
    private TextField nameField;
    @FXML
    private TextField emailField;

    private AddAccountCallback callback;

    public void setCallback(AddAccountCallback callback) {
        this.callback = callback;
    }

    @FXML
    private void onAdd() {
        String name = nameField.getText().trim();
        String email = emailField.getText().trim();

        if (!name.isEmpty() && !email.isEmpty() && callback != null) {
            callback.onAccountAdded(name, email);
        }

        close();
    }

    @FXML
    private void onCancel() {
        close();
    }

    private void close() {
        ((Stage) nameField.getScene().getWindow()).close();
    }

    public interface AddAccountCallback {
        void onAccountAdded(String name, String email);
    }
}
