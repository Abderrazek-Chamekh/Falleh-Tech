// ✅ Full updated and optimized VerifyAccountController.java
// Now with super smooth animations for StackPane and animated progress bar.

package tn.esprit.controllers;

import javafx.animation.*;
import javafx.fxml.FXML;
import javafx.geometry.NodeOrientation;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.util.Duration;
import net.sourceforge.tess4j.Tesseract;
import net.sourceforge.tess4j.TesseractException;
import tn.esprit.services.SmsService;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class VerifyAccountController {

    @FXML private ProgressBar progressBar;
    @FXML private StackPane stackPane;

    @FXML private VBox cinBox;
    @FXML private VBox smsBox;

    @FXML private ImageView idPreview;
    @FXML private Label statusLabelCin;
    @FXML private Label extractedCinLabel;

    @FXML private TextField phoneNumberField;
    @FXML private TextArea messageField;
    @FXML private Button sendButton;
    @FXML private Label statusLabelSms;

    private File selectedFile;
    private String userCIN = "12725696"; // Example connected user CIN.
    private String generatedCode = null;

    @FXML
    public void initialize() {
        smsBox.setVisible(false);
        smsBox.setTranslateX(800); // Pre-position SMS box off-screen
        progressBar.setProgress(0);

        sendButton.setOnAction(event -> sendSms());
    }

    @FXML
    public void onChooseImage() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Choisir une image");
        fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("Images", "*.png", "*.jpg", "*.jpeg")
        );
        selectedFile = fileChooser.showOpenDialog(null);
        if (selectedFile != null) {
            idPreview.setImage(new Image(selectedFile.toURI().toString()));
            statusLabelCin.setText("📎 Image chargée.");
        }
    }

    @FXML
    public void onVerifyCin() {
        try {
            if (selectedFile == null || !selectedFile.exists()) {
                statusLabelCin.setText("❌ Aucune image sélectionnée.");
                return;
            }

            BufferedImage image = ImageIO.read(selectedFile);

            Tesseract tesseract = new Tesseract();
            tesseract.setDatapath("C:\\Program Files\\Tesseract-OCR");
            tesseract.setLanguage("eng");

            String result = tesseract.doOCR(image);

            // Extract CIN
            Pattern pattern = Pattern.compile("\\b[0-9IOSB]{6,10}\\b");
            Matcher matcher = pattern.matcher(result);

            String bestCIN = null;
            while (matcher.find()) {
                String raw = matcher.group();
                String cleaned = raw.toUpperCase()
                        .replace("I", "1").replace("O", "0")
                        .replace("S", "5").replace("B", "8");

                if (cleaned.matches("\\d{8}")) {
                    bestCIN = cleaned;
                    break;
                }
            }

            extractedCinLabel.setText(bestCIN != null ? bestCIN : "—");

            if (bestCIN != null && bestCIN.equals(userCIN)) {
                statusLabelCin.setText("✅ CIN vérifié avec succès !");
                animateProgressBar(0.5);
                slideToSmsVerification();
            } else {
                statusLabelCin.setText("❌ CIN non valide !");
            }

        } catch (TesseractException | IOException e) {
            statusLabelCin.setText("Erreur OCR: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void slideToSmsVerification() {
        TranslateTransition slideOut = new TranslateTransition(Duration.millis(600), cinBox);
        slideOut.setToX(-800);

        TranslateTransition slideIn = new TranslateTransition(Duration.millis(600), smsBox);
        slideIn.setFromX(800);
        slideIn.setToX(0);

        slideOut.setOnFinished(e -> {
            cinBox.setVisible(false);
            smsBox.setVisible(true);
            slideIn.play();
        });

        slideOut.play();
    }

    private void sendSms() {
        String phoneNumber = phoneNumberField.getText().trim();

        if (phoneNumber.isEmpty()) {
            statusLabelSms.setText("❌ Entrez un numéro !");
            return;
        }

        if (!phoneNumber.startsWith("+")) {
            phoneNumber = "+216" + phoneNumber;
        }

        try {
            generatedCode = String.format("%06d", new Random().nextInt(999999));
            SmsService.send(phoneNumber, "Votre code de vérification: " + generatedCode);

            messageField.setDisable(false);
            messageField.clear();
            messageField.setPromptText("Entrez le code reçu...");

            sendButton.setText("Vérifier Code");
            sendButton.setOnAction(event -> verifyCode());

            statusLabelSms.setText("📩 Code envoyé !");
        } catch (Exception e) {
            e.printStackTrace();
            statusLabelSms.setText("Erreur SMS: " + e.getMessage());
        }
    }

    private void verifyCode() {
        String inputCode = messageField.getText().trim();
        if (inputCode.equals(generatedCode)) {
            statusLabelSms.setText("✅ Téléphone vérifié !");
            animateProgressBar(1.0);
            sendButton.setDisable(true);
        } else {
            statusLabelSms.setText("❌ Code incorrect !");
        }
    }

    private void animateProgressBar(double target) {
        Timeline timeline = new Timeline(
                new KeyFrame(Duration.ZERO, new KeyValue(progressBar.progressProperty(), progressBar.getProgress())),
                new KeyFrame(Duration.seconds(1), new KeyValue(progressBar.progressProperty(), target))
        );
        timeline.play();
    }
}
