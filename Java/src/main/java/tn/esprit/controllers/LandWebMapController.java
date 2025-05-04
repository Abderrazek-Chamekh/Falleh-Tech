package tn.esprit.controllers;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;

import java.net.URL;
import java.util.ResourceBundle;

public class LandWebMapController implements Initializable {

    @FXML
    private WebView mapWebView;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        System.out.println("🔵 Initializing LandWebMapController...");

        try {
            WebEngine engine = mapWebView.getEngine();
            URL mapUrl = getClass().getResource("/html/map.html");

            if (mapUrl == null) {
                System.err.println("❌ map.html not found in resources/web/");
                return;
            }

            String localMapUrl = mapUrl.toExternalForm();
            System.out.println("🌍 Loading map from: " + localMapUrl);

            engine.load(localMapUrl);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
