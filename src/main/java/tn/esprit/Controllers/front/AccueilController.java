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
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;
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
}