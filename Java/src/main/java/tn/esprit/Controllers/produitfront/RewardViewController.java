package tn.esprit.Controllers.produitfront;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.stage.FileChooser;
import tn.esprit.entities.Reward;
import tn.esprit.entities.User;
import tn.esprit.services.RewardService;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.ResourceBundle;

public class RewardViewController implements Initializable {

    @FXML
    private ListView<Reward> rewardListView;

    private Long currentUserId;
    private boolean initialized = false;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        initialized = true;

        if (currentUserId != null) {
            chargerRewards();
        }
    }

    public void setUser(User user) {
        if (user != null) {
            this.currentUserId = user.getId().longValue();
            if (initialized) {
                chargerRewards();
            }
        }
    }

    public void chargerRewards() {
        if (currentUserId == null) return;

        List<Reward> rewards = new RewardService().getRewardsByUser(currentUserId);
        ObservableList<Reward> items = FXCollections.observableArrayList(rewards);

        rewardListView.setItems(items);

        rewardListView.setCellFactory(param -> new ListCell<>() {
            @Override
            protected void updateItem(Reward item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setGraphic(null);
                    setText(null);
                } else {
                    Label label = new Label(item.getType().equalsIgnoreCase("pdf")
                            ? " PDF: " + item.getValue()
                            : "Code Promo: " + item.getValue());
                    label.getStyleClass().add("reward-label");

                    Button actionButton = new Button(item.getType().equalsIgnoreCase("pdf") ? "Télécharger" : "Copier");
                    actionButton.getStyleClass().add("reward-button");

                    actionButton.setOnAction(event -> {
                        if (item.getType().equalsIgnoreCase("pdf")) {
                            telechargerPDF(item.getValue());
                        } else if (item.getType().equalsIgnoreCase("code_promo")) {
                            copierCode(item.getValue());
                        }
                    });


                    Region spacer = new Region();
                    HBox.setHgrow(spacer, Priority.ALWAYS);

                    HBox hbox = new HBox(10, label, spacer, actionButton);
                    hbox.setPadding(new Insets(10));
                    hbox.setAlignment(Pos.CENTER_LEFT);

                    setGraphic(hbox);
                    setText(null);
                }
            }
        });
    }

    private void telechargerPDF(String filename) {
        try {
            // Charger le PDF via le classpath
            var inputStream = getClass().getResourceAsStream("/pdfs/" + filename);
            if (inputStream == null) {
                System.err.println("❌ Fichier PDF introuvable dans resources : " + filename);
                return;
            }

            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Enregistrer le PDF");
            fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Fichiers PDF", "*.pdf"));
            fileChooser.setInitialFileName(filename);

            File destination = fileChooser.showSaveDialog(rewardListView.getScene().getWindow());

            if (destination != null) {
                Files.copy(inputStream, destination.toPath(), StandardCopyOption.REPLACE_EXISTING);
                showAlert("Succès", "✅ PDF téléchargé avec succès !");
            }
        } catch (IOException e) {
            System.err.println("❌ Erreur lors du téléchargement du PDF : " + e.getMessage());
            showAlert("Erreur", "❌ Une erreur est survenue lors du téléchargement !");
        }
    }


    private void copierCode(String code) {
        ClipboardContent content = new ClipboardContent();
        content.putString(code);
        Clipboard.getSystemClipboard().setContent(content);
        showAlert("Succès", "✅ Code promo copié dans le presse-papiers !");
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
