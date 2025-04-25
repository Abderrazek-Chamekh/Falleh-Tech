package tn.esprit.Controllers.front;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.web.WebView;
import javafx.stage.Stage;
import javafx.scene.control.Label;
import javafx.scene.control.Hyperlink;

import java.awt.Desktop;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ResourceBundle;

public class AccueilController implements Initializable {

    @FXML private ImageView carottesImage;
    @FXML private ImageView laitueImage;
    @FXML private ImageView tomatesImage;
    @FXML private ImageView pommesImage;
    @FXML private ImageView stockImage;

    @FXML private GridPane widgetGrid;
    @FXML private Button btnAddWidget;

    @FXML private VBox produitsWidget;
    @FXML private VBox offresWidget;
    @FXML private VBox blogWidget;
    @FXML private VBox statsWidget;

    private VBox draggedWidget = null;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        System.out.println("✅ AccueilController initialized");
        loadImages();

        produitsWidget.setId("produitsWidget");
        offresWidget.setId("offresWidget");
        blogWidget.setId("blogWidget");
        statsWidget.setId("statsWidget");

        setupDraggable(produitsWidget);
        setupDraggable(offresWidget);
        setupDraggable(blogWidget);
        setupDraggable(statsWidget);
        setupDropTargets();

        btnAddWidget.setOnAction(e -> ajouterWidgetDetectionPlante());
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
            System.out.println("👉 Drag started on " + widget.getId());
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

    private void ajouterWidgetDetectionPlante() {
        VBox detectionWidget = new VBox();
        detectionWidget.setSpacing(10);
        detectionWidget.setStyle("-fx-background-color: #f1f8e9; -fx-padding: 15; -fx-border-color: #8bc34a; -fx-border-width: 2px; -fx-border-radius: 5px;");

        Label titre = new Label("🌿 Détection de Plantes avec IA");
        titre.setStyle("-fx-font-weight: bold; -fx-font-size: 16px; -fx-text-fill: #33691e;");

        Button openHtml = new Button("🔍 Ouvrir la page de détection");
        openHtml.setStyle("-fx-background-color: #aed581; -fx-text-fill: white;");
        openHtml.setOnAction(e -> {
            try {
                Desktop.getDesktop().browse(new URI("http://localhost/plant-detect.html"));
            } catch (IOException | URISyntaxException ex) {
                System.err.println("❌ Erreur lors de l'ouverture de la page HTML : " + ex.getMessage());
            }
        });

        Button closeBtn = new Button("❌ Fermer");
        closeBtn.setStyle("-fx-background-color: #e57373; -fx-text-fill: white;");
        closeBtn.setOnAction(e -> widgetGrid.getChildren().remove(detectionWidget));

        detectionWidget.getChildren().addAll(titre, openHtml, closeBtn);

        GridPane.setRowIndex(detectionWidget, 2);
        GridPane.setColumnIndex(detectionWidget, 0);
        detectionWidget.setId("widgetDetectionPlante");

        setupDraggable(detectionWidget);
        widgetGrid.getChildren().add(detectionWidget);
    }
    @FXML
    private void ouvrirDetectionPlante() {
        Stage webStage = new Stage();
        webStage.setTitle("Détection de Plante 🌿");

        javafx.scene.web.WebView webView = new javafx.scene.web.WebView();
        javafx.scene.web.WebEngine webEngine = webView.getEngine();

        // 🔽 Remplace ce chemin par le bon emplacement local de ton fichier HTML
        File htmlFile = new File("C:/xampp/htdocs/plant-detect.html");
        webEngine.load(htmlFile.toURI().toString());

        Scene scene = new Scene(webView, 800, 600);
        webStage.setScene(scene);
        webStage.show();
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

    @FXML private void goToMesOffres() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/farmer_front/MesOffresView.fxml"));
            Parent view = loader.load();
            Stage stage = (Stage) widgetGrid.getScene().getWindow();
            Scene scene = new Scene(view);
            scene.getStylesheets().add(getClass().getResource("/styles/main.css").toExternalForm());
            stage.setScene(scene);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    @FXML
    private void ouvrirJeuRecette() {
        Stage stage = new Stage();
        WebView webView = new WebView();
        webView.getEngine().load("http://localhost/jeu-recettes.html");
        Scene scene = new Scene(webView, 750, 600);
        stage.setTitle("Jeu des Recettes 🍲");
        stage.setScene(scene);
        stage.show();
    }

}
