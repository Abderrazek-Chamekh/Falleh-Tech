package tn.esprit.controllers;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Line;
import javafx.scene.shape.Polygon;

import java.util.ArrayList;
import java.util.List;

public class LandDrawController {

    @FXML private Pane drawPane;
    @FXML private Label coordLabel;
    @FXML private Button btnSave;
    @FXML private Button btnReset;

    private final List<Double> currentPoints = new ArrayList<>();
    private final List<Circle> currentMarkers = new ArrayList<>();
    private final List<Line> currentLines = new ArrayList<>();

    private final List<Polygon> allPolygons = new ArrayList<>();
    private final List<String> allZoneNames = new ArrayList<>();

    private Circle firstPoint = null;
    private boolean shapeClosed = false;
    private Polygon currentPolygon = null;
    private String currentZoneName = "";

    @FXML
    public void initialize() {
        drawPane.setOnMouseClicked(this::handleClick);
        drawPane.setOnMouseMoved(this::updateCoordinates);
        btnSave.setDisable(true);
    }

    private void handleClick(MouseEvent event) {
        if (shapeClosed) return;

        double x = event.getX();
        double y = event.getY();

        if (firstPoint != null && Math.hypot(x - firstPoint.getCenterX(), y - firstPoint.getCenterY()) < 15) {
            closeShape();
            return;
        }

        currentPoints.add(x);
        currentPoints.add(y);

        Circle point = new Circle(x, y, 5, Color.DARKGREEN);
        currentMarkers.add(point);
        drawPane.getChildren().add(point);

        if (currentMarkers.size() > 1) {
            Circle prev = currentMarkers.get(currentMarkers.size() - 2);
            Line line = new Line(prev.getCenterX(), prev.getCenterY(), x, y);
            line.setStroke(Color.DARKGRAY);
            currentLines.add(line);
            drawPane.getChildren().add(line);
        }

        if (firstPoint == null) {
            firstPoint = point;
        }
    }

    private void updateCoordinates(MouseEvent event) {
        coordLabel.setText("🖱️ X: " + (int)event.getX() + ", Y: " + (int)event.getY());
    }

    private void closeShape() {
        if (currentPoints.size() < 6) {
            showAlert("Veuillez sélectionner au moins 3 points.");
            return;
        }

        Circle last = currentMarkers.get(currentMarkers.size() - 1);
        Line closingLine = new Line(last.getCenterX(), last.getCenterY(), firstPoint.getCenterX(), firstPoint.getCenterY());
        closingLine.setStroke(Color.DARKGRAY);
        drawPane.getChildren().add(closingLine);
        currentLines.add(closingLine);

        currentPolygon = new Polygon();
        currentPolygon.getPoints().addAll(currentPoints);
        currentPolygon.setFill(Color.rgb(100, 200, 100, 0.4));
        currentPolygon.setStroke(Color.FORESTGREEN);
        currentPolygon.setStrokeWidth(2);
        drawPane.getChildren().add(currentPolygon);
        currentPolygon.toBack();

        shapeClosed = true;
        btnSave.setDisable(false);
        askZoneName();
    }

    private void askZoneName() {
        TextInputDialog dialog = new TextInputDialog("Zone " + (allZoneNames.size() + 1));
        dialog.setTitle("Nom de la parcelle");
        dialog.setHeaderText("Entrez un nom pour cette zone");
        dialog.setContentText("Nom:");
        dialog.showAndWait().ifPresent(name -> currentZoneName = name);
    }

    @FXML
    private void saveZone() {
        if (!shapeClosed || currentZoneName.isEmpty()) {
            showAlert("Zone invalide ou nom manquant.");
            return;
        }

        allPolygons.add(currentPolygon);
        allZoneNames.add(currentZoneName);

        System.out.println("✅ Zone sauvegardée: " + currentZoneName);
        System.out.println("Coordonnées: " + currentPolygon.getPoints());

        showAlert("✅ Zone \"" + currentZoneName + "\" sauvegardée avec succès !");

        // Reset current drawing state (but not canvas)
        clearCurrentZone();
    }

    @FXML
    private void resetDrawing() {
        clearCurrentZone();
        coordLabel.setText("");
    }

    private void clearCurrentZone() {
        for (Circle c : currentMarkers) drawPane.getChildren().remove(c);
        for (Line l : currentLines) drawPane.getChildren().remove(l);
        if (currentPolygon != null) drawPane.getChildren().remove(currentPolygon);

        currentPoints.clear();
        currentLines.clear();
        currentMarkers.clear();
        currentPolygon = null;
        firstPoint = null;
        shapeClosed = false;
        currentZoneName = "";
        btnSave.setDisable(true);
    }

    private void showAlert(String msg) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Information");
        alert.setContentText(msg);
        alert.showAndWait();
    }
}
