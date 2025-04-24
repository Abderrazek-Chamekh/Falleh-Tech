package tn.esprit.controllers;

import javafx.fxml.FXML;
import javafx.scene.control.Tooltip;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.shape.Circle;

import java.util.List;

public class HeatmapController {

    @FXML private AnchorPane rootPane;
    @FXML private ImageView mapImage;

    @FXML
    public void initialize() {
        System.out.println("✅ HeatmapController initialized. Map loaded.");
        renderHeatPoints();
    }

    public static class HeatPoint {
        private final String region;
        private final double normalizedX;
        private final double normalizedY;
        private final double intensity;

        public HeatPoint(String region, double normalizedX, double normalizedY, double intensity) {
            this.region = region;
            this.normalizedX = normalizedX;
            this.normalizedY = normalizedY;
            this.intensity = intensity;
        }

        public String getRegion() { return region; }

        public double getPixelX(double width) {
            return normalizedX * width;
        }

        public double getPixelY(double height) {
            return normalizedY * height;
        }

        public double getIntensity() { return intensity; }
    }

    // ✅ Normalized (x,y) based on 800x560 image
    private final List<HeatPoint> heatPoints = List.of(
            new HeatPoint("Ariana", 0.393, 0.13, 0.6),
            new HeatPoint("Béja", 0.331, 0.167, 0.6),
            new HeatPoint("Ben Arous", 0.400, 0.146, 0.6),
            new HeatPoint("Bizerte", 0.355, 0.088, 0.6),
            new HeatPoint("Gabès", 0.375, 0.531, 0.6),
            new HeatPoint("Gafsa", 0.299, 0.456, 0.6),
            new HeatPoint("Jendouba", 0.283, 0.164, 0.6),
            new HeatPoint("Kairouan", 0.367, 0.289, 0.6),
            new HeatPoint("Kasserine", 0.298, 0.35, 0.6),
            new HeatPoint("Kebili", 0.284, 0.576, 0.6),
            new HeatPoint("Kef", 0.289, 0.238, 0.6),
            new HeatPoint("Mahdia", 0.422, 0.33, 0.6),
            new HeatPoint("Manouba", 0.39, 0.133, 0.6),
            new HeatPoint("Medenine", 0.457, 0.595, 0.6),
            new HeatPoint("Monastir", 0.436, 0.296, 0.6),
            new HeatPoint("Nabeul", 0.431, 0.159, 0.6),
            new HeatPoint("Sfax", 0.442, 0.387, 0.6),
            new HeatPoint("Sidi Bouzid", 0.354, 0.398, 0.6),
            new HeatPoint("Siliana", 0.331, 0.249, 0.6),
            new HeatPoint("Sousse", 0.399, 0.16, 0.6),
            new HeatPoint("Tataouine", 312.0, 408.0, 0.6),
            new HeatPoint("Tozeur", 0.222, 0.524, 0.6),
            new HeatPoint("Tunis", 0.381, 0.132, 0.6),
            new HeatPoint("Zaghouan", 0.377, 0.198, 0.6)
    );

    private void renderHeatPoints() {
        double mapWidth = mapImage.getFitWidth();
        double mapHeight = mapImage.getFitHeight();

        for (HeatPoint hp : heatPoints) {
            Circle heatCircle = new Circle();
            heatCircle.setCenterX(hp.getPixelX(mapWidth));
            heatCircle.setCenterY(hp.getPixelY(mapHeight));
            heatCircle.setRadius(25);
            heatCircle.setStyle(
                    "-fx-fill: rgba(255, 0, 0, " + hp.getIntensity() + ");" +
                            "-fx-stroke: white;" +
                            "-fx-stroke-width: 1.5;"
            );

            Tooltip tooltip = new Tooltip(hp.getRegion());
            Tooltip.install(heatCircle, tooltip);

            rootPane.getChildren().add(heatCircle);
        }
    }

    @FXML
    private void handleMouseClick(MouseEvent event) {
        double x = event.getX();
        double y = event.getY();
        System.out.println("🖱️ Clicked at X: " + x + ", Y: " + y);
    }
}
