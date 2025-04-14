package tn.esprit.fx;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Pane;

import java.io.File;
import java.net.URL;
import java.util.ResourceBundle;
import java.util.prefs.Preferences;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import javafx.fxml.FXML;
import javafx.scene.image.ImageView;

import java.io.IOException;

public class AccueilController implements Initializable {

    private final Preferences prefs = Preferences.userNodeForPackage(getClass());

    @FXML private ImageView carottesImage;
    @FXML private ImageView laitueImage;
    @FXML private ImageView tomatesImage;
    @FXML private ImageView pommesImage;
    @FXML private ImageView stockImage;

    @FXML private Pane statsPane;
    @FXML private Pane blogPane;
    @FXML private Pane offresPane;
    @FXML private Pane productsPane;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        loadImages();
        setupDraggableWidgets();
    }

    private void loadImages() {
        carottesImage.setImage(loadImage("photos/carottes.jpg"));
        laitueImage.setImage(loadImage("photos/laitue.jpg"));
        tomatesImage.setImage(loadImage("photos/tomates.jpg"));
        pommesImage.setImage(loadImage("photos/pommes.jpg"));
        stockImage.setImage(loadImage("photos/stock_pie.png"));
    }

    private void setupDraggableWidgets() {
        setupWidget(offresPane, "offresPane", 50, 250);
        setupWidget(blogPane, "blogPane", 500, 250);
        setupWidget(statsPane, "statsPane", 275, 450);
        setupWidget(productsPane, "productsPane", 50, 50);
    }

    private void setupWidget(Pane pane, String key, double defaultX, double defaultY) {
        if (pane != null) {
            restoreWidgetPosition(pane, key, defaultX, defaultY);
            makeDraggable(pane, key);
        }
    }

    private Image loadImage(String path) {
        File file = new File(path);
        return file.exists() ? new Image(file.toURI().toString()) : null;
    }

    private void makeDraggable(Node node, String key) {
        final double[] offsetX = new double[1];
        final double[] offsetY = new double[1];

        node.setOnMousePressed(event -> {
            offsetX[0] = event.getSceneX() - node.getLayoutX();
            offsetY[0] = event.getSceneY() - node.getLayoutY();
        });

        node.setOnMouseDragged(event -> {
            node.setLayoutX(event.getSceneX() - offsetX[0]);
            node.setLayoutY(event.getSceneY() - offsetY[0]);
            saveWidgetPosition(node, key);
        });
    }

    private void saveWidgetPosition(Node node, String key) {
        prefs.putDouble(key + "X", node.getLayoutX());
        prefs.putDouble(key + "Y", node.getLayoutY());
    }

    private void restoreWidgetPosition(Node node, String key, double defaultX, double defaultY) {
        double x = prefs.getDouble(key + "X", defaultX);
        double y = prefs.getDouble(key + "Y", defaultY);
        node.setLayoutX(x);
        node.setLayoutY(y);
    }

    @FXML private void goToOffres() {}
    @FXML private void goToBlog() {}
    @FXML
    private void goToMesOffres() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/farmer_front/MesOffresView.fxml"));
            Parent view = loader.load();

            // Replace current scene
            Stage stage = (Stage) carottesImage.getScene().getWindow(); // Any node in the scene will work
            Scene scene = new Scene(view);
            scene.getStylesheets().add(getClass().getResource("/styles/main.css").toExternalForm());
            stage.setScene(scene);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
