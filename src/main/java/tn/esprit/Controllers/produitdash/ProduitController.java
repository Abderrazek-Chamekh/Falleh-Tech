package tn.esprit.Controllers.produitdash;

import jakarta.mail.*;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.StringConverter;
import tn.esprit.entities.Categorie;
import tn.esprit.entities.Produit;
import tn.esprit.entities.SousCategorie;
import tn.esprit.utils.ImageUtils;
import tn.esprit.services.ProduitService;
import tn.esprit.services.CategorieService;
import tn.esprit.services.SousCategorieService;
import tn.esprit.Controllers.produitfront.AjouterProduitController;


import java.io.IOException;
import java.math.BigDecimal;
import java.net.URL;
import java.util.List;
import java.util.Optional;
import java.util.Properties;
import java.util.ResourceBundle;

public class ProduitController implements Initializable {

    @FXML private TableView<Produit> tableView;

    @FXML private TableColumn<Produit, String> nomCol, descCol, imageCol, categorieCol, sousCategorieCol;
    @FXML private TableColumn<Produit, BigDecimal> prixCol;
    @FXML private TableColumn<Produit, Integer> stockCol;
    @FXML private TableColumn<Produit, Void> actionCol;

    @FXML private ComboBox<String> filtreTypeComboBox;
    @FXML private TextField rechercheField;
    @FXML private ComboBox<Categorie> filtreCategorieComboBox;
    @FXML private ComboBox<SousCategorie> filtreSousCategorieComboBox;
    @FXML private ComboBox<String> filtreStockComboBox;

    private final ProduitService produitService = new ProduitService();
    private final CategorieService categorieService = new CategorieService();
    private final SousCategorieService sousCategorieService = new SousCategorieService();

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        configColonnes();
        configFiltrage();
        setupActionColumn();
        afficherProduits();
    }

    private void configColonnes() {


        nomCol.setCellValueFactory(new PropertyValueFactory<>("nom"));
        nomCol.setPrefWidth(140);

        descCol.setCellValueFactory(new PropertyValueFactory<>("description"));
        descCol.setPrefWidth(180);

        prixCol.setCellValueFactory(new PropertyValueFactory<>("prix"));
        prixCol.setPrefWidth(80);

        // 🌡️ Colonne Stock avec alerte visuelle + bouton mail
        stockCol.setCellValueFactory(new PropertyValueFactory<>("stock"));
        stockCol.setCellFactory(col -> new TableCell<>() {
            private final Label stockLabel = new Label();
            private final Button mailBtn = new Button();
            private final HBox box = new HBox(5, stockLabel, mailBtn);

            {
                // Icône
                URL iconUrl = getClass().getResource("/icons/mail.png");
                if (iconUrl != null) {
                    ImageView icon = new ImageView(new Image(iconUrl.toExternalForm()));
                    icon.setFitHeight(16);
                    icon.setFitWidth(16);
                    mailBtn.setGraphic(icon);
                }
                mailBtn.setStyle("-fx-background-color: transparent;");
                mailBtn.setOnAction(e -> {
                    Produit produit = getTableView().getItems().get(getIndex());
                    envoyerMailStockFaible(produit);
                });
            }

            @Override
            protected void updateItem(Integer stock, boolean empty) {
                super.updateItem(stock, empty);

                if (empty || stock == null) {
                    setGraphic(null);
                } else {
                    stockLabel.setText(String.valueOf(stock));
                    mailBtn.setVisible(stock <= 5); // on cache ou montre le bouton
                    stockLabel.setStyle(stock <= 5
                            ? "-fx-text-fill: red; -fx-font-weight: bold;"
                            : "-fx-text-fill: green;");
                    setGraphic(box); // toujours afficher le HBox
                }
            }
        });


        // 📂 Catégorie
        categorieCol.setCellValueFactory(c -> new ReadOnlyObjectWrapper<>(
                Optional.ofNullable(c.getValue().getCategorie()).map(Categorie::getNom).orElse("")
        ));
        categorieCol.setPrefWidth(130);

        // 📂 Sous-Catégorie
        sousCategorieCol.setCellValueFactory(c -> new ReadOnlyObjectWrapper<>(
                Optional.ofNullable(c.getValue().getSousCategorie()).map(SousCategorie::getNom).orElse("")
        ));
        sousCategorieCol.setPrefWidth(140);

        // 🖼️ Colonne Image
        imageCol.setCellValueFactory(new PropertyValueFactory<>("image"));
        imageCol.setPrefWidth(100);
        imageCol.setCellFactory(param -> new TableCell<>() {
            private final ImageView imageView = new ImageView();
            {
                imageView.setFitWidth(80);
                imageView.setFitHeight(60);
                imageView.setPreserveRatio(true);
            }

            @Override
            protected void updateItem(String imageName, boolean empty) {
                super.updateItem(imageName, empty);
                if (empty || imageName == null || imageName.isBlank()) {
                    setGraphic(null);
                } else {
                    imageView.setImage(ImageUtils.chargerDepuisNom(imageName));
                    setGraphic(imageView);
                }
            }
        });

        // 📐 Résize auto (optionnel)
        tableView.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);
    }

    private void setupActionColumn() {
        actionCol.setCellFactory(col -> new TableCell<>() {
            private final Button editBtn = createIconButton("/icons/modifier.png");
            private final Button deleteBtn = createIconButton("/icons/delete.png");
            private final HBox hbox = new HBox(10, editBtn, deleteBtn);

            {
                editBtn.setOnAction(e -> openPopup("/front/produit/ajouterproduit.fxml", getTableView().getItems().get(getIndex())));
                deleteBtn.setOnAction(e -> {
                    Produit produit = getTableView().getItems().get(getIndex());
                    Alert confirm = new Alert(Alert.AlertType.CONFIRMATION, "Supprimer ce produit ?", ButtonType.YES, ButtonType.NO);
                    confirm.setHeaderText("Confirmation");
                    confirm.showAndWait().ifPresent(response -> {
                        if (response == ButtonType.YES) {
                            produitService.supprimer(produit);
                            afficherProduits();
                        }
                    });
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : hbox);
            }
        });
        actionCol.setCellValueFactory(data -> new ReadOnlyObjectWrapper<>(null));
    }

    private Button createIconButton(String iconPath) {
        Button button = new Button();
        try {
            URL iconUrl = getClass().getResource(iconPath);
            if (iconUrl != null) {
                ImageView icon = new ImageView(new Image(iconUrl.toExternalForm()));
                icon.setFitWidth(18);
                icon.setFitHeight(18);
                button.setGraphic(icon);
                button.getStyleClass().add("action-button");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return button;
    }

    private void configFiltrage() {
        filtreTypeComboBox.setItems(FXCollections.observableArrayList("Nom", "Catégorie", "Sous-Catégorie", "Stock"));
        filtreCategorieComboBox.setItems(FXCollections.observableArrayList(categorieService.getAll()));
        filtreSousCategorieComboBox.setItems(FXCollections.observableArrayList(sousCategorieService.getAll()));
        filtreStockComboBox.setItems(FXCollections.observableArrayList("Stock faible", "Stock suffisant"));

        filtreCategorieComboBox.setConverter(new StringConverter<>() {
            @Override public String toString(Categorie c) { return c != null ? c.getNom() : ""; }
            @Override public Categorie fromString(String s) { return null; }
        });
        filtreSousCategorieComboBox.setConverter(new StringConverter<>() {
            @Override public String toString(SousCategorie sc) { return sc != null ? sc.getNom() : ""; }
            @Override public SousCategorie fromString(String s) { return null; }
        });

        filtreTypeComboBox.setOnAction(e -> {
            rechercheField.setDisable(true);
            filtreCategorieComboBox.setDisable(true);
            filtreSousCategorieComboBox.setDisable(true);
            filtreStockComboBox.setDisable(true);
            switch (filtreTypeComboBox.getValue()) {
                case "Nom" -> {
                    rechercheField.setDisable(false);
                    rechercheField.textProperty().addListener((obs, oldVal, newVal) -> appliquerFiltres());
                }
                case "Catégorie" -> filtreCategorieComboBox.setDisable(false);
                case "Sous-Catégorie" -> filtreSousCategorieComboBox.setDisable(false);
                case "Stock" -> filtreStockComboBox.setDisable(false);
            }
        });
    }

    @FXML
    public void appliquerFiltres() {
        String type = filtreTypeComboBox.getValue();
        List<Produit> produits = produitService.getAll();

        if (type == null) {
            tableView.getItems().setAll(produits);
            return;
        }

        switch (type) {
            case "Nom" -> {
                String query = rechercheField.getText().toLowerCase().trim();
                produits = produits.stream().filter(p -> p.getNom().toLowerCase().contains(query)).toList();
            }
            case "Catégorie" -> {
                Categorie selected = filtreCategorieComboBox.getValue();
                if (selected != null)
                    produits = produits.stream().filter(p -> p.getCategorie().getId().equals(selected.getId())).toList();
            }
            case "Sous-Catégorie" -> {
                SousCategorie selected = filtreSousCategorieComboBox.getValue();
                if (selected != null)
                    produits = produits.stream().filter(p -> p.getSousCategorie().getId().equals(selected.getId())).toList();
            }
            case "Stock" -> {
                String stockType = filtreStockComboBox.getValue();
                if ("Stock faible".equals(stockType)) produits = produits.stream().filter(p -> p.getStock() < 5).toList();
                else if ("Stock suffisant".equals(stockType)) produits = produits.stream().filter(p -> p.getStock() >= 5).toList();
            }
        }

        tableView.getItems().setAll(produits);
    }

    @FXML
    public void afficherProduits() {
        tableView.getItems().setAll(produitService.getAll());
    }

    @FXML
    public void ouvrirPopupAjout() {
        openPopup("/front/produit/ajouterproduit.fxml", null);
    }

    @FXML
    public void ouvrirPopupStatistiques() {
        openPopup("/views/produitdash/stat.fxml", null);
    }

    private void openPopup(String fxmlPath, Produit produit) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Parent root = loader.load();

            if (produit != null) {
                AjouterProduitController controller = loader.getController();
                controller.setProduit(produit);
            }

            Stage stage = new Stage();
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setTitle(produit == null ? "Ajouter Produit" : "Modifier Produit");
            stage.setScene(new Scene(root));

            // 🔁 Callback à la fermeture : recharger les produits
            stage.setOnHidden(e -> afficherProduits());

            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    private void envoyerMailStockFaible(Produit produit) {
        try {
            Properties props = new Properties();
            props.put("mail.smtp.auth", "true");
            props.put("mail.smtp.starttls.enable", "true");
            props.put("mail.smtp.host", "smtp.gmail.com");
            props.put("mail.smtp.port", "587");

            Session session = Session.getInstance(props, new Authenticator() {
                protected PasswordAuthentication getPasswordAuthentication() {
                    return new PasswordAuthentication("sarafaleh76@gmail.com", "eaktxbfbmqpnedtw");
                }
            });

            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress("sarafaleh76@gmail.com"));
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse("sarah.faleh@esprit.tn"));
            message.setSubject("Alerte : Stock faible pour le produit " + produit.getNom());

            message.setText("Bonjour,\n\n" +
                    "Le produit suivant présente un stock faible :\n\n" +
                    "Nom : " + produit.getNom() + "\n" +
                    "Stock : " + produit.getStock() + "\n" +
                    "Catégorie : " + produit.getCategorie().getNom() + "\n" +
                    "Sous-catégorie : " + produit.getSousCategorie().getNom() + "\n\n" +
                    "Merci de réapprovisionner ce produit rapidement.\n\n" +
                    "Cordialement,\nL’équipe de gestion d’inventaire.");

            Transport.send(message);
            new Alert(Alert.AlertType.INFORMATION, "Email envoyé avec succès !").showAndWait();

        } catch (Exception e) {
            e.printStackTrace();
            new Alert(Alert.AlertType.ERROR, "Erreur d'envoi d'email !").showAndWait();
        }

    }
    @FXML
    private void ouvrirPopupFavoris() {
        try {
            // Remplace le chemin par le bon chemin dans ton projet
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/produitdash/FavorisDashboard.fxml"));
            Parent root = loader.load();

            Stage stage = new Stage();
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setTitle("Produits Favoris");
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }



}
