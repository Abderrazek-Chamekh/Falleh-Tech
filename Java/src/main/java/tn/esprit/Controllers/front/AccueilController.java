package tn.esprit.Controllers.front;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.control.Label;
import javafx.scene.web.WebView;
import javafx.stage.Stage;
import tn.esprit.services.FlammeService;

import java.awt.Desktop;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ResourceBundle;

public class AccueilController implements Initializable {

    @FXML private ImageView carottesImage, laitueImage, tomatesImage, pommesImage, stockImage;
    @FXML private GridPane widgetGrid;
    @FXML private Button btnAddWidget;
    @FXML private VBox produitsWidget, offresWidget, blogWidget, statsWidget;
    @FXML private VBox optionsContainer; // <= ajout ici pour montrer/cacher les options
    private VBox draggedWidget = null;
    private Long currentUserId;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        System.out.println("✅ AccueilController initialized");

        loadImages();
        setupDraggable(produitsWidget);
        setupDraggable(offresWidget);
        setupDraggable(blogWidget);
        setupDraggable(statsWidget);
        setupDropTargets();

        produitsWidget.setId("produitsWidget");
        offresWidget.setId("offresWidget");
        blogWidget.setId("blogWidget");
        statsWidget.setId("statsWidget");

        btnAddWidget.setOnAction(e -> toggleOptions());
    }

    private void loadImages() {
        carottesImage.setImage(loadImage("photos/carottes.jpg"));
        laitueImage.setImage(loadImage("photos/laitue.jpg"));
        tomatesImage.setImage(loadImage("photos/tomates.jpg"));
        pommesImage.setImage(loadImage("photos/pommes.jpg"));
        stockImage.setImage(loadImage("photos/stock_pie.png"));
    }

    private Image loadImage(String path) {
        File file = new File(path);
        return file.exists() ? new Image(file.toURI().toString()) : null;
    }

    private void setupDraggable(VBox widget) {
        widget.setOnDragDetected(event -> {
            if (widget.getId() == null) {
                System.out.println("❌ Widget ID is null!");
                return;
            }
            Dragboard db = widget.startDragAndDrop(TransferMode.MOVE);
            ClipboardContent content = new ClipboardContent();
            content.putString("WIDGET:" + widget.getId());
            db.setContent(content);
            draggedWidget = widget;
            event.consume();
        });
    }

    private void setupDropTargets() {
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 3; col++) {
                final int targetRow = row;
                final int targetCol = col;
                StackPane cell = new StackPane();
                cell.setMinSize(200, 200);
                cell.setStyle("-fx-background-color: transparent; -fx-border-color: #ddd;");

                GridPane.setRowIndex(cell, targetRow);
                GridPane.setColumnIndex(cell, targetCol);

                cell.setOnDragOver(event -> {
                    if (event.getGestureSource() != cell && event.getDragboard().hasString()) {
                        event.acceptTransferModes(TransferMode.MOVE);
                    }
                    event.consume();
                });

                cell.setOnDragDropped(event -> {
                    if (draggedWidget != null) {
                        widgetGrid.getChildren().remove(draggedWidget);
                        GridPane.setRowIndex(draggedWidget, targetRow);
                        GridPane.setColumnIndex(draggedWidget, targetCol);
                        widgetGrid.getChildren().add(draggedWidget);
                        draggedWidget = null;
                        event.setDropCompleted(true);
                    } else {
                        event.setDropCompleted(false);
                    }
                    event.consume();
                });

                widgetGrid.getChildren().add(cell);
                cell.toBack();
            }
        }
    }

    public void setUserId(Long userId) {
        this.currentUserId = userId;
    }

    // 👇 Ouvre/ferme les options sous "Ajouter Widget"
    @FXML
    private void toggleOptions() {
        boolean show = !optionsContainer.isVisible();
        optionsContainer.setVisible(show);
        optionsContainer.setManaged(show);
    }

    // 👇 Ajoute le widget Détection de Plante
    @FXML
    private void ajouterWidgetDetectionPlante() {
        VBox detectionWidget = createSimpleWidget("🌿 Détection de Plantes avec IA", "#f1f8e9");
        setupDraggable(detectionWidget);
        widgetGrid.getChildren().add(detectionWidget);
    }

    // 👇 Ajoute le widget Mini-Jeu
    @FXML
    private void ajouterWidgetMiniJeu() {
        VBox miniJeuWidget = createSimpleWidget("🎮 Mini-Jeu Agricole", "#e1f5fe");
        setupDraggable(miniJeuWidget);
        widgetGrid.getChildren().add(miniJeuWidget);
    }

    // 👇 Ajoute un Nouveau Widget générique
    @FXML
    private void ajouterNouveauWidget() {
        VBox nouveauWidget = createSimpleWidget("📦 Nouveau Widget", "#ffe0b2");
        setupDraggable(nouveauWidget);
        widgetGrid.getChildren().add(nouveauWidget);
    }

    private VBox createSimpleWidget(String title, String backgroundColor) {
        VBox widget = new VBox();
        widget.setSpacing(10);
        widget.setStyle("-fx-background-color: " + backgroundColor + "; -fx-padding: 15; -fx-border-color: #bbb; -fx-border-radius: 5px; -fx-border-width: 2px;");

        Label label = new Label(title);
        label.setStyle("-fx-font-weight: bold; -fx-font-size: 16px;");

        widget.getChildren().add(label);
        return widget;
    }

    @FXML private void ouvrirDetectionPlante() {
        try {
            Stage stage = new Stage();
            WebView webView = new WebView();
            File file = new File("C:/xampp/htdocs/plant-detect.html");
            webView.getEngine().load(file.toURI().toString());
            Scene scene = new Scene(webView, 800, 600);
            stage.setScene(scene);
            stage.setTitle("Détection de Plante 🌿");
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML private void ouvrirJeuRecette() {
        FlammeService flammeService = FlammeService.getInstance();
        if (flammeService.peutAjouterFlammes(currentUserId)) {
            Stage stage = new Stage();
            WebView webView = new WebView();
            webView.getEngine().load("http://localhost/jeu-recettes.html");
            stage.setScene(new Scene(webView, 750, 600));
            stage.setTitle("Jeu des Recettes 🍲");
            stage.show();
        } else {
            showAlert("⛔ Limite atteinte", "Vous avez déjà gagné 3 flammes aujourd'hui. Revenez demain !");
        }
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    @FXML private void onRemoveProduits() {
        widgetGrid.getChildren().remove(produitsWidget);
    }

    @FXML private void onRemoveOffres() {
        widgetGrid.getChildren().remove(offresWidget);
    }

    @FXML private void onRemoveBlog() {
        widgetGrid.getChildren().remove(blogWidget);
    }

    @FXML private void onRemoveStats() {
        widgetGrid.getChildren().remove(statsWidget);
    }

    @FXML private void goToBlog() {
        System.out.println("Blog link clicked!");
    }

}
