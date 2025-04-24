// StatistiquesPredictionsController.java
package tn.esprit.controllers;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.util.Duration;
import tn.esprit.entities.OffreEmploi;
import tn.esprit.services.ServiceOffreEmploi;

import java.net.URL;
import java.time.LocalDate;
import java.time.format.TextStyle;
import java.util.*;
import java.util.stream.Collectors;

public class StatistiquesPredictionsController implements Initializable {


    @FXML private LineChart<String, Number> lineChart;

    private final ServiceOffreEmploi serviceOffre = new ServiceOffreEmploi();

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        setupChart();
        animateChart();
    }

    private void setupChart() {
        lineChart.setTitle("Évolution mensuelle des offres d'emploi");
        lineChart.getXAxis().setLabel("Mois");
        lineChart.getYAxis().setLabel("");

        List<OffreEmploi> allOffers = serviceOffre.getAll();

        // Group real data by month
        Map<String, Long> realData = allOffers.stream()
                .filter(o -> o.getStartDate().isBefore(LocalDate.now()))
                .collect(Collectors.groupingBy(
                        o -> o.getStartDate().getMonth().getDisplayName(TextStyle.SHORT, Locale.FRENCH),
                        TreeMap::new,
                        Collectors.counting()
                ));

        XYChart.Series<String, Number> actualSeries = new XYChart.Series<>();
        actualSeries.setName("Offres Actuelles");

        XYChart.Series<String, Number> predictionSeries = new XYChart.Series<>();
        predictionSeries.setName("Prévision");

        List<String> months = new ArrayList<>(realData.keySet());

        // Add actual data (6 months max)
        for (int i = 0; i < Math.min(6, months.size()); i++) {
            String month = months.get(i);
            actualSeries.getData().add(new XYChart.Data<>(month, realData.get(month)));
        }

        // Add 3 future predicted months (dotted)
        String[] futureMonths = {"juil.", "août", "sept."};
        Random random = new Random();

        for (String month : futureMonths) {
            int predicted = 10 + random.nextInt(15); // mock prediction
            XYChart.Data<String, Number> predictionPoint = new XYChart.Data<>(month, predicted);
            predictionSeries.getData().add(predictionPoint);
        }

        lineChart.getData().addAll(actualSeries, predictionSeries);

        // Apply dotted style for predictions after UI is rendered
        predictionSeries.getData().forEach(data -> {
            data.nodeProperty().addListener((obs, oldNode, newNode) -> {
                if (newNode != null) {
                    newNode.setStyle("-fx-stroke-dash-array: 12 6;");
                }
            });
        });
    }


    private void animateChart() {
        for (XYChart.Series<String, Number> series : lineChart.getData()) {
            for (XYChart.Data<String, Number> data : series.getData()) {
                data.getNode().setOpacity(0);
            }
        }

        Timeline timeline = new Timeline();
        int delay = 0;
        for (XYChart.Series<String, Number> series : lineChart.getData()) {
            for (XYChart.Data<String, Number> data : series.getData()) {
                KeyFrame keyFrame = new KeyFrame(Duration.millis(delay), e -> data.getNode().setOpacity(1));
                timeline.getKeyFrames().add(keyFrame);
                delay += 150;
            }
        }
        timeline.play();
    }
}
