package tn.esprit.controllers;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import tn.esprit.services.SmsService;

import java.util.Random;

public class SendSmsController {

    @FXML
    private TextField phoneNumberField;

    @FXML
    private TextArea messageField; // We won't use it for typing manually anymore

    @FXML
    private Button sendButton;

    @FXML
    private Label statusLabel;

    private String generatedCode = null; // Store the 6-digit code to verify

    @FXML
    public void initialize() {
        sendButton.setOnAction(event -> sendSms());
    }

    private void sendSms() {
        String phoneNumber = phoneNumberField.getText().trim();

        if (phoneNumber.isEmpty()) {
            statusLabel.setText("❌ Please enter your phone number.");
            return;
        }

        // 📌 Auto-add +216 if needed
        if (!phoneNumber.startsWith("+")) {
            phoneNumber = "+216" + phoneNumber;
        }

        try {
            // Step 1: Generate random 6-digit code
            generatedCode = String.format("%06d", new Random().nextInt(999999));

            // Step 2: Send the code via SMS
            SmsService.send(phoneNumber, "Your verification code is: " + generatedCode);

            // Step 3: Update the messageField to ask user to type the code
            messageField.clear();
            messageField.setPromptText("Enter the code you received...");
            messageField.setDisable(false); // Enable if was disabled

            // Step 4: Update button action to verify now
            sendButton.setText("Verify Code");
            sendButton.setOnAction(event -> verifyCode());

            statusLabel.setText("📩 Code sent! Check your SMS.");
        } catch (Exception e) {
            e.printStackTrace();
            statusLabel.setText("❌ Failed to send SMS: " + e.getMessage());
        }
    }

    private void verifyCode() {
        String userInputCode = messageField.getText().trim();

        if (userInputCode.equals(generatedCode)) {
            statusLabel.setText("✅ Verification successful!");
            sendButton.setDisable(true); // Optional: Disable the button after success
        } else {
            statusLabel.setText("❌ Wrong code. Please try again.");
        }
    }
}
