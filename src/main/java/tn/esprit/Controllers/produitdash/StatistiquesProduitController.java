package tn.esprit.Controllers.produitdash;

import javafx.animation.FadeTransition;
import javafx.animation.ScaleTransition;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.chart.*;
import javafx.scene.control.ComboBox;
import javafx.stage.Stage;
import javafx.util.Duration;
import tn.esprit.entities.Produit;
import tn.esprit.services.ProduitService;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class StatistiquesProduitController {

    @FXML private ComboBox<String> statistiqueComboBox;
    @FXML private BarChart<String, Number> statistiqueChart;
    @FXML private PieChart pieChart;
    @FXML private CategoryAxis xAxis;
    @FXML private NumberAxis yAxis;

    private final ProduitService produitService = new ProduitService();

    @FXML
    public void initialize() {
        statistiqueComboBox.getItems().addAll(
                "Nombre de produits par catégorie",
                "Top 5 des produits avec le stock le plus élevé",
                "Produits les plus populaires (stock faible)",
                "Répartition des produits par stock"
        );
    }

    @FXML
    public void afficherStatistiques() {
        String selected = statistiqueComboBox.getValue();
        if (selected == null) return;

        statistiqueChart.setVisible(false);
        pieChart.setVisible(false);
        statistiqueChart.getData().clear();
        pieChart.getData().clear();

        List<Produit> produits = produitService.getAll();

        switch (selected) {
            case "Nombre de produits par catégorie" -> {
                Map<String, Long> countByCat = produits.stream()
                        .collect(Collectors.groupingBy(
                                p -> p.getCategorie() != null ? p.getCategorie().getNom() : "Inconnue",
                                Collectors.counting()
                        ));

                XYChart.Series<String, Number> series = new XYChart.Series<>();
                countByCat.forEach((cat, count) -> series.getData().add(new XYChart.Data<>(cat, count)));
                statistiqueChart.getData().add(series);
                statistiqueChart.setVisible(true);
                animerBarChart(statistiqueChart);
            }

            case "Top 5 des produits avec le stock le plus élevé" -> {
                XYChart.Series<String, Number> series = new XYChart.Series<>();
                produits.stream()
                        .sorted(Comparator.comparingInt(Produit::getStock).reversed())
                        .limit(5)
                        .forEach(p -> series.getData().add(new XYChart.Data<>(p.getNom(), p.getStock())));
                statistiqueChart.getData().add(series);
                statistiqueChart.setVisible(true);
                animerBarChart(statistiqueChart);
            }

            case "Produits les plus populaires (stock faible)" -> {
                Map<String, Long> alertes = produits.stream()
                        .filter(p -> p.getStock() <= 5)
                        .collect(Collectors.groupingBy(Produit::getNom, Collectors.counting()));
                alertes.forEach((nom, count) -> pieChart.getData().add(new PieChart.Data(nom, count)));
                pieChart.setVisible(true);
                animerPieChart(pieChart);
            }

            case "Répartition des produits par stock" -> {
                Map<String, Long> repartition = produits.stream().collect(Collectors.groupingBy(p -> {
                    int stock = p.getStock();
                    if (stock == 0) return "Rupture";
                    else if (stock <= 5) return "Stock Faible";
                    else if (stock <= 20) return "Stock Moyen";
                    else return "Stock Élevé";
                }, Collectors.counting()));

                repartition.forEach((label, count) -> pieChart.getData().add(new PieChart.Data(label, count)));
                pieChart.setVisible(true);
                animerPieChart(pieChart);
            }
        }
    }

    private void animerBarChart(BarChart<String, Number> chart) {
        for (XYChart.Series<String, Number> series : chart.getData()) {
            for (XYChart.Data<String, Number> data : series.getData()) {
                Node node = data.getNode();
                node.setScaleY(0);
                ScaleTransition st = new ScaleTransition(Duration.millis(700), node);
                st.setToY(1);
                st.play();
            }
        }
    }

    private void animerPieChart(PieChart chart) {
        for (PieChart.Data data : chart.getData()) {
            Node node = data.getNode();
            node.setOpacity(0);
            node.setScaleX(0);
            node.setScaleY(0);

            FadeTransition fade = new FadeTransition(Duration.millis(600), node);
            fade.setToValue(1);

            ScaleTransition scale = new ScaleTransition(Duration.millis(600), node);
            scale.setToX(1);
            scale.setToY(1);

            fade.play();
            scale.play();
        }
    }

    @FXML
    public void fermerFenetre() {
        Stage stage = (Stage) statistiqueChart.getScene().getWindow();
        stage.close();
    }
}
