package tn.esprit.Controllers.User.Admin;

import javafx.animation.*;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.chart.*;
import javafx.scene.control.Label;
import javafx.scene.effect.DropShadow;
import javafx.scene.effect.Glow;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.util.Duration;
import tn.esprit.services.UserService;

import java.net.URL;
import java.sql.SQLException;
import java.time.Month;
import java.util.HashMap;
import java.util.Map;
import java.util.ResourceBundle;

public class UserStatsController implements Initializable {

    // Charts
    @FXML private PieChart rolesPieChart;
    @FXML private PieChart statusPieChart;

    // Stats card labels
    @FXML private Label totalUsersLabel;
    @FXML private Label activeUsersLabel;
    @FXML private Label rolesCountLabel;
    @FXML private Label activeUsersNote;
    @FXML private Label rolesNote;
    @FXML private Label rolesChartNote;
    @FXML private Label statusChartNote;

    // Stats cards
    @FXML private VBox totalUsersCard;
    @FXML private VBox activeUsersCard;
    @FXML private VBox rolesCard;

    private final UserService userService = new UserService();

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        initializeAnimations();

        try {
            loadStatsCards();
            loadRolesDistribution();
            loadStatusDistribution();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void initializeAnimations() {
        // Add animation classes to elements
        totalUsersCard.getStyleClass().add("zoom-in");
        activeUsersCard.getStyleClass().add("zoom-in");
        rolesCard.getStyleClass().add("zoom-in");
        rolesPieChart.getStyleClass().add("fade-in");
        statusPieChart.getStyleClass().add("fade-in");

        // Create staggered animation
        SequentialTransition seqTransition = new SequentialTransition(
                createCardAnimation(totalUsersCard),
                createCardAnimation(activeUsersCard),
                createCardAnimation(rolesCard),
                createChartAnimation(rolesPieChart),
                createChartAnimation(statusPieChart)
        );

        // Play animation after scene is shown
        Platform.runLater(seqTransition::play);
    }

    private Animation createCardAnimation(VBox card) {
        ScaleTransition scale = new ScaleTransition(Duration.millis(400), card);
        scale.setFromX(0.9);
        scale.setFromY(0.9);
        scale.setToX(1);
        scale.setToY(1);

        FadeTransition fade = new FadeTransition(Duration.millis(400), card);
        fade.setFromValue(0);
        fade.setToValue(1);

        ParallelTransition parallel = new ParallelTransition(scale, fade);
        parallel.setOnFinished(e -> card.getStyleClass().add("show"));

        return parallel;
    }

    private Animation createChartAnimation(Node chart) {
        if (chart.getStyleClass().contains("fade-in")) {
            FadeTransition fade = new FadeTransition(Duration.millis(500), chart);
            fade.setFromValue(0);
            fade.setToValue(1);
            fade.setOnFinished(e -> chart.getStyleClass().add("show"));
            return fade;
        } else {
            TranslateTransition translate = new TranslateTransition(Duration.millis(500), chart);
            translate.setFromY(20);
            translate.setToY(0);

            FadeTransition fade = new FadeTransition(Duration.millis(500), chart);
            fade.setFromValue(0);
            fade.setToValue(1);

            ParallelTransition parallel = new ParallelTransition(translate, fade);
            parallel.setOnFinished(e -> chart.getStyleClass().add("show"));
            return parallel;
        }
    }

    private void loadStatsCards() throws SQLException {
        int totalUsers = userService.getTotalUserCount();
        int activeUsers = userService.getActiveUserCount();
        int roleCount = userService.getRoleCount();

        totalUsersLabel.setText(String.valueOf(totalUsers));
        activeUsersLabel.setText(String.valueOf(activeUsers));
        rolesCountLabel.setText(String.valueOf(roleCount));

        activeUsersNote.setText(String.format("%.1f%% of total users", (activeUsers * 100.0 / totalUsers)));
        rolesNote.setText("Across all user roles");
    }

    private void loadRolesDistribution() throws SQLException {
        Map<String, Integer> roleCounts = userService.getUserCountByRole();

        rolesPieChart.setData(FXCollections.observableArrayList(
                roleCounts.entrySet().stream()
                        .map(entry -> new PieChart.Data(
                                entry.getKey() + " (" + entry.getValue() + ")",
                                entry.getValue()
                        ))
                        .toList()
        ));

        rolesChartNote.setText("Showing distribution across " + roleCounts.size() + " roles");
        animatePieChart(rolesPieChart);
    }

    private void loadStatusDistribution() throws SQLException {
        Map<Boolean, Integer> statusCounts = userService.getUserCountByStatus();

        statusPieChart.setData(FXCollections.observableArrayList(
                new PieChart.Data("Active (" + statusCounts.get(true) + ")", statusCounts.get(true)),
                new PieChart.Data("Inactive (" + statusCounts.get(false) + ")", statusCounts.get(false))
        ));

        statusChartNote.setText(String.format("%.1f%% active accounts",
                (statusCounts.get(true) * 100.0 / (statusCounts.get(true) + statusCounts.get(false)))));
        animatePieChart(statusPieChart);
    }

    private void animatePieChart(PieChart pieChart) {
        for (final PieChart.Data data : pieChart.getData()) {
            data.getNode().setOnMouseEntered(e -> {
                data.getNode().setEffect(new Glow(0.3));
                data.getNode().setScaleX(1.05);
                data.getNode().setScaleY(1.05);
            });

            data.getNode().setOnMouseExited(e -> {
                data.getNode().setEffect(null);
                data.getNode().setScaleX(1.0);
                data.getNode().setScaleY(1.0);
            });
        }
    }

    private void animateBarChart(BarChart<String, Number> barChart) {
        for (XYChart.Series<String, Number> series : barChart.getData()) {
            for (XYChart.Data<String, Number> data : series.getData()) {
                Node node = data.getNode();
                node.setOnMouseEntered(e -> {
                    node.setEffect(new DropShadow(10, Color.web("#4f46e5")));
                    node.setScaleX(1.05);
                    node.setScaleY(1.05);
                });
                node.setOnMouseExited(e -> {
                    node.setEffect(null);
                    node.setScaleX(1.0);
                    node.setScaleY(1.0);
                });
            }
        }
    }

    @FXML
    private void handleClose() {
        Stage stage = (Stage) rolesPieChart.getScene().getWindow();
        stage.close();
    }
}