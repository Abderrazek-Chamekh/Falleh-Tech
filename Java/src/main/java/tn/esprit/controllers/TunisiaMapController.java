package tn.esprit.controllers;

import javafx.animation.FadeTransition;
import javafx.animation.PauseTransition;
import javafx.animation.ScaleTransition;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Bounds;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.ToggleButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.SVGPath;
import javafx.util.Duration;
import tn.esprit.entities.OffreEmploi;
import tn.esprit.services.ServiceOffreEmploi;

import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TunisiaMapController implements Initializable {

    @FXML
    private Pane mapPane;

    @FXML
    private Pane singleGovernoratePane;

    @FXML
    private Label governorateNameLabel;

    @FXML
    private Label offersCountLabel;

    @FXML
    private ToggleButton toggleMyOffersButton;

    private final Group mapGroup = new Group();
    private final HashMap<String, String> idToNameMap = new HashMap<>();
    private final HashMap<String, SVGPath> governoratePaths = new HashMap<>();

    private final ServiceOffreEmploi serviceOffre = new ServiceOffreEmploi();
    private final Map<String, List<OffreEmploi>> offersByGovernorate = new HashMap<>();

    private final Label tooltipLabel = new Label();

    private boolean showingMyOffers = false;
    private final int loggedInFarmerId = 16; // TODO: Replace with real logged-in farmer ID

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        mapPane.getChildren().add(mapGroup);
        mapGroup.setCache(true);
        mapGroup.setCacheHint(javafx.scene.CacheHint.SCALE);

        tooltipLabel.setStyle("-fx-background-color: white; -fx-text-fill: black; -fx-padding: 5px; -fx-border-color: black; -fx-border-radius: 5; -fx-background-radius: 5;");
        tooltipLabel.setVisible(false);
        mapPane.getChildren().add(tooltipLabel);

        initializeGovernorateMap();
        loadTunisiaMap();
        initializeRealOffers();
        applyHeatMap();

        mapGroup.setScaleX(0.6);
        mapGroup.setScaleY(0.6);
        mapGroup.setLayoutX(-50);
        mapGroup.setLayoutY(-250);

        toggleMyOffersButton.setOnAction(e -> {
            showingMyOffers = toggleMyOffersButton.isSelected();
            reloadMapOffers();
        });
    }

    private void initializeGovernorateMap() {
        idToNameMap.put("TN11", "Tunis");
        idToNameMap.put("TN12", "Ariana");
        idToNameMap.put("TN13", "Ben Arous");
        idToNameMap.put("TN14", "Manubah");
        idToNameMap.put("TN21", "Nabeul");
        idToNameMap.put("TN22", "Zaghouan");
        idToNameMap.put("TN23", "Bizerte");
        idToNameMap.put("TN31", "Béja");
        idToNameMap.put("TN32", "Jendouba");
        idToNameMap.put("TN33", "Le Kef");
        idToNameMap.put("TN34", "Siliana");
        idToNameMap.put("TN41", "Kairouan");
        idToNameMap.put("TN42", "Kassérine");
        idToNameMap.put("TN43", "Sidi Bou Zid");
        idToNameMap.put("TN51", "Sousse");
        idToNameMap.put("TN52", "Monastir");
        idToNameMap.put("TN53", "Mahdia");
        idToNameMap.put("TN61", "Sfax");
        idToNameMap.put("TN71", "Gafsa");
        idToNameMap.put("TN72", "Tozeur");
        idToNameMap.put("TN73", "Kebili");
        idToNameMap.put("TN81", "Gabès");
        idToNameMap.put("TN82", "Médenine");
        idToNameMap.put("TN83", "Tataouine");
    }

    private void initializeRealOffers() {
        List<OffreEmploi> allOffers = serviceOffre.getAll();
        for (OffreEmploi offre : allOffers) {
            String governorate = offre.getLieu();
            offersByGovernorate.computeIfAbsent(governorate, k -> new ArrayList<>()).add(offre);
        }
    }

    private void applyHeatMap() {
        int maxOffers = offersByGovernorate.values().stream().mapToInt(List::size).max().orElse(1);

        for (String governorate : governoratePaths.keySet()) {
            int count = offersByGovernorate.getOrDefault(governorate, Collections.emptyList()).size();
            SVGPath path = governoratePaths.get(governorate);

            int greenLevel = (int) (255 - (count / (double) maxOffers) * 150);
            path.setFill(Color.rgb(100, greenLevel, 100));
        }
    }

    private void loadTunisiaMap() {
        try {
            String svgContent = new String(Files.readAllBytes(Paths.get("src/main/resources/tn.svg")));
            Pattern pattern = Pattern.compile("<path[^>]*d=\"([^\"]*)\"[^>]*id=\"([^\"]*)\"");
            Matcher matcher = pattern.matcher(svgContent);

            while (matcher.find()) {
                String pathData = matcher.group(1);
                String governorateID = matcher.group(2);
                String governorateName = idToNameMap.getOrDefault(governorateID, governorateID);

                SVGPath svgPath = new SVGPath();
                svgPath.setContent(pathData);
                svgPath.setStroke(Color.BLACK);
                svgPath.setStrokeWidth(0.5);

                svgPath.setCache(true);
                svgPath.setCacheHint(javafx.scene.CacheHint.SCALE);
                svgPath.setUserData(governorateName);

                svgPath.setOnMouseEntered(this::onHoverEnter);
                svgPath.setOnMouseExited(this::onHoverExit);
                svgPath.setOnMouseClicked(e -> onGovernorateClick(governorateName));

                mapGroup.getChildren().add(svgPath);
                governoratePaths.put(governorateName, svgPath);
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void reloadMapOffers() {
        mapGroup.getChildren().clear();

        // Draw the full Tunisia map
        for (SVGPath path : governoratePaths.values()) {
            path.setStroke(Color.BLACK);
            path.setStrokeWidth(0.5);
            path.setFill(Color.LIGHTGREEN);
            mapGroup.getChildren().add(path);
        }

        List<OffreEmploi> allOffers = serviceOffre.getAll();
        Random random = new Random();

        for (OffreEmploi offer : allOffers) {
            if (showingMyOffers && offer.getIdEmployeur().getId() != loggedInFarmerId) {
                continue;
            }

            String governorate = offer.getLieu();
            SVGPath path = governoratePaths.get(governorate);
            if (path == null) continue;

            Bounds bounds = path.getBoundsInParent();
            double width = bounds.getWidth();
            double height = bounds.getHeight();

            double randomX = bounds.getMinX() + random.nextDouble() * width;
            double randomY = bounds.getMinY() + random.nextDouble() * height;

            Circle offerPoint = new Circle(4, showingMyOffers ? Color.BLUE : Color.YELLOW);
            offerPoint.setLayoutX(randomX);
            offerPoint.setLayoutY(randomY);

            mapGroup.getChildren().add(offerPoint);
        }
    }

    private void onHoverEnter(MouseEvent event) {
        SVGPath path = (SVGPath) event.getSource();
        ScaleTransition st = new ScaleTransition(Duration.millis(150), path);
        st.setToX(1.1);
        st.setToY(1.1);
        st.play();

        String governorate = (String) path.getUserData();
        int offers = offersByGovernorate.getOrDefault(governorate, Collections.emptyList()).size();
        tooltipLabel.setText(governorate + " - " + offers + " offres");

        tooltipLabel.setVisible(true);
        moveTooltip(event);

        path.setOnMouseMoved(this::moveTooltip);
    }

    private void moveTooltip(MouseEvent event) {
        tooltipLabel.setLayoutX(event.getX() + 20);
        tooltipLabel.setLayoutY(event.getY() + 20);
    }

    private void onHoverExit(MouseEvent event) {
        SVGPath path = (SVGPath) event.getSource();
        ScaleTransition st = new ScaleTransition(Duration.millis(150), path);
        st.setToX(1.0);
        st.setToY(1.0);
        st.play();

        tooltipLabel.setVisible(false);
    }

    private void onGovernorateClick(String governorateName) {
        singleGovernoratePane.getChildren().clear();

        SVGPath original = governoratePaths.get(governorateName);
        if (original == null) return;

        SVGPath clone = new SVGPath();
        clone.setContent(original.getContent());
        clone.setFill(Color.DARKGREEN);
        clone.setStroke(Color.BLACK);
        clone.setStrokeWidth(0.5);

        Group shapeGroup = new Group(clone);

        List<OffreEmploi> offres = offersByGovernorate.getOrDefault(governorateName, Collections.emptyList());
        Bounds bounds = clone.getBoundsInLocal();

        double width = bounds.getWidth();
        double height = bounds.getHeight();
        Random random = new Random();

        for (OffreEmploi offer : offres) {
            Circle point = new Circle(3, Color.YELLOW);

            boolean placed = false;
            while (!placed) {
                double randomX = bounds.getMinX() + random.nextDouble() * width;
                double randomY = bounds.getMinY() + random.nextDouble() * height;

                if (clone.contains(randomX, randomY)) {
                    point.setCenterX(randomX);
                    point.setCenterY(randomY);
                    placed = true;
                }
            }

            point.setUserData(offer);

            point.setOnMouseClicked(e -> showOfferPopupInsideState((OffreEmploi) point.getUserData(), point.getCenterX(), point.getCenterY(), shapeGroup));

            shapeGroup.getChildren().add(point);
        }

        singleGovernoratePane.getChildren().add(shapeGroup);

        Platform.runLater(() -> {
            Bounds cloneBounds = clone.getBoundsInLocal();
            shapeGroup.setTranslateX(-cloneBounds.getMinX());
            shapeGroup.setTranslateY(-cloneBounds.getMinY());

            double cloneWidth = cloneBounds.getWidth();
            double cloneHeight = cloneBounds.getHeight();

            double paneWidth = singleGovernoratePane.getWidth();
            double paneHeight = singleGovernoratePane.getHeight();

            double scaleX = (paneWidth * 0.7) / cloneWidth;
            double scaleY = (paneHeight * 0.7) / cloneHeight;
            double finalScale = Math.min(scaleX, scaleY);

            shapeGroup.setScaleX(finalScale);
            shapeGroup.setScaleY(finalScale);

            double offsetX = (paneWidth - cloneWidth * finalScale) / 2;
            double offsetY = (paneHeight - cloneHeight * finalScale) / 2;

            shapeGroup.setLayoutX(offsetX);
            shapeGroup.setLayoutY(offsetY);

            ScaleTransition scaleTransition = new ScaleTransition(Duration.millis(400), shapeGroup);
            scaleTransition.setFromX(0);
            scaleTransition.setFromY(0);
            scaleTransition.setToX(finalScale);
            scaleTransition.setToY(finalScale);

            FadeTransition fadeTransition = new FadeTransition(Duration.millis(400), shapeGroup);
            fadeTransition.setFromValue(0);
            fadeTransition.setToValue(1);

            scaleTransition.play();
            fadeTransition.play();
        });

        governorateNameLabel.setText("📍 " + governorateName);
        offersCountLabel.setText("💼 Nombre d'offres: " + offres.size());
    }

    private void showOfferPopupInsideState(OffreEmploi offer, double localX, double localY, Group parentGroup) {
        parentGroup.getChildren().removeIf(node -> node.getId() != null && node.getId().equals("offerPopup"));

        VBox popup = new VBox(3);
        popup.setId("offerPopup");
        popup.setStyle("-fx-background-color: white; -fx-border-color: black; -fx-padding: 5px; -fx-background-radius: 6px; -fx-border-radius: 6px; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.2), 6, 0, 0, 0);");

        Label title = new Label("📋 " + offer.getTitre());
        title.setStyle("-fx-font-weight: bold; -fx-text-fill: #2c3e50; -fx-font-size: 11px;");

        Label salary = new Label("💰 " + offer.getSalaire() + " DT");
        salary.setStyle("-fx-text-fill: #27ae60; -fx-font-size: 10px;");

        Label startDate = new Label("📅 " + offer.getStartDate());
        startDate.setStyle("-fx-text-fill: #2980b9; -fx-font-size: 10px;");

        popup.getChildren().addAll(title, salary, startDate);

        popup.setLayoutX(localX + 10);
        popup.setLayoutY(localY + 10);

        parentGroup.getChildren().add(popup);

        PauseTransition delay = new PauseTransition(Duration.seconds(3));
        delay.setOnFinished(e -> parentGroup.getChildren().remove(popup));
        delay.play();
    }
}
