package tn.esprit.Controllers.Blog.Post;

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
import tn.esprit.services.ServicePost;

import java.net.URL;
import java.sql.SQLException;
import java.time.Month;
import java.util.HashMap;
import java.util.Map;
import java.util.ResourceBundle;

public class PostStatsController implements Initializable {

    @FXML private PieChart categoryPieChart;
    @FXML private BarChart<String, Number> monthlyBarChart;
    @FXML private CategoryAxis monthAxis;
    @FXML private NumberAxis postCountAxis;

    // Stats card labels
    @FXML private Label totalPostsLabel;
    @FXML private Label categoriesCountLabel;
    @FXML private Label activeMonthLabel;
    @FXML private Label pieChartNote;
    @FXML private Label barChartNote;

    // Stats cards
    @FXML private VBox totalPostsCard;
    @FXML private VBox categoriesCard;
    @FXML private VBox activeMonthCard;

    private final ServicePost postService = new ServicePost();

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        initializeAnimations();

        try {
            loadStatsCards();
            loadCategoryStats();
            loadMonthlyStats();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void initializeAnimations() {
        // Add animation classes to elements
        totalPostsCard.getStyleClass().add("slide-up");
        categoriesCard.getStyleClass().add("slide-up");
        activeMonthCard.getStyleClass().add("slide-up");
        categoryPieChart.getStyleClass().add("fade-in");
        monthlyBarChart.getStyleClass().add("fade-in");

        // Create sequential animation
        SequentialTransition seqTransition = new SequentialTransition(
                createCardAnimation(totalPostsCard),
                createCardAnimation(categoriesCard),
                createCardAnimation(activeMonthCard),
                createChartAnimation(categoryPieChart),
                createChartAnimation(monthlyBarChart)
        );

        // Play animation after scene is shown
        Platform.runLater(seqTransition::play);
    }

    private Animation createCardAnimation(VBox card) {
        FadeTransition fade = new FadeTransition(Duration.millis(300), card);
        fade.setFromValue(0);
        fade.setToValue(1);

        TranslateTransition translate = new TranslateTransition(Duration.millis(300), card);
        translate.setFromY(20);
        translate.setToY(0);

        ParallelTransition parallel = new ParallelTransition(fade, translate);
        parallel.setOnFinished(e -> card.getStyleClass().add("show"));

        return parallel;
    }

    private Animation createChartAnimation(Node chart) {
        FadeTransition fade = new FadeTransition(Duration.millis(500), chart);
        fade.setFromValue(0);
        fade.setToValue(1);
        fade.setOnFinished(e -> chart.getStyleClass().add("show"));
        return fade;
    }

    private void loadStatsCards() throws SQLException {
        int totalPosts = postService.getTotalPostCount();
        int categoryCount = postService.getCategoryCount();
        String activeMonth = postService.getMostActiveMonth();

        totalPostsLabel.setText(String.valueOf(totalPosts));
        categoriesCountLabel.setText(String.valueOf(categoryCount));
        activeMonthLabel.setText(activeMonth);

        pieChartNote.setText("Showing distribution across " + categoryCount + " categories");
        barChartNote.setText("Analyzing post activity over 12 months");
    }

    private void loadCategoryStats() throws SQLException {
        Map<String, Integer> categoryCounts = postService.getPostCountByCategory();

        categoryPieChart.setData(FXCollections.observableArrayList(
                categoryCounts.entrySet().stream()
                        .map(entry -> new PieChart.Data(
                                entry.getKey() + " (" + entry.getValue() + ")",
                                entry.getValue()
                        ))
                        .toList()
        ));

        // Add animation to pie slices
        animatePieChart();
    }

    private void animatePieChart() {
        for (final PieChart.Data data : categoryPieChart.getData()) {
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

    private void loadMonthlyStats() throws SQLException {
        Map<Month, Integer> monthlyCounts = postService.getPostCountByMonth();

        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("Posts");

        monthlyCounts.forEach((month, count) ->
                series.getData().add(new XYChart.Data<>(month.toString(), count))
        );

        monthlyBarChart.getData().add(series);

        // Add animation to bars
        animateBarChart();
    }

    private void animateBarChart() {
        for (XYChart.Series<String, Number> series : monthlyBarChart.getData()) {
            for (XYChart.Data<String, Number> data : series.getData()) {
                Node node = data.getNode();
                node.setOnMouseEntered(e -> {
                    node.setEffect(new DropShadow(10, Color.BLACK));
                    node.setScaleX(1.1);
                    node.setScaleY(1.1);
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
        Stage stage = (Stage) categoryPieChart.getScene().getWindow();
        stage.close();
    }
}