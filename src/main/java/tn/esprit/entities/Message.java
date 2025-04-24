package tn.esprit.entities;

import java.time.LocalDateTime;

public class Message {
    private int id;
    private int senderId;
    private int receiverId;
    private int conversationId;
    private String content;
    private LocalDateTime createdAt;
    private boolean isRead;

    public Message() {
        this.createdAt = LocalDateTime.now();
        this.isRead = false;
    }

    public Message(int senderId, int receiverId, int conversationId, String content) {
        this.senderId = senderId;
        this.receiverId = receiverId;
        this.conversationId = conversationId;
        this.content = content;
        this.createdAt = LocalDateTime.now();
        this.isRead = false;
    }

    // Getters & Setters

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getSenderId() {
        return senderId;
    }

    public void setSenderId(int senderId) {
        this.senderId = senderId;
    }

    public int getReceiverId() {
        return receiverId;
    }

    public void setReceiverId(int receiverId) {
        this.receiverId = receiverId;
    }

    public int getConversationId() {
        return conversationId;
    }

    public void setConversationId(int conversationId) {
        this.conversationId = conversationId;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public boolean isRead() {
        return isRead;
    }

    public void setRead(boolean read) {
        isRead = read;
    }
}
