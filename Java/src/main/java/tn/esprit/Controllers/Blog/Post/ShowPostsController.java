package tn.esprit.Controllers.Blog.Post;

import javafx.animation.FadeTransition;
import javafx.animation.RotateTransition;
import javafx.animation.ScaleTransition;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.effect.DropShadow;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.util.Duration;
import tn.esprit.entities.Post;
import tn.esprit.entities.User;
import tn.esprit.services.ServiceLike;
import tn.esprit.services.ServicePost;
import tn.esprit.tools.SessionManager;
import tn.esprit.tools.WeatherService;

import java.io.IOException;
import java.sql.SQLException;
import java.util.*;

public class ShowPostsController {

    @FXML private StackPane viewContainer;
    @FXML private ListView<Post> postListView;
    @FXML private TableView<Post> postTableView;
    @FXML private TableColumn<Post, String> titleCol;
    @FXML private TableColumn<Post, String> categoryCol;
    @FXML private TableColumn<Post, String> dateCol;
    @FXML private TableColumn<Post, Integer> likesCol;
    @FXML private TableColumn<Post, Void> actionsCol;
    @FXML private HBox adminControls;
    @FXML private BorderPane borderPane;
    @FXML private TextField searchField;
    @FXML private Button mod, supp, ajouter, statsBtn, detailsBtn;
    @FXML private ComboBox<String> categoryCombo;
    @FXML private Button toggleViewButton;
    private boolean showingAllPosts = false;
    private ObservableList<Post> allPostsList;
    private ObservableList<Post> topPostsList;
    @FXML private Label postsTitleLabel;
    private final Map<String, String> categoryMap = Map.of(
            "All", "All",
            "Agriculture", "agriculture_news",
            "Technology", "technology",
            "Recipes", "recipes",
            "Urban Farming", "urban_farming"
    );
    private final ServiceLike likeService = new ServiceLike();
    private final ServicePost pubService = new ServicePost();
    private ObservableList<Post> publicationList;
    private Timer filterTimer;

    public void initialize() {
        User currentUser = SessionManager.getInstance().getCurrentUser();
        boolean isAdmin = "ROLE_ADMIN".equalsIgnoreCase(currentUser.getRole());

        if (isAdmin) {
            setupTableView();
            postTableView.setVisible(true);
            postTableView.setManaged(true);
            postListView.setVisible(false);
            postListView.setManaged(false);
        } else {
            setupListView();
            postListView.setVisible(true);
            postListView.setManaged(true);
            postTableView.setVisible(false);
            postTableView.setManaged(false);
            toggleViewButton.setVisible(true);
        }

        // Configure admin controls
        ajouter.setVisible(isAdmin);
        mod.setVisible(isAdmin);
        supp.setVisible(isAdmin);
        detailsBtn.setVisible(isAdmin);
        statsBtn.setVisible(isAdmin);
        adminControls.managedProperty().bind(adminControls.visibleProperty());

        // Disable action buttons initially
        mod.setDisable(true);
        supp.setDisable(true);
        detailsBtn.setDisable(true);

        // Setup selection listeners
        setupSelectionListeners();

        // Setup UI components
        setupCategoryCombo();
        setupFilters();
        loadPublications();
    }

    private void setupSelectionListeners() {
        // ListView selection listener
        postListView.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            boolean isDisabled = (newVal == null);
            mod.setDisable(isDisabled);
            supp.setDisable(isDisabled);
            detailsBtn.setDisable(isDisabled);
        });

        // TableView selection listener
        postTableView.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            boolean isDisabled = (newVal == null);
            mod.setDisable(isDisabled);
            supp.setDisable(isDisabled);
            detailsBtn.setDisable(isDisabled);
        });
    }

    private void setupListView() {
        postListView.setCellFactory(param -> {
            try {
                return new PostCell();
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        });
        postListView.setPlaceholder(new Label("No posts available"));
    }

    private void setupTableView() {
        // Configure columns
        titleCol.setCellValueFactory(new PropertyValueFactory<>("titre"));
        categoryCol.setCellValueFactory(new PropertyValueFactory<>("category"));
        dateCol.setCellValueFactory(new PropertyValueFactory<>("date"));

        likesCol.setCellValueFactory(cellData -> {
            int likes = likeService.countReactions(cellData.getValue().getId());
            return new SimpleIntegerProperty(likes).asObject();
        });

        // Remove the actions column since we'll use the buttons below
        postTableView.getColumns().remove(actionsCol);

        // Configure selection listener
        postTableView.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            boolean isDisabled = (newVal == null);
            mod.setDisable(isDisabled);
            supp.setDisable(isDisabled);
        });

        postTableView.setPlaceholder(new Label("No posts available"));
    }

    @FXML
    private void handleDetails(ActionEvent event) {
        Post selectedPost = getSelectedPost();
        if (selectedPost == null) {
            showError("Error", "Please select a post to view details.");
            return;
        }
        showPostDetailsModal(selectedPost);
    }

    private void showPostDetailsModal(Post post) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/Blog/PostDetails.fxml"));
            Parent root = loader.load();

            PostDetailsController controller = loader.getController();
            controller.setPost(post);

            Stage modalStage = new Stage();
            modalStage.setTitle("Post Details");
            modalStage.setScene(new Scene(root));
            modalStage.initModality(Modality.APPLICATION_MODAL);
            modalStage.initOwner(borderPane.getScene().getWindow());
            modalStage.showAndWait();
        } catch (IOException e) {
            showError("Error", "Could not load post details: " + e.getMessage());
        }
    }

    private void setupCategoryCombo() {
        categoryCombo.setItems(FXCollections.observableArrayList(
                "All", "Agriculture", "Technology", "Recipes", "Urban Farming"
        ));
        categoryCombo.getSelectionModel().select(0);
    }

    private void setupFilters() {
        // Debounced search field listener
        searchField.textProperty().addListener((obs, oldVal, newVal) -> {
            if (filterTimer != null) {
                filterTimer.cancel();
            }
            filterTimer = new Timer();
            filterTimer.schedule(new TimerTask() {
                @Override
                public void run() {
                    Platform.runLater(() -> {
                        try {
                            filterPosts();
                        } catch (SQLException e) {
                            showError("Filter Error", "Failed to apply filters: " + e.getMessage());
                        }
                    });
                }
            }, 300); // 300ms delay
        });

        // Immediate category filter listener
        categoryCombo.valueProperty().addListener((obs, oldVal, newVal) -> {
            try {
                filterPosts();
            } catch (SQLException e) {
                showError("Filter Error", "Failed to filter by category: " + e.getMessage());
            }
        });
    }

    private void loadPublications() {
        try {
            // Load all posts
            allPostsList = FXCollections.observableArrayList(pubService.getAll());
            topPostsList = FXCollections.observableArrayList(pubService.getTopEngagedPosts(3));

            User currentUser = SessionManager.getInstance().getCurrentUser();
            boolean isAdmin = "ROLE_ADMIN".equalsIgnoreCase(currentUser.getRole());

            if (isAdmin) {
                postTableView.setItems(allPostsList);
            } else {
                showTopPosts();
            }
        } catch (Exception e) {
            showError("Error", "Failed to load publications: " + e.getMessage());
            postListView.setPlaceholder(new Label("Error loading posts"));
            postTableView.setPlaceholder(new Label("Error loading posts"));
        }
    }

    private void showTopPosts() {
        postListView.setItems(topPostsList);
        postsTitleLabel.setText("Top 3 Most Liked Posts");
        toggleViewButton.setText("Show All Posts");
        showingAllPosts = false;
    }

    // Update your showAllPosts method
    private void showAllPosts() {
        postListView.setItems(allPostsList);
        postsTitleLabel.setText("All Posts");
        toggleViewButton.setText("Show Top Posts");
        showingAllPosts = true;
    }

    // Update your filterPosts method
    private void filterPosts() throws SQLException {
        String searchText = searchField.getText().trim();
        String selectedCategory = categoryCombo.getValue();
        String categoryValue = categoryMap.get(selectedCategory);

        User currentUser = SessionManager.getInstance().getCurrentUser();
        boolean isAdmin = "ROLE_ADMIN".equalsIgnoreCase(currentUser.getRole());

        if (isAdmin) {
            postTableView.setPlaceholder(new Label("Loading posts..."));
        } else {
            postListView.setPlaceholder(new Label("Loading posts..."));
        }

        Task<List<Post>> filterTask = new Task<>() {
            @Override
            protected List<Post> call() throws Exception {
                return pubService.getFilteredPosts(categoryValue, searchText);
            }
        };

        filterTask.setOnSucceeded(e -> {
            List<Post> filteredPosts = filterTask.getValue();
            allPostsList.setAll(filteredPosts);

            if (isAdmin) {
                postTableView.setPlaceholder(new Label("No posts found"));
            } else {
                if (!showingAllPosts) {
                    List<Post> filteredTopPosts = filteredPosts.stream()
                            .sorted((p1, p2) -> {
                                int likes1 = likeService.countReactions(p1.getId());
                                int likes2 = likeService.countReactions(p2.getId());
                                return Integer.compare(likes2, likes1);
                            })
                            .limit(3)
                            .toList();
                    topPostsList.setAll(filteredTopPosts);
                }
                postListView.setPlaceholder(new Label("No posts found"));
            }
        });

        filterTask.setOnFailed(e -> {
            showError("Filter Error", "Failed to load posts: " + filterTask.getException().getMessage());
            if (isAdmin) {
                postTableView.setPlaceholder(new Label("Error loading posts"));
            } else {
                postListView.setPlaceholder(new Label("Error loading posts"));
            }
        });

        new Thread(filterTask).start();
    }

    @FXML
    private void handleToggleView(ActionEvent event) {
        if (showingAllPosts) {
            showTopPosts();
        } else {
            showAllPosts();
        }
    }

    @FXML
    private void handleRefresh(ActionEvent event) {
        loadPublications();
        searchField.clear();
        categoryCombo.getSelectionModel().select(0);
        showInfo("Refreshed", "Posts list has been refreshed");
    }

    @FXML
    private void handleModifier(ActionEvent event) {
        Post selectedPost = getSelectedPost();
        if (selectedPost == null) {
            showError("Error", "Please select a post to edit.");
            return;
        }

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/Blog/AddPost.fxml"));
            Parent root = loader.load();

            AddPostController controller = loader.getController();
            controller.setPublication(selectedPost);

            Stage modalStage = new Stage();
            modalStage.setTitle("Edit Post");
            modalStage.setScene(new Scene(root));
            modalStage.initModality(Modality.WINDOW_MODAL);
            modalStage.initOwner(borderPane.getScene().getWindow());
            modalStage.setResizable(false);
            modalStage.showAndWait();

            loadPublications();
        } catch (IOException e) {
            showError("Error", "Failed to open the edit page: " + e.getMessage());
        }
    }

    @FXML
    private void handleSupprimer(ActionEvent event) {
        Post selectedPost = getSelectedPost();
        if (selectedPost == null) {
            showError("Error", "Please select a post to delete.");
            return;
        }

        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Delete Confirmation");
        alert.setHeaderText("Are you sure you want to delete this post?");
        alert.setContentText("This action cannot be undone.");

        alert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                try {
                    pubService.supprimer(selectedPost);
                    if (postTableView.isVisible()) {
                        postTableView.getItems().remove(selectedPost);
                    } else {
                        postListView.getItems().remove(selectedPost);
                    }
                    showInfo("Success", "Post deleted successfully.");
                } catch (Exception e) {
                    showError("Error", "Failed to delete the post: " + e.getMessage());
                }
            }
        });
    }

    private Post getSelectedPost() {
        if (postTableView.isVisible()) {
            return postTableView.getSelectionModel().getSelectedItem();
        } else {
            return postListView.getSelectionModel().getSelectedItem();
        }
    }

    @FXML
    public void handleShowWeather() {
        // Create styled dialog for city selection
        Dialog<String> dialog = new Dialog<>();
        dialog.setTitle("🌦 Tunisia Weather for Farmers");
        dialog.getDialogPane().getStylesheets().add(getClass().getResource("/styles/weather.css").toExternalForm());

        // Custom dialog header with animation
        HBox header = new HBox(10);
        header.setAlignment(Pos.CENTER_LEFT);
        ImageView weatherIcon = new ImageView(new Image(getClass().getResourceAsStream("/Images/weather/weather.gif")));
        weatherIcon.setFitHeight(40);
        weatherIcon.setFitWidth(40);

        // Add pulsing animation to the icon
        ScaleTransition pulse = new ScaleTransition(Duration.seconds(1), weatherIcon);
        pulse.setFromX(1);
        pulse.setFromY(1);
        pulse.setToX(1.1);
        pulse.setToY(1.1);
        pulse.setAutoReverse(true);
        pulse.setCycleCount(ScaleTransition.INDEFINITE);
        pulse.play();

        Label headerText = new Label("Select a city to view weather conditions");
        headerText.setStyle("-fx-font-size: 16px; -fx-text-fill: #2c3e50;");
        header.getChildren().addAll(weatherIcon, headerText);
        dialog.getDialogPane().setHeader(header);

        // Set button types with custom styles
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        // Style the combo box
        ComboBox<String> cityCombo = new ComboBox<>();
        cityCombo.getItems().addAll(WeatherService.getTunisianCities().keySet());
        cityCombo.setPromptText("Select a city");
        cityCombo.setStyle("-fx-font-size: 14px; -fx-pref-width: 250px; -fx-background-radius: 20;");

        VBox vbox = new VBox(15, new Label("🌍 Select Tunisian city:"), cityCombo);
        vbox.setPadding(new Insets(20));
        vbox.setStyle("-fx-background-color: #f8f9fa;");
        dialog.getDialogPane().setContent(vbox);

        // Convert result to city name when OK is clicked
        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == ButtonType.OK) {
                return cityCombo.getValue();
            }
            return null;
        });

        // Show dialog and process result
        Optional<String> result = dialog.showAndWait();
        if (result.isPresent() && result.get() != null) {
            showWeatherDetails(result.get());
        }
    }

    private void showWeatherDetails(String city) {
        Stage weatherStage = new Stage();
        weatherStage.initModality(Modality.NONE);
        weatherStage.initStyle(StageStyle.UTILITY);

        // Main container with gradient background
        VBox root = new VBox(15);
        root.setPadding(new Insets(20));
        root.setStyle("-fx-background-color: linear-gradient(to bottom, #4facfe 0%, #00f2fe 100%);");

        // City header
        Label cityLabel = new Label("Weather in " + city);
        cityLabel.setStyle("-fx-font-size: 24px; -fx-font-weight: bold; -fx-text-fill: white;");

        // Weather details grid
        GridPane grid = new GridPane();
        grid.setHgap(15);
        grid.setVgap(10);
        grid.setPadding(new Insets(15));
        grid.setStyle("-fx-background-color: rgba(255,255,255,0.2); -fx-background-radius: 10;");

        String weatherInfo = WeatherService.getWeatherForCity(city);
        String[] parts = weatherInfo.split("\n");

        // Extract weather data for advice
        double temp = Double.parseDouble(parts[1].split(":")[1].replace("°C","").trim());
        int humidity = Integer.parseInt(parts[2].split(":")[1].replace("%","").trim());
        String conditions = parts[3].split(":")[1].trim();

        // Temperature row
        ImageView tempIcon = new ImageView(new Image(getClass().getResourceAsStream("/images/weather/temp.png")));
        tempIcon.setFitHeight(30);
        tempIcon.setFitWidth(30);
        FadeTransition fade = new FadeTransition(Duration.seconds(2), tempIcon);
        fade.setFromValue(0.5);
        fade.setToValue(1.0);
        fade.setCycleCount(FadeTransition.INDEFINITE);
        fade.setAutoReverse(true);
        fade.play();

        grid.add(tempIcon, 0, 0);
        grid.add(createWeatherLabel(parts[1], "-fx-font-size: 18px; -fx-text-fill: white;"), 1, 0);

        // Humidity row
        ImageView humidityIcon = new ImageView(new Image(getClass().getResourceAsStream("/images/weather/humidity.png")));
        humidityIcon.setFitHeight(30);
        humidityIcon.setFitWidth(30);
        grid.add(humidityIcon, 0, 1);
        grid.add(createWeatherLabel(parts[2], "-fx-font-size: 18px; -fx-text-fill: white;"), 1, 1);

        // Conditions row
        ImageView conditionsIcon = getConditionsIcon(conditions);
        conditionsIcon.setFitHeight(30);
        conditionsIcon.setFitWidth(30);
        grid.add(conditionsIcon, 0, 2);
        grid.add(createWeatherLabel(parts[3], "-fx-font-size: 18px; -fx-text-fill: white;"), 1, 2);

        // Farmer advice
        Label adviceLabel = new Label(WeatherService.getAgriculturalAdvice(temp, humidity, conditions));
        adviceLabel.setWrapText(true);
        adviceLabel.setStyle("-fx-font-size: 16px; -fx-text-fill: #fff; -fx-font-weight: bold; -fx-padding: 10; " +
                "-fx-background-color: rgba(0,0,0,0.2); -fx-background-radius: 10;");

        root.getChildren().addAll(cityLabel, grid, adviceLabel);

        // Scene with smooth fade-in animation
        Scene scene = new Scene(root, 400, 400); // Slightly taller for advice
        scene.setFill(Color.TRANSPARENT);
        weatherStage.setScene(scene);
        weatherStage.setTitle("🌤 Farmers Weather Info - " + city);

        // Add drop shadow effect
        root.setEffect(new DropShadow(20, Color.BLACK));

        // Fade in animation when showing
        weatherStage.setOnShown(e -> {
            FadeTransition ft = new FadeTransition(Duration.millis(500), root);
            ft.setFromValue(0);
            ft.setToValue(1);
            ft.play();
        });

        weatherStage.show();
    }

    private Label createWeatherLabel(String text, String style) {
        Label label = new Label(text);
        label.setStyle(style);
        return label;
    }

    private ImageView getConditionsIcon(String conditions) {
        String iconPath = "/images/weather/";
        if (conditions.toLowerCase().contains("rain")) {
            iconPath += "rain.png";
        } else if (conditions.toLowerCase().contains("cloud")) {
            iconPath += "cloud.png";
        } else if (conditions.toLowerCase().contains("sun")) {
            iconPath += "sun.png";
        } else {
            iconPath += "generic.png";
        }
        return new ImageView(new Image(getClass().getResourceAsStream(iconPath)));
    }

    @FXML
    private void handleShowStats(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/Blog/Statistiques.fxml"));
            Parent root = loader.load();

            Stage statsStage = new Stage();
            statsStage.setTitle("Post Statistics");
            statsStage.setScene(new Scene(root));
            statsStage.initModality(javafx.stage.Modality.WINDOW_MODAL);
            statsStage.initOwner(borderPane.getScene().getWindow());
            statsStage.show();
        } catch (IOException e) {
            showError("Error", "Could not load the statistics window: " + e.getMessage());
        }
    }

    @FXML
    private void handleAjouter(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/Blog/AddPost.fxml"));
            Parent root = loader.load();

            Stage modalStage = new Stage();
            modalStage.setTitle("Add Publication");
            modalStage.setScene(new Scene(root));
            modalStage.initModality(javafx.stage.Modality.WINDOW_MODAL);
            modalStage.initOwner(borderPane.getScene().getWindow());
            modalStage.setResizable(false);
            modalStage.showAndWait();

            loadPublications();
        } catch (IOException e) {
            showError("Error", "Failed to open the add publication page: " + e.getMessage());
        }
    }

    private void showError(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void showInfo(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}