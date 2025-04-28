package tn.esprit.Controllers.E_Commerce;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.PieChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.ComboBox;
import tn.esprit.entities.Commande;
import tn.esprit.services.ServiceCommande;

import java.time.LocalDateTime;
import java.time.Month;
import java.time.format.TextStyle;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

public class StatsController {

    @FXML private BarChart<String, Number> monthlyChart;
    @FXML private PieChart statusChart;
    @FXML private PieChart paymentChart;
    @FXML private ComboBox<Integer> yearComboBox;

    private ServiceCommande commandeService = new ServiceCommande();

    @FXML
    public void initialize() {
        loadMonthlyStats();
        loadStatusStats();
        loadPaymentStats();
        setupYearFilter();
    }

    private void loadMonthlyStats() {
        List<Commande> allCommandes = commandeService.getAll();

        // Group by month-year and count
        Map<String, Long> monthlyCounts = allCommandes.stream()
                .collect(Collectors.groupingBy(
                        cmd -> getMonthYearString(cmd.getDateCreation()),
                        Collectors.counting()
                ));

        XYChart.Series<String, Number> series = new XYChart.Series<>();

        monthlyCounts.forEach((monthYear, count) -> {
            series.getData().add(new XYChart.Data<>(monthYear, count));
        });

        monthlyChart.getData().add(series);
    }

    private void loadStatusStats() {
        // Same as before, no changes needed
        List<Commande> allCommandes = commandeService.getAll();

        Map<String, Long> statusCounts = allCommandes.stream()
                .collect(Collectors.groupingBy(
                        Commande::getStatus,
                        Collectors.counting()
                ));

        statusChart.getData().addAll(
                statusCounts.entrySet().stream()
                        .map(entry -> new PieChart.Data(entry.getKey(), entry.getValue()))
                        .collect(Collectors.toList())
        );
    }

    private void loadPaymentStats() {
        // Same as before, no changes needed
        List<Commande> allCommandes = commandeService.getAll();

        Map<String, Long> paymentCounts = allCommandes.stream()
                .collect(Collectors.groupingBy(
                        Commande::getModePaiement,
                        Collectors.counting()
                ));

        paymentChart.getData().addAll(
                paymentCounts.entrySet().stream()
                        .map(entry -> new PieChart.Data(entry.getKey(), entry.getValue()))
                        .collect(Collectors.toList())
        );
    }

    private void setupYearFilter() {
        // Extract distinct years from commandes
        List<Integer> years = commandeService.getAll().stream()
                .map(cmd -> cmd.getDateCreation().getYear())
                .distinct()
                .sorted()
                .collect(Collectors.toList());

        yearComboBox.setItems(FXCollections.observableArrayList(years));

        // Select current year by default
        yearComboBox.getSelectionModel().selectLast();

        // Update chart when year changes
        yearComboBox.valueProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                updateMonthlyChart(newVal);
            }
        });
    }

    private void updateMonthlyChart(int year) {
        monthlyChart.getData().clear();

        List<Commande> filteredCommandes = commandeService.getAll().stream()
                .filter(cmd -> cmd.getDateCreation().getYear() == year)
                .collect(Collectors.toList());

        // Group by month and count
        Map<Month, Long> monthlyCounts = filteredCommandes.stream()
                .collect(Collectors.groupingBy(
                        cmd -> cmd.getDateCreation().getMonth(),
                        Collectors.counting()
                ));

        XYChart.Series<String, Number> series = new XYChart.Series<>();

        // Add all months (even those with 0 counts)
        for (Month month : Month.values()) {
            String monthName = month.getDisplayName(TextStyle.FULL, Locale.FRENCH);
            long count = monthlyCounts.getOrDefault(month, 0L);
            series.getData().add(new XYChart.Data<>(monthName, count));
        }

        monthlyChart.getData().add(series);
    }

    private String getMonthYearString(LocalDateTime date) {
        return date.getMonth().getDisplayName(TextStyle.FULL, Locale.FRENCH) + " " + date.getYear();
    }
}