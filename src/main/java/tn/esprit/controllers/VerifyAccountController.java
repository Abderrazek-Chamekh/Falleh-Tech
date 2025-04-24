package tn.esprit.controllers;

import javafx.fxml.FXML;
import javafx.geometry.NodeOrientation;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.FileChooser;
import net.sourceforge.tess4j.Tesseract;
import net.sourceforge.tess4j.TesseractException;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class VerifyAccountController {

    @FXML private ImageView idPreview;
    @FXML private Label statusLabel;
    @FXML private TextArea ocrOutput;
    @FXML private Label extractedCinLabel;

    private File selectedFile;

    // 🔐 Example connected user CIN — should be passed dynamically
    private final String userCIN = "12725696";

    @FXML
    public void onChooseImage() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Choisir une image");
        fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("Images", "*.png", "*.jpg", "*.jpeg")
        );

        File file = fileChooser.showOpenDialog(null);
        if (file != null) {
            selectedFile = file;
            Image image = new Image(file.toURI().toString());
            idPreview.setImage(image);
            statusLabel.setText("📎 Image chargée : " + file.getName());
            statusLabel.setStyle("-fx-text-fill: #444;");
        }
    }
    @FXML
    public void onVerify() {
        try {
            if (selectedFile == null || !selectedFile.exists()) {
                showAlert("Erreur", "Fichier image introuvable.");
                return;
            }

            // 🖼️ Read the FULL image (not cropped anymore!)
            BufferedImage image = ImageIO.read(selectedFile);

            // 🧠 OCR Setup
            Tesseract tesseract = new Tesseract();
            tesseract.setDatapath("C:\\Program Files\\Tesseract-OCR"); // Adjust this if needed
            tesseract.setLanguage("eng"); // "fra" if your CIN includes French labels

            // 📝 Perform OCR
            String result = tesseract.doOCR(image);

            // 📋 Display OCR result in the app
            ocrOutput.setText(result);
            ocrOutput.setNodeOrientation(NodeOrientation.LEFT_TO_RIGHT);
            ocrOutput.setStyle("-fx-font-family: 'Consolas'; -fx-font-size: 14px;");

            // 🔍 Extract all potential 6–10 digit number strings with OCR noise correction
            Pattern pattern = Pattern.compile("\\b[0-9IOSB]{6,10}\\b");
            Matcher matcher = pattern.matcher(result);

            String bestCIN = null;
            while (matcher.find()) {
                String raw = matcher.group();

                // Fix common OCR misreads
                String cleaned = raw.toUpperCase()
                        .replace("I", "1")
                        .replace("O", "0")
                        .replace("S", "5")
                        .replace("B", "8");

                System.out.println("🔍 Found CIN candidate: " + raw + " → cleaned: " + cleaned);

                // Check if it's a valid 8-digit CIN
                if (cleaned.matches("\\d{8}")) {
                    bestCIN = cleaned;
                    break; // Use the first valid one
                }
            }

            // 👁️ Display extracted CIN in the UI
            extractedCinLabel.setText(bestCIN != null ? bestCIN : "—");

            // ✅ Compare to user CIN
            if (bestCIN != null) {
                if (bestCIN.equals(userCIN)) {
                    statusLabel.setText("✅ Identité vérifiée avec succès !");
                    statusLabel.setStyle("-fx-text-fill: green; -fx-font-weight: bold; -fx-font-size: 16px;");
                } else {
                    statusLabel.setText("❌ CIN ne correspond pas à votre profil !");
                    statusLabel.setStyle("-fx-text-fill: red; -fx-font-weight: bold; -fx-font-size: 16px;");
                }
            } else {
                statusLabel.setText("❌ Aucun CIN détecté.");
                statusLabel.setStyle("-fx-text-fill: orange;");
            }

        } catch (TesseractException | IOException e) {
            e.printStackTrace();
            showAlert("Erreur OCR", "Une erreur est survenue pendant l’analyse.");
        }
    }


    private BufferedImage cropTopRegion(BufferedImage original) {
        int height = original.getHeight() / 4;
        int width = original.getWidth();
        return original.getSubimage(0, 0, width, height);
    }

    private void showAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}
