package tn.esprit.controllers;

import javafx.animation.FadeTransition;
import javafx.animation.ScaleTransition;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Bounds;
import javafx.scene.Group;
import javafx.scene.control.Label;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.SVGPath;
import javafx.util.Duration;

import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.ResourceBundle;
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

    private Group mapGroup = new Group();
    private HashMap<String, String> idToNameMap = new HashMap<>();
    private HashMap<String, SVGPath> governoratePaths = new HashMap<>();
    private Map<String, Integer> offersPerGovernorate = new HashMap<>(); // 🛠 REAL dynamic data

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        mapPane.getChildren().add(mapGroup);
        mapGroup.setCache(true);
        mapGroup.setCacheHint(javafx.scene.CacheHint.SCALE);

        initializeGovernorateMap();
        loadTunisiaMap();
        initializeOffers(); // 👈 ADD this

        mapGroup.setScaleX(0.6);
        mapGroup.setScaleY(0.6);
        mapGroup.setLayoutX(-50);
        mapGroup.setLayoutY(-250);
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

    private void initializeOffers() {
        // 💼 Simulated offers (later you can connect to your DB)
        offersPerGovernorate.put("Tunis", 12);
        offersPerGovernorate.put("Ariana", 8);
        offersPerGovernorate.put("Ben Arous", 5);
        offersPerGovernorate.put("Manubah", 6);
        offersPerGovernorate.put("Nabeul", 9);
        offersPerGovernorate.put("Zaghouan", 4);
        offersPerGovernorate.put("Bizerte", 7);
        offersPerGovernorate.put("Béja", 3);
        offersPerGovernorate.put("Jendouba", 2);
        offersPerGovernorate.put("Le Kef", 5);
        offersPerGovernorate.put("Siliana", 3);
        offersPerGovernorate.put("Kairouan", 6);
        offersPerGovernorate.put("Kassérine", 4);
        offersPerGovernorate.put("Sidi Bou Zid", 4);
        offersPerGovernorate.put("Sousse", 10);
        offersPerGovernorate.put("Monastir", 7);
        offersPerGovernorate.put("Mahdia", 5);
        offersPerGovernorate.put("Sfax", 11);
        offersPerGovernorate.put("Gafsa", 3);
        offersPerGovernorate.put("Tozeur", 2);
        offersPerGovernorate.put("Kebili", 1);
        offersPerGovernorate.put("Gabès", 4);
        offersPerGovernorate.put("Médenine", 6);
        offersPerGovernorate.put("Tataouine", 2);
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
                svgPath.setFill(Color.LIGHTGREEN);
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

    private void onHoverEnter(MouseEvent event) {
        SVGPath path = (SVGPath) event.getSource();
        ScaleTransition st = new ScaleTransition(Duration.millis(150), path);
        st.setToX(1.1);
        st.setToY(1.1);
        st.play();
        path.setFill(Color.DARKGREEN);
    }

    private void onHoverExit(MouseEvent event) {
        SVGPath path = (SVGPath) event.getSource();
        ScaleTransition st = new ScaleTransition(Duration.millis(150), path);
        st.setToX(1.0);
        st.setToY(1.0);
        st.play();
        path.setFill(Color.LIGHTGREEN);
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

        Group group = new Group(clone);
        singleGovernoratePane.getChildren().add(group);

        Platform.runLater(() -> {
            Bounds bounds = clone.getBoundsInLocal();
            clone.setTranslateX(-bounds.getMinX());
            clone.setTranslateY(-bounds.getMinY());

            double width = bounds.getWidth();
            double height = bounds.getHeight();

            double paneWidth = singleGovernoratePane.getWidth();
            double paneHeight = singleGovernoratePane.getHeight();

            double scaleX = (paneWidth * 0.7) / width;
            double scaleY = (paneHeight * 0.7) / height;
            double finalScale = Math.min(scaleX, scaleY);

            group.setScaleX(finalScale);
            group.setScaleY(finalScale);

            double offsetX = (paneWidth - width * finalScale) / 2;
            double offsetY = (paneHeight - height * finalScale) / 2;

            group.setLayoutX(offsetX);
            group.setLayoutY(offsetY);

            // Animate: FADE + ZOOM together
            ScaleTransition scaleTransition = new ScaleTransition(Duration.millis(400), group);
            scaleTransition.setFromX(0);
            scaleTransition.setFromY(0);
            scaleTransition.setToX(finalScale);
            scaleTransition.setToY(finalScale);

            FadeTransition fadeTransition = new FadeTransition(Duration.millis(400), group);
            fadeTransition.setFromValue(0);
            fadeTransition.setToValue(1);

            scaleTransition.play();
            fadeTransition.play();
        });

        // Update information
        governorateNameLabel.setText("📍 " + governorateName);
        int offers = offersPerGovernorate.getOrDefault(governorateName, 0);
        offersCountLabel.setText("💼 Nombre d'offres: " + offers);
    }
}
