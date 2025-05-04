package tn.esprit.controllers;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import tn.esprit.entities.User;
import tn.esprit.services.ServiceUser;

import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

public class UserListController implements Initializable {

    @FXML private TableView<User> userTable;

    @FXML private TableColumn<User, Integer> colId;
    @FXML private TableColumn<User, String> colName;
    @FXML private TableColumn<User, String> colLastName;
    @FXML private TableColumn<User, String> colEmail;
    @FXML private TableColumn<User, String> colPhone;
    @FXML private TableColumn<User, String> colRole;
    @FXML private TableColumn<User, String> colCarteIdentite;
    @FXML private TableColumn<User, String> colLocation;
    @FXML private TableColumn<User, String> colExperience;
    @FXML private TableColumn<User, Boolean> colActive;

    private final ServiceUser serviceUser = new ServiceUser();

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colName.setCellValueFactory(new PropertyValueFactory<>("name"));
        colLastName.setCellValueFactory(new PropertyValueFactory<>("lastName"));
        colEmail.setCellValueFactory(new PropertyValueFactory<>("email"));
        colPhone.setCellValueFactory(new PropertyValueFactory<>("phoneNumber"));
        colRole.setCellValueFactory(new PropertyValueFactory<>("role"));
        colCarteIdentite.setCellValueFactory(new PropertyValueFactory<>("carteIdentite"));
        colLocation.setCellValueFactory(new PropertyValueFactory<>("location"));
        colExperience.setCellValueFactory(new PropertyValueFactory<>("experience"));
        colActive.setCellValueFactory(new PropertyValueFactory<>("active"));

        List<User> users = serviceUser.getAll();
        userTable.getItems().setAll(users);
    }
}
