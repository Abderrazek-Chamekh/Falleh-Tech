package tn.esprit.Controllers.produitfront;

import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import tn.esprit.entities.Favoris;
import tn.esprit.entities.Produit;
import tn.esprit.entities.User;
import tn.esprit.services.FavorisService;
import tn.esprit.services.PanierService;
import tn.esprit.tools.SessionManager;
import tn.esprit.utils.ImageUtils;
import javafx.scene.layout.FlowPane;

import java.net.URL;
import java.util.*;
import java.util.stream.Collectors;

public class FavorisController implements Initializable {

    @FXML private TableView<Favoris> tableFavoris;
    @FXML private TableColumn<Favoris, ImageView> colImage;
    @FXML private TableColumn<Favoris, String> colNom;
    @FXML private TableColumn<Favoris, String> colPrix;
    @FXML private TableColumn<Favoris, Void> colActions;
    @FXML private FlowPane boxSuggestions;
    @FXML private Label labelSuggestions;

    private final FavorisService favorisService = new FavorisService();
    private final PanierService panierService = PanierService.getInstance();
    private List<Produit> currentSuggestions = new ArrayList<>();

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        tableFavoris.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_ALL_COLUMNS);
        colActions.setMaxWidth(140);
        loadFavoris();
    }

    private void loadFavoris() {
        tableFavoris.getItems().clear();

        User currentUser = SessionManager.getInstance().getCurrentUser();
        if (currentUser == null) {
            System.err.println("❌ Aucun utilisateur connecté !");
            return;
        }

        List<Favoris> favorisList = favorisService.getFavorisParUser(currentUser.getId());

        colImage.setCellValueFactory(data -> {
            Produit produit = data.getValue().getProduit();
            ImageView imageView = new ImageView(ImageUtils.chargerDepuisNom(produit.getImage()));
            imageView.setFitHeight(50);
            imageView.setFitWidth(50);
            imageView.setPreserveRatio(true);
            return new SimpleObjectProperty<>(imageView);
        });

        colNom.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getProduit().getNom()));
        colPrix.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getProduit().getPrix() + " DT"));

        colActions.setCellFactory(param -> new TableCell<>() {
            private final Button btnAddCart = new Button();
            private final Button btnDelete = new Button("🗑");
            private final HBox actionBox = new HBox(8, btnAddCart, btnDelete);

            {
                actionBox.setAlignment(Pos.CENTER_LEFT);

                try {
                    Image image = new Image(getClass().getResourceAsStream("/icons/cart.png"));
                    ImageView imageView = new ImageView(image);
                    imageView.setFitWidth(16);
                    imageView.setFitHeight(16);
                    btnAddCart.setGraphic(imageView);
                } catch (Exception e) {
                    btnAddCart.setText("🛒");
                }

                btnAddCart.setPrefSize(30, 30);
                btnDelete.setPrefSize(30, 30);
                btnAddCart.getStyleClass().add("add-to-cart-button");
                btnDelete.getStyleClass().add("delete-button");

                btnAddCart.setOnAction(event -> {
                    Favoris favoris = getTableView().getItems().get(getIndex());
                    Produit produit = favoris.getProduit();
                    if (produit.getStock() == 0) {
                        showAlert(Alert.AlertType.ERROR, "Rupture de stock", "Ce produit est en rupture de stock.");
                        return;
                    }
                    if (produit.getStock() <= 5) {
                        showAlert(Alert.AlertType.WARNING, "Stock faible", "⚠ Il ne reste que " + produit.getStock() + " unités !");
                    }

                    panierService.ajouterProduit(produit, 1);
                    showAlert(Alert.AlertType.INFORMATION, null, produit.getNom() + " ajouté au panier !");
                });

                btnDelete.setOnAction(event -> {
                    Favoris favoris = getTableView().getItems().get(getIndex());
                    favorisService.supprimer(favoris.getId());
                    getTableView().getItems().remove(favoris);
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : actionBox);
            }
        });

        tableFavoris.getItems().addAll(favorisList);
        loadSuggestions(currentUser.getId());
    }

    private void loadSuggestions(int userId) {
        currentSuggestions = suggereProduitsSimilaires(userId);
        labelSuggestions.setVisible(true);
        boxSuggestions.setVisible(true);
        boxSuggestions.getChildren().clear();

        Set<Long> idsDéjàAffichés = new HashSet<>();
        int maxSuggestions = 4;
        int count = 0;

        for (Produit p : currentSuggestions) {
            if (idsDéjàAffichés.contains(p.getId())) continue;

            boxSuggestions.getChildren().add(creerCarteSuggestion(p, userId, currentSuggestions));
            idsDéjàAffichés.add(p.getId());
            count++;
            if (count >= maxSuggestions) break;
        }

        if (count == 0) {
            Label vide = new Label("Aucune suggestion disponible.");
            vide.getStyleClass().add("empty-suggestion-message");
            boxSuggestions.getChildren().add(vide);
        }
    }

    private VBox creerCarteSuggestion(Produit produit, int userId, List<Produit> suggestions) {
        VBox card = new VBox(5);
        card.getStyleClass().add("suggestion-card");
        card.setAlignment(Pos.CENTER);
        card.setPrefWidth(150);
        card.setMaxWidth(150);
        card.setMinWidth(150);

        ImageView image = new ImageView(ImageUtils.chargerDepuisNom(produit.getImage()));
        image.setFitWidth(100);
        image.setFitHeight(100);
        image.setPreserveRatio(true);
        image.getStyleClass().add("image-view");

        Label nom = new Label(produit.getNom());
        nom.getStyleClass().add("nom-label");

        Label prix = new Label("Prix: " + produit.getPrix() + " DT");
        prix.getStyleClass().add("prix-label");

        Button btnFavoris = new Button("❤ Favoris");
        btnFavoris.getStyleClass().add("add-fav-button");

        btnFavoris.setOnAction(e -> {
            if (!favorisService.existeDansFavoris(produit.getId(), userId)) {
                if (!favorisService.ajouterFavoris(produit.getId(), userId)) {
                    // 1. Ajouter le nouveau favori à la table
                    Favoris nouveau = new Favoris();
                    nouveau.setUserId(userId);
                    nouveau.setProduit(produit);
                    tableFavoris.getItems().add(nouveau);

                    // 2. Supprimer cette carte de suggestions
                    boxSuggestions.getChildren().remove(card);

                    // 3. Recalculer toutes les suggestions valides
                    currentSuggestions = favorisService.suggereProduitsSimilaires(userId);

                    // 4. Filtrer celles déjà affichées dans favoris et celles déjà visibles
                    Set<Long> idsDejaAffiches = new HashSet<>();
                    for (Favoris f : tableFavoris.getItems()) {
                        idsDejaAffiches.add(f.getProduit().getId());
                    }
                    for (javafx.scene.Node node : boxSuggestions.getChildren()) {
                        if (node instanceof VBox) {
                            VBox vbox = (VBox) node;
                            for (javafx.scene.Node child : vbox.getChildren()) {
                                if (child instanceof Label label && label.getStyleClass().contains("nom-label")) {
                                    Produit existant = currentSuggestions.stream()
                                            .filter(p -> p.getNom().equalsIgnoreCase(label.getText()))
                                            .findFirst().orElse(null);
                                    if (existant != null) idsDejaAffiches.add(existant.getId());
                                }
                            }
                        }
                    }

                    // 5. Ajouter une nouvelle suggestion unique
                    for (Produit np : currentSuggestions) {
                        if (!idsDejaAffiches.contains(np.getId())) {
                            VBox newCard = creerCarteSuggestion(np, userId, currentSuggestions);
                            boxSuggestions.getChildren().add(newCard);
                            break;
                        }
                    }
                } else {
                    showAlert(Alert.AlertType.ERROR, "Erreur", "Ajout du favori échoué !");
                }
            }
        });


        card.getChildren().addAll(image, nom, prix, btnFavoris);
        return card;
    }

    private void showAlert(Alert.AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setHeaderText(title);
        alert.setContentText(content);
        alert.showAndWait();
    }

    private List<Produit> suggereProduitsSimilaires(int userId) {
        return favorisService.suggereProduitsSimilaires(userId);
    }
}
