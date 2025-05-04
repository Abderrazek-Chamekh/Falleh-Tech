package tn.esprit.Controllers.produitfront;

import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.Duration;
import tn.esprit.entities.PanierItem;
import tn.esprit.entities.Produit;
import tn.esprit.services.PanierService;
import tn.esprit.utils.ImageUtils;
import tn.esprit.utils.QRCodeGenerator;

import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

public class PanierController implements Initializable {

    @FXML private TableView<PanierItem> tablePanier;
    @FXML private TableColumn<PanierItem, ImageView> colImage;
    @FXML private TableColumn<PanierItem, String> colNom;
    @FXML private TableColumn<PanierItem, Double> colPrix;
    @FXML private TableColumn<PanierItem, Integer> colQuantite;
    @FXML private TableColumn<PanierItem, Double> colTotal;
    @FXML private TableColumn<PanierItem, Void> colAction;
    @FXML private Label hintLabel;
    @FXML private Label totalLabel;
    @FXML private Label remiseLabel;
    @FXML private Label oldTotalLabel;
    @FXML private Button qrCodeBtn;
    @FXML private TextField codePromoField;
    @FXML private Button appliquerCodeBtn;

    private final PanierService panierService = PanierService.getInstance();
    private boolean remiseAppliquee = false;
    private boolean codePromoApplique = false;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        setupColumns();
        chargerPanier();

        qrCodeBtn.setOnAction(e -> afficherQRCode());
        appliquerCodeBtn.setOnAction(e -> appliquerCodePromo());

        remiseLabel.setVisible(false);
        oldTotalLabel.setVisible(false);

        try {
            Files.writeString(Paths.get("C:/xampp/htdocs/quiz/quiz_success_flag.txt"), "0");
        } catch (IOException e) {
            System.err.println("Erreur initialisation du fichier remise : " + e.getMessage());
        }
    }

    private void setupColumns() {
        colImage.setCellValueFactory(cell -> {
            ImageView img = new ImageView(ImageUtils.chargerDepuisNom(cell.getValue().getProduit().getImage()));
            img.setFitWidth(50);
            img.setFitHeight(50);
            img.setPreserveRatio(true);
            return new javafx.beans.property.SimpleObjectProperty<>(img);
        });

        colNom.setCellValueFactory(cell -> new javafx.beans.property.SimpleStringProperty(cell.getValue().getProduit().getNom()));
        colPrix.setCellValueFactory(cell -> new javafx.beans.property.SimpleObjectProperty<>(cell.getValue().getProduit().getPrix().doubleValue()));
        colQuantite.setCellValueFactory(cellData -> new javafx.beans.property.SimpleIntegerProperty(cellData.getValue().getQuantite()).asObject());

        colQuantite.setCellFactory(col -> new TableCell<>() {
            private final Button minusBtn = new Button("-");
            private final Button plusBtn = new Button("+");
            private final Label qtyLabel = new Label();
            private final HBox box = new HBox(8, minusBtn, qtyLabel, plusBtn);

            {
                box.setAlignment(Pos.CENTER);
                minusBtn.setOnAction(e -> updateQuantity(-1));
                plusBtn.setOnAction(e -> updateQuantity(1));
            }

            private void updateQuantity(int delta) {
                PanierItem item = getTableView().getItems().get(getIndex());
                int newQty = item.getQuantite() + delta;
                if (newQty >= 1) {
                    panierService.ajouterProduit(item.getProduit(), delta);
                    item.setQuantite(newQty);
                    item.updateTotal();
                    tablePanier.refresh();
                    updateTotal();
                }
            }

            @Override
            protected void updateItem(Integer quantite, boolean empty) {
                super.updateItem(quantite, empty);
                if (empty || quantite == null) {
                    setGraphic(null);
                } else {
                    qtyLabel.setText(String.valueOf(quantite));
                    setGraphic(box);
                }
            }
        });

        colTotal.setCellValueFactory(cell -> new javafx.beans.property.SimpleObjectProperty<>(cell.getValue().getTotal()));

        colAction.setCellFactory(param -> new TableCell<>() {
            private final Button deleteBtn = new Button("🗑");

            {
                deleteBtn.setOnAction(event -> {
                    PanierItem item = getTableView().getItems().get(getIndex());
                    panierService.supprimerProduit(item.getProduit().getId());
                    chargerPanier();
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : deleteBtn);
            }
        });
    }

    private void chargerPanier() {
        Map<Produit, Integer> map = panierService.getPanier();
        tablePanier.getItems().setAll(
                map.entrySet().stream()
                        .map(e -> new PanierItem(e.getKey(), e.getValue()))
                        .collect(Collectors.toList())
        );
        updateTotal();
    }

    private void updateTotal() {
        double totalSansRemise = panierService.getTotalSansRemise();
        double total = panierService.getTotal();

        totalLabel.setText(String.format("%.2f DT", total));

        if (panierService.isRemiseAppliquee()) {
            remiseLabel.setVisible(true);
            oldTotalLabel.setVisible(true);
            oldTotalLabel.setText(String.format("%.2f DT", totalSansRemise));
            qrCodeBtn.setVisible(false);
            hintLabel.setVisible(false);
        } else {
            remiseLabel.setVisible(false);
            oldTotalLabel.setVisible(false);

            if (totalSansRemise >= 150) {
                qrCodeBtn.setVisible(true);
                hintLabel.setVisible(false);
            } else {
                qrCodeBtn.setVisible(false);
                hintLabel.setVisible(true);
            }
        }
    }

    private void afficherQRCode() {
        try {
            String quizUrl = "http://172.20.10.2/quiz/quiz.html"; // 👈 ton IP locale pour le quiz
            String filePath = "qr-code.png";
            QRCodeGenerator.generateQRCode(quizUrl, filePath, 250, 250);

            ImageView qrImage = new ImageView("file:" + filePath);
            qrImage.setFitWidth(250);
            qrImage.setFitHeight(250);

            VBox content = new VBox(10, new Label("Scannez ce QR code pour tenter de gagner 15% de remise !"), qrImage);
            content.setAlignment(Pos.CENTER);

            Stage qrStage = new Stage();
            qrStage.setTitle("Quiz Agricole 🎉");
            qrStage.initModality(Modality.APPLICATION_MODAL);
            qrStage.setScene(new Scene(content));
            qrStage.setResizable(false);
            qrStage.showAndWait();

            verifierRemise();
        } catch (Exception e) {
            e.printStackTrace();
            new Alert(Alert.AlertType.ERROR, "Erreur QR Code").show();
        }
    }

    private void verifierRemise() {
        Timeline timeline = new Timeline(new KeyFrame(Duration.seconds(5), event -> {
            try {
                String path = "C:/xampp/htdocs/quiz/quiz_success_flag.txt";
                String content = Files.readString(Paths.get(path)).trim();

                if ("1".equals(content) && !remiseAppliquee) {
                    panierService.appliquerRemise(0.15);
                    remiseAppliquee = true;
                    Files.writeString(Paths.get(path), "0"); // Remettre à zéro après usage
                    updateTotal();

                    Alert alert = new Alert(Alert.AlertType.INFORMATION);
                    alert.setTitle("Remise activée !");
                    alert.setHeaderText("🎉 Félicitations !");
                    alert.setContentText("Vous avez reçu une remise de 15% grâce au quiz !");
                    alert.show();
                }
            } catch (IOException e) {
                System.err.println("Erreur lecture remise : " + e.getMessage());
            }
        }));
        timeline.setCycleCount(Animation.INDEFINITE);
        timeline.play();
    }

    private void appliquerCodePromo() {
        String codeSaisi = codePromoField.getText().trim();

        if (codePromoApplique) {
            new Alert(Alert.AlertType.INFORMATION, "✅ Le code promo a déjà été appliqué !").show();
            return;
        }

        if ("AGRI5".equalsIgnoreCase(codeSaisi)) {
            panierService.appliquerRemise(0.05);
            codePromoApplique = true;
            updateTotal();
            new Alert(Alert.AlertType.INFORMATION, "🎉 Code promo appliqué avec succès ! -5%").show();
        } else {
            new Alert(Alert.AlertType.ERROR, "⛔ Code promo invalide !").show();
        }
    }

    @FXML
    private void ouvrirAjouterCommande(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/Commande/PasserCommande.fxml"));
            Parent root = loader.load();

            Stage stage = new Stage();
            stage.setScene(new Scene(root));
            stage.setTitle("Passer Commande");
            stage.show();



        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
