package tn.esprit.controllers;

import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.util.Duration;
import tn.esprit.entities.User;
import tn.esprit.entities.Conversation;
import tn.esprit.entities.Message;
import tn.esprit.services.ServiceConversation;
import tn.esprit.services.ServiceMessage;

import java.net.URL;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

public class MessagingController implements Initializable {
    @FXML private TextField searchField;
    @FXML private VBox userListContainer;
    @FXML private VBox chatArea;
    @FXML private TextField messageInput;
    @FXML private ScrollPane chatScroll;

    private Button selectedUserButton;
    private User currentUser;
    private User chatPartner;
    private Conversation currentConversation;
    private List<User> fullUserList = new ArrayList<>();

    private int lastLoadedMessageId = -1;

    private final ServiceConversation convService = new ServiceConversation();
    private final ServiceMessage msgService = new ServiceMessage();

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        searchField.textProperty().addListener((obs, oldText, newText) -> {
            String search = newText.trim().toLowerCase();
            List<User> filtered = fullUserList.stream()
                    .filter(u -> u.getName().toLowerCase().contains(search))
                    .toList();
            updateDisplayedUserList(filtered);
        });

        Timeline timeline = new Timeline(new KeyFrame(Duration.seconds(3), e -> refreshMessages()));
        timeline.setCycleCount(Animation.INDEFINITE);
        timeline.play();
    }

    public void setCurrentUser(User user) {
        this.currentUser = user;
        loadUsers();
    }

    private void loadUsers() {
        fullUserList = convService.getAllUsersExcept(currentUser.getId());
        updateDisplayedUserList(fullUserList);
    }

    private void updateDisplayedUserList(List<User> users) {
        userListContainer.getChildren().clear();

        for (User user : users) {
            Button btn = new Button(user.getName());
            btn.getStyleClass().add("user-button");
            btn.setMaxWidth(Double.MAX_VALUE);

            btn.setOnAction(e -> {
                highlightSelectedUser(btn);
                openConversationWith(user);
            });

            userListContainer.getChildren().add(btn);
        }
    }

    private void highlightSelectedUser(Button newSelectedBtn) {
        if (selectedUserButton != null) {
            selectedUserButton.getStyleClass().remove("selected-user");
        }
        selectedUserButton = newSelectedBtn;
        if (!selectedUserButton.getStyleClass().contains("selected-user")) {
            selectedUserButton.getStyleClass().add("selected-user");
        }
    }

    private void openConversationWith(User contact) {
        this.chatPartner = contact;
        chatArea.getChildren().clear();
        lastLoadedMessageId = -1;

        int conversationId = convService.getOrCreateConversation(currentUser.getId(), contact.getId());
        this.currentConversation = new Conversation();
        currentConversation.setId(conversationId);

        msgService.markMessagesAsRead(chatPartner.getId(), currentUser.getId());

        List<Message> messages = msgService.getMessagesBetweenUsers(currentUser.getId(), chatPartner.getId());
        for (Message msg : messages) {
            addMessageToUI(msg);
            lastLoadedMessageId = Math.max(lastLoadedMessageId, msg.getId());
        }

        Platform.runLater(() -> chatScroll.setVvalue(1.0));
    }

    @FXML
    private void sendMessage() {
        String content = messageInput.getText().trim();
        if (content.isEmpty() || currentConversation == null || chatPartner == null) return;

        Message msg = new Message();
        msg.setSenderId(currentUser.getId());
        msg.setReceiverId(chatPartner.getId());
        msg.setConversationId(currentConversation.getId());
        msg.setContent(content);
        msg.setCreatedAt(LocalDateTime.now());
        msg.setRead(false);

        int generatedId = msgService.sendMessage(msg); // Must return real DB id
        if (generatedId != -1) {
            msg.setId(generatedId);
            addMessageToUI(msg);
            lastLoadedMessageId = generatedId;
        }

        messageInput.clear();
        Platform.runLater(() -> chatScroll.setVvalue(1.0));
    }

    private void refreshMessages() {
        if (chatPartner == null || currentConversation == null) return;

        List<Message> messages = msgService.getMessagesBetweenUsers(currentUser.getId(), chatPartner.getId());

        for (Message msg : messages) {
            if (msg.getId() > lastLoadedMessageId) {
                addMessageToUI(msg);
                lastLoadedMessageId = msg.getId();
            }
        }

        msgService.markMessagesAsRead(chatPartner.getId(), currentUser.getId());
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

    public void setChatSession(User currentUser, User chatPartner) {
        this.currentUser = currentUser;
        this.chatPartner = chatPartner;
        openConversationWith(chatPartner);
    }
}
