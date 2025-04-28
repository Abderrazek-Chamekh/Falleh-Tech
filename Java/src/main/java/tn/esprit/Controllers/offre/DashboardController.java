package tn.esprit.Controllers.offre;

import javafx.animation.FadeTransition;
import javafx.animation.ScaleTransition;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.chart.*;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.layout.*;
import javafx.util.Duration;

public class DashboardController {

    @FXML private Label statsTitle;
    @FXML private ComboBox<String> comboStatsType;
    @FXML private AnchorPane chartContainer;
    @FXML private VBox statsBox;
    @FXML private Button btnCustomize;
    @FXML private HBox cardContainer;

    @FXML private VBox cardCandidatures, cardOffres, cardProduits, cardUsers;
    @FXML private Label lblCandidatures, lblOffres, lblProduits, lblUsers;

    @FXML
    public void initialize() {
        comboStatsType.setItems(FXCollections.observableArrayList("Produits", "Candidatures", "Utilisateurs", "Achats", "Posts"));

        animateCard(cardCandidatures);
        animateCard(cardOffres);
        animateCard(cardProduits);
        animateCard(cardUsers);

        animateNumber(lblCandidatures, 17, Duration.seconds(1.2));
        animateNumber(lblOffres, 9, Duration.seconds(1));
        animateNumber(lblProduits, 32, Duration.seconds(1.5));
        animateNumber(lblUsers, 15, Duration.seconds(1.3));
    }

    private void animateNumber(Label label, int targetValue, Duration duration) {
        final int fps = 30;
        final int steps = (int) (duration.toMillis() / (1000.0 / fps));
        final double increment = (double) targetValue / steps;

        new Thread(() -> {
            double value = 0;
            for (int i = 0; i < steps; i++) {
                value += increment;
                final int displayValue = (int) Math.floor(value);
                Platform.runLater(() -> label.setText(String.valueOf(displayValue)));
                try {
                    Thread.sleep((long) (1000.0 / fps));
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            Platform.runLater(() -> label.setText(String.valueOf(targetValue)));
        }).start();
    }

    private void animateCard(Node card) {
        ScaleTransition scale = new ScaleTransition(Duration.millis(400), card);
        scale.setFromX(0.8);
        scale.setFromY(0.8);
        scale.setToX(1);
        scale.setToY(1);

        FadeTransition fade = new FadeTransition(Duration.millis(400), card);
        fade.setFromValue(0);
        fade.setToValue(1);

        scale.play();
        fade.play();
    }

    @FXML
    private void closeCard(javafx.event.ActionEvent event) {
        Button closeBtn = (Button) event.getSource();
        StackPane parent = (StackPane) closeBtn.getParent();
        cardContainer.getChildren().remove(parent);
    }

    @FXML
    private void customizeCards() {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Personnalisation");
        alert.setHeaderText("Fonctionnalité à implémenter");
        alert.setContentText("Ajout dynamique de cartes à implémenter ici.");
        alert.showAndWait();
    }

    @FXML
    private void showSelectedStats() {
        String selected = comboStatsType.getValue();
        if (selected == null) return;

        statsTitle.setText("\uD83D\uDCCA Statistiques pour : " + selected);
        chartContainer.getChildren().clear();

        switch (selected) {
            case "Produits":
                drawBarChart("Produits", new String[]{"Vendus", "Stockés"}, new int[]{52, 24});
                break;
            case "Candidatures":
                drawPieChart("Candidatures", new String[]{"Acceptées", "Rejetées", "En attente"}, new int[]{10, 3, 4});
                break;
            case "Utilisateurs":
                drawPieChart("Utilisateurs", new String[]{"Admins", "Agriculteurs", "Ouvriers"}, new int[]{2, 8, 13});
                break;
            case "Achats":
                drawBarChart("Achats", new String[]{"Total"}, new int[]{1200});
                break;
            default:
                statsTitle.setText("Aucune donnée.");
        }
    }

    private void drawPieChart(String title, String[] labels, int[] values) {
        PieChart chart = new PieChart();
        chart.setTitle(title);
        chart.setLabelsVisible(true);
        chart.setLegendVisible(true);
        chart.setLegendSide(javafx.geometry.Side.RIGHT);
        chart.setClockwise(true);
        chart.setPrefSize(500, 400);

        for (int i = 0; i < labels.length; i++) {
            chart.getData().add(new PieChart.Data(labels[i], values[i]));
        }

        chartContainer.getChildren().add(chart);
        AnchorPane.setTopAnchor(chart, 0.0);
        AnchorPane.setBottomAnchor(chart, 0.0);
        AnchorPane.setLeftAnchor(chart, 0.0);
        AnchorPane.setRightAnchor(chart, 0.0);

        Platform.runLater(() -> animatePieSlices(chart));

        FadeTransition fade = new FadeTransition(Duration.millis(500), chart);
        fade.setFromValue(0);
        fade.setToValue(1);
        fade.play();
    }

    private void animatePieSlices(PieChart chart) {
        int delay = 0;
        for (PieChart.Data data : chart.getData()) {
            Node node = data.getNode();
            node.setScaleX(0);
            node.setScaleY(0);

            ScaleTransition st = new ScaleTransition(Duration.millis(600), node);
            st.setToX(1);
            st.setToY(1);
            st.setDelay(Duration.millis(delay));
            st.play();

            delay += 150;
        }
    }

    private void drawBarChart(String title, String[] categories, int[] values) {
        CategoryAxis xAxis = new CategoryAxis();
        NumberAxis yAxis = new NumberAxis();

        BarChart<String, Number> chart = new BarChart<>(xAxis, yAxis);
        chart.setTitle(title);
        xAxis.setLabel("Catégorie");
        yAxis.setLabel("Valeur");

        XYChart.Series<String, Number> series = new XYChart.Series<>();
        for (int i = 0; i < categories.length; i++) {
            series.getData().add(new XYChart.Data<>(categories[i], values[i]));
        }

        chart.getData().add(series);
        chart.setPrefSize(600, 400);

        animateChart(chart);
    }

    private void animateChart(Node chart) {
        chart.setOpacity(0);

        if (chart instanceof Region region) {
            region.setPrefSize(600, 400);
        }

        chartContainer.getChildren().add(chart);
        AnchorPane.setTopAnchor(chart, 0.0);
        AnchorPane.setBottomAnchor(chart, 0.0);
        AnchorPane.setLeftAnchor(chart, 0.0);
        AnchorPane.setRightAnchor(chart, 0.0);

        FadeTransition fade = new FadeTransition(Duration.millis(700), chart);
        fade.setFromValue(0.0);
        fade.setToValue(1.0);
        fade.play();
    }
}