package tn.esprit.controllers.ouvrier;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.util.Duration;
import tn.esprit.entities.Conversation;
import tn.esprit.entities.Message;
import tn.esprit.entities.User;
import tn.esprit.services.ServiceConversation;
import tn.esprit.services.ServiceMessage;
import tn.esprit.services.ServiceUser;

import java.net.URL;
import java.time.LocalDateTime;
import java.util.List;
import java.util.ResourceBundle;

public class ChatPopupController implements Initializable {

    @FXML private Label lblChatWith;
    @FXML private VBox chatArea;
    @FXML private TextField messageField;
    @FXML private ScrollPane chatScroll;
    @FXML private Button sendButton;

    private User currentUser;
    private User chatPartner;
    private Conversation currentConversation;
    private int lastLoadedMessageId = -1;

    private final ServiceConversation convService = new ServiceConversation();
    private final ServiceMessage msgService = new ServiceMessage();
    private final ServiceUser userService = new ServiceUser();

    private int lastMessageCount = 0;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        Timeline timeline = new Timeline(new KeyFrame(Duration.seconds(3), e -> refreshMessages()));
        timeline.setCycleCount(Timeline.INDEFINITE);
        timeline.play();

        sendButton.setOnAction(e -> sendMessage());
    }

    public void setChatSession(User currentUser, int chatPartnerId) {
        this.currentUser = currentUser;

        try {
            this.chatPartner = userService.findById(chatPartnerId);
            if (chatPartner == null) {
                throw new IllegalArgumentException("User with ID " + chatPartnerId + " does not exist in the database.");
            }

            System.out.println("✅ Setting up chat between " + currentUser.getId() + " and " + chatPartner.getId());
            this.lblChatWith.setText("💬 Discussion avec " + chatPartner.getName());

            int conversationId = convService.getOrCreateConversation(currentUser.getId(), chatPartner.getId());
            this.currentConversation = new Conversation();
            this.currentConversation.setId(conversationId);

            refreshMessages(); // fetch initial messages
        } catch (Exception e) {
            System.err.println("🔥 Failed to create or retrieve conversation: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void sendMessage() {
        String content = messageField.getText().trim();
        if (content.isEmpty() || currentConversation == null || chatPartner == null) return;

        Message msg = new Message();
        msg.setSenderId(currentUser.getId());
        msg.setReceiverId(chatPartner.getId());
        msg.setConversationId(currentConversation.getId());
        msg.setContent(content);
        msg.setCreatedAt(LocalDateTime.now());
        msg.setRead(false);

        msgService.sendMessage(msg);
        addMessageToUI(msg);

        messageField.clear();
        Platform.runLater(() -> chatScroll.setVvalue(1.0));
        lastMessageCount++;
    }

    private void refreshMessages() {
        if (chatPartner == null || currentConversation == null) return;

        List<Message> messages = msgService.getMessagesBetweenUsers(currentUser.getId(), chatPartner.getId());

        for (Message msg : messages) {
            if (msg.getId() > lastLoadedMessageId) {
                addMessageToUI(msg); // 👈 Only add new messages
                lastLoadedMessageId = msg.getId(); // 🧠 Track last
            }
        }

        Platform.runLater(() -> chatScroll.setVvalue(1.0));
    }

    private void addMessageToUI(Message msg) {
        Label label = new Label(msg.getContent());
        label.setWrapText(true);
        label.setMaxWidth(300);
        label.getStyleClass().add("message-bubble");

        HBox msgBox = new HBox(label);
        msgBox.setFillHeight(true);

        if (msg.getSenderId() == currentUser.getId()) {
            msgBox.getStyleClass().add("hbox-right");
            label.getStyleClass().add("me");
        } else {
            msgBox.getStyleClass().add("hbox-left");
            label.getStyleClass().add("them");
        }

        chatArea.getChildren().add(msgBox);
    }
}
