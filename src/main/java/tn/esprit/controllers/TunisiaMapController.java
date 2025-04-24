package tn.esprit.controllers;

import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.input.MouseEvent;
import javafx.scene.shape.Circle;

public class TunisiaMapController {

    @FXML private Circle dynamicPoint;
    @FXML private Label percentageLabel;
    @FXML private ComboBox<String> skillsDropdown;
    @FXML private ComboBox<String> experienceDropdown;

    @FXML
    public void initialize() {
        // Initialize dropdowns with example values
        skillsDropdown.getItems().addAll("Plomberie", "Récolte", "Conduite", "Élevage");
        experienceDropdown.getItems().addAll("Débutant", "Intermédiaire", "Expert");

        skillsDropdown.setOnAction(e -> updatePercentage());
        experienceDropdown.setOnAction(e -> updatePercentage());
    }

    @FXML
    private void onCirclePressed(MouseEvent event) {
        Circle circle = (Circle) event.getSource();
        circle.setUserData(new double[]{
                event.getSceneX(), event.getSceneY(),
                circle.getLayoutX(), circle.getLayoutY()
        });
    }

    @FXML
    private void onCircleDragged(MouseEvent event) {
        Circle circle = (Circle) event.getSource();
        double[] data = (double[]) circle.getUserData();
        double deltaX = event.getSceneX() - data[0];
        double deltaY = event.getSceneY() - data[1];
        circle.setLayoutX(data[2] + deltaX);
        circle.setLayoutY(data[3] + deltaY);

        updatePercentage();
    }

    private void updatePercentage() {
        // Dummy formula based on layoutX and layoutY just for illustration
        double x = dynamicPoint.getLayoutX();
        double y = dynamicPoint.getLayoutY();

        String skill = skillsDropdown.getValue();
        String exp = experienceDropdown.getValue();

        int skillFactor = skill == null ? 1 : skill.length();
        int expFactor = exp == null ? 1 : exp.length();

        int result = (int) ((x + y) % 100 * skillFactor * expFactor % 100);

        percentageLabel.setText(result + "%");
    }

}
