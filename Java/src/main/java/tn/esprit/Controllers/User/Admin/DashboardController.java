package tn.esprit.Controllers.User.Admin;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import tn.esprit.entities.User;
import tn.esprit.services.UserService;

import java.io.IOException;
import java.net.URL;
import java.sql.SQLException;
import java.util.List;
import java.util.ResourceBundle;

public class DashboardController implements Initializable {

    @FXML private BorderPane border;
    @FXML private TableView<User> usersTable;
    @FXML private TableColumn<User, Integer> numberColumn;
    @FXML private TableColumn<User, String> nameColumn;
    @FXML private TableColumn<User, String> emailColumn;
    @FXML private TableColumn<User, String> roleColumn;
    @FXML private TableColumn<User, String> statusColumn;
    @FXML private TableColumn<User, Void> actionsColumn;
    @FXML private TextField searchField;
    @FXML private ComboBox<String> filterCombo;
    @FXML private Pagination pagination;
    private ObservableList<User> filteredUsers = FXCollections.observableArrayList();
    private static final int ROWS_PER_PAGE = 10;

    private final UserService userService = new UserService();
    private final ObservableList<User> allUsers = FXCollections.observableArrayList();

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        setupTableColumns();
        setupRoleFilterCombo();
        loadUsers();
        setupSearchFilter();
        setupRoleFilter();
        setupPagination();
    }
    private void setupPagination() {
        pagination.currentPageIndexProperty().addListener((obs, oldIndex, newIndex) -> {
            int fromIndex = newIndex.intValue() * ROWS_PER_PAGE;
            int toIndex = Math.min(fromIndex + ROWS_PER_PAGE, filteredUsers.size());

            if (filteredUsers.size() > 0) {
                usersTable.setItems(FXCollections.observableArrayList(filteredUsers.subList(fromIndex, toIndex)));
            } else {
                usersTable.setItems(FXCollections.observableArrayList());
            }
        });
    }

    private void updatePagination() {
        int pageCount = (filteredUsers.size() + ROWS_PER_PAGE - 1) / ROWS_PER_PAGE;
        pagination.setPageCount(pageCount);
        pagination.setCurrentPageIndex(0);

        // Show first page
        if (filteredUsers.size() > 0) {
            usersTable.setItems(FXCollections.observableArrayList(filteredUsers.subList(0, Math.min(ROWS_PER_PAGE, filteredUsers.size()))));
        } else {
            usersTable.setItems(FXCollections.observableArrayList());
        }
    }

    private void setupTableColumns() {
        // Number column showing sequential numbers
        numberColumn.setCellFactory(column -> new TableCell<User, Integer>() {
            @Override
            protected void updateItem(Integer item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setText(null);
                    setGraphic(null);
                } else {
                    int index = getIndex() + 1 + (pagination.getCurrentPageIndex() * ROWS_PER_PAGE);
                    setText(String.valueOf(index));
                    setStyle("-fx-alignment: CENTER; -fx-font-weight: bold;");
                }
            }
        });

        nameColumn.setCellValueFactory(new PropertyValueFactory<>("fullName"));
        emailColumn.setCellValueFactory(new PropertyValueFactory<>("email"));

        // Role column with formatted display
        roleColumn.setCellValueFactory(new PropertyValueFactory<>("role"));
        roleColumn.setCellFactory(column -> new TableCell<User, String>() {
            @Override
            protected void updateItem(String role, boolean empty) {
                super.updateItem(role, empty);
                if (empty || role == null) {
                    setText(null);
                    setStyle("");
                } else {
                    // Format role display
                    String displayRole = role;
                    if (role.equalsIgnoreCase("ROLE_ADMIN"))
                        displayRole = "Admin";
                    setText(displayRole);
                }
            }
        });

        // Status column with badges
        statusColumn.setCellValueFactory(new PropertyValueFactory<>("status"));
        statusColumn.setCellFactory(column -> new TableCell<User, String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setStyle("");
                } else {
                    User user = getTableView().getItems().get(getIndex());
                    setText(user.isActive() ? "Active" : "Inactive");
                    /*setStyle(user.isActive()
                            ? "-fx-background-color: #2E8B57; -fx-text-fill: white; -fx-background-radius: 10px; -fx-padding: 3px 10px;"
                            : "-fx-background-color: #E53935; -fx-text-fill: white; -fx-background-radius: 10px; -fx-padding: 3px 10px;");*/
                }
            }
        });

        // Actions column with buttons
        actionsColumn.setCellFactory(param -> new TableCell<>() {
            private final Button toggleBtn = new Button();
            private final Button editBtn = new Button("Edit");
            private final Button deleteBtn = new Button("Delete");
            private final HBox buttons = new HBox(5, toggleBtn, editBtn);

            {
                buttons.setStyle("-fx-alignment: CENTER;");
                toggleBtn.setStyle("-fx-background-color: #FF9800; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 5px 10px; -fx-background-radius: 5px;");
                editBtn.setStyle("-fx-background-color: #2196F3; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 5px 10px; -fx-background-radius: 5px;");
                deleteBtn.setStyle("-fx-background-color: #E53935; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 5px 10px; -fx-background-radius: 5px;");

                toggleBtn.setOnAction(event -> {
                    User user = getTableView().getItems().get(getIndex());
                    toggleUserStatus(user);
                });

                editBtn.setOnAction(event -> {
                    User user = getTableView().getItems().get(getIndex());
                    editUser(user);
                });

                deleteBtn.setOnAction(event -> {
                    User user = getTableView().getItems().get(getIndex());
                    deleteUser(user);
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    User user = getTableView().getItems().get(getIndex());
                    toggleBtn.setText(user.isActive() ? "Deactivate" : "Activate");
                    setGraphic(buttons);
                }
            }
        });
    }
    private void setupRoleFilterCombo() {
        filterCombo.getSelectionModel().selectFirst();
    }

    @FXML
    private void refreshUsers() {
        loadUsers();
    }

    private void loadUsers() {
        try {
            List<User> users = userService.getAll();
            allUsers.setAll(users);
            filteredUsers.setAll(users);
            updatePagination();
        } catch (Exception e) {
            showAlert("Error", "Failed to load users: " + e.getMessage(), Alert.AlertType.ERROR);
            e.printStackTrace();
        }
    }

    private void setupSearchFilter() {
        searchField.textProperty().addListener((observable, oldValue, newValue) -> {
            filterUsers();
        });
    }

    private void setupRoleFilter() {
        filterCombo.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            filterUsers();
        });
    }

    private void filterUsers() {
        String searchText = searchField.getText().toLowerCase();
        String selectedRole = filterCombo.getValue();

        filteredUsers.setAll(allUsers.filtered(user -> {
            boolean matchesSearch = searchText.isEmpty() ||
                    user.getFullName().toLowerCase().contains(searchText) ||
                    user.getEmail().toLowerCase().contains(searchText);

            boolean matchesRole = selectedRole.equals("All") ||
                    user.getRole().equalsIgnoreCase(selectedRole);

            return matchesSearch && matchesRole;
        }));

        updatePagination();
    }


    @FXML
    private void openAddUser() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/User/Admin/AddUser.fxml"));
            Parent root = loader.load();

            Stage statsStage = new Stage();
            statsStage.setScene(new Scene(root));
            statsStage.initModality(Modality.WINDOW_MODAL);
            statsStage.initOwner(border.getScene().getWindow());
            statsStage.show();
        } catch (IOException e) {
            showAlert("Error", "Could not load statistics dashboard", Alert.AlertType.ERROR);
            e.printStackTrace();
        }
    }

    @FXML
    private void showUserStats() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/User/Admin/UserStats.fxml"));
            Parent root = loader.load();

            Stage statsStage = new Stage();
            statsStage.setTitle("User Statistics Dashboard");
            statsStage.setScene(new Scene(root));
            statsStage.initModality(Modality.WINDOW_MODAL);
            statsStage.initOwner(border.getScene().getWindow());
            statsStage.show();
        } catch (IOException e) {
            showAlert("Error", "Could not load statistics dashboard", Alert.AlertType.ERROR);
            e.printStackTrace();
        }
    }
    private void toggleUserStatus(User user) {
        try {
            boolean newStatus = !user.isActive();
            userService.updateUserStatus(user.getId(), newStatus);
            user.setActive(newStatus);
            usersTable.refresh();

            showAlert("Success",
                    String.format("User %s %s successfully",
                            user.getFullName(),
                            newStatus ? "activated" : "deactivated"),
                    Alert.AlertType.INFORMATION);
        } catch (SQLException e) {
            showAlert("Error", "Failed to update user status: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    private void editUser(User user) {
        try {
            // Load the edit user form
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/User/Admin/EditUser.fxml"));
            Parent root = loader.load();

            // Get the controller and pass the user to edit
            EditUserController controller = loader.getController();
            controller.setUserToEdit(user);

            // Create and show the edit window
            Stage stage = new Stage();
            stage.setScene(new Scene(root));
            stage.setTitle("Edit User: " + user.getFullName());
            stage.initModality(Modality.APPLICATION_MODAL); // Make it modal
            stage.showAndWait();

            // Refresh the table after editing
            loadUsers();
        } catch (IOException e) {
            showAlert("Error", "Failed to open edit form: " + e.getMessage(), Alert.AlertType.ERROR);
            e.printStackTrace();
        }
    }

    private void deleteUser(User user) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirm Delete");
        alert.setHeaderText("Delete User");
        alert.setContentText("Are you sure you want to delete " + user.getFullName() + "?");

        alert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                try {
                    userService.delete(user.getId());
                    loadUsers(); // Refresh the table
                    showAlert("Success", "User deleted successfully", Alert.AlertType.INFORMATION);
                } catch (Exception e) {
                    showAlert("Error", "Failed to delete user: " + e.getMessage(), Alert.AlertType.ERROR);
                }
            }
        });
    }

    private void showAlert(String title, String message, Alert.AlertType alertType) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}