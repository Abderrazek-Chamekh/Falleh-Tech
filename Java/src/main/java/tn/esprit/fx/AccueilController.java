package tn.esprit.fx;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

public class AccueilController implements Initializable {

    @FXML private GridPane widgetGrid;
    @FXML private Button btnAddWidget;

    private GridManager gridManager;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        System.out.println("✅ AccueilController initialized");

        gridManager = new GridManager(widgetGrid);

        setupDropTargets();
        btnAddWidget.setOnAction(e -> openAddWidgetPopup());

        loadStatsPredictionsWidget();
    }

    private void setupDropTargets() {
        for (int row = 0; row < 2; row++) {
            final int finalRow = row;
            for (int col = 0; col < 3; col++) {
                final int finalCol = col;

                StackPane cell = new StackPane();
                cell.setMinSize(300, 300);
                cell.setStyle("-fx-background-color: transparent; -fx-border-color: #ddd;");
                cell.setOnDragOver(event -> {
                    if (event.getGestureSource() != cell && event.getDragboard().hasString()) {
                        event.acceptTransferModes(TransferMode.MOVE);
                    }
                    event.consume();
                });
                cell.setOnDragDropped(event -> {
                    gridManager.onCellClicked(finalRow, finalCol);
                    event.setDropCompleted(true);
                    event.consume();
                });

                GridPane.setRowIndex(cell, finalRow);
                GridPane.setColumnIndex(cell, finalCol);
                widgetGrid.getChildren().add(cell);
                cell.toBack();
            }
        }
    }

    private void openAddWidgetPopup() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/front/AddWidgetDialog.fxml"));
            Parent root = loader.load();

            AddWidgetDialogController controller = loader.getController();
            controller.setAccueilController(this);

            Stage popupStage = new Stage();
            popupStage.initModality(Modality.APPLICATION_MODAL);
            popupStage.setTitle("Ajouter un Widget");
            popupStage.setScene(new Scene(root));
            popupStage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void loadStatsPredictionsWidget() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/front/StatistiquesPredictions.fxml"));
            VBox chartWidget = loader.load();
            chartWidget.setId("statsPredictionsWidget");

            chartWidget.setOnDragDetected((MouseEvent event) -> {
                Dragboard db = chartWidget.startDragAndDrop(TransferMode.MOVE);
                ClipboardContent content = new ClipboardContent();
                content.putString("dragging");
                db.setContent(content);
                gridManager.activateMoveMode(chartWidget);
                event.consume();
            });

            GridPane.setRowIndex(chartWidget, 0);
            GridPane.setColumnIndex(chartWidget, 0);
            GridPane.setColumnSpan(chartWidget, 3);
            widgetGrid.getChildren().add(chartWidget);

            System.out.println("📈 Chart widget loaded and spans 3 columns");

        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("❌ Error loading StatistiquesPredictions.fxml");
        }
    }
}
