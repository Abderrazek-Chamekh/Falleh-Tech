/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package controllers;

import java.net.URL;
import java.util.ResourceBundle;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.fxml.FXMLLoader;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;

import java.io.IOException;

/**
 *
 * @author Miss Anee
 */
public class UIController implements Initializable {
    
    @FXML
    private Label label;
    @FXML
    private AnchorPane rootPane;

    @FXML
    private void handleButtonAction(ActionEvent event) {
        System.out.println("You clicked me!");
        label.setText("Hello World!");
    }
    
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // TODO
    }
    @FXML
    private void goToOffres() {
        try {
            AnchorPane pane = FXMLLoader.load(getClass().getResource("../main/resources/fxml/offres.fxml"));
            rootPane.getChildren().setAll(pane);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    @FXML
    private void goToCandidatures() {
        try {
            AnchorPane pane = FXMLLoader.load(getClass().getResource("../main/resources/fxml/Candidature.fxml"));
            rootPane.getChildren().setAll(pane);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    @FXML
    public void goToProfil(MouseEvent mouseEvent) {
        try {
            Parent profileRoot = FXMLLoader.load(getClass().getResource("../main/resources/fxml/profil.fxml"));
            Scene scene = new Scene(profileRoot);
            Stage stage = (Stage) ((Node) mouseEvent.getSource()).getScene().getWindow();
            stage.setScene(scene);
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
