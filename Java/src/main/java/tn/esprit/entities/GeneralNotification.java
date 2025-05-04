package tn.esprit.entities;

import java.time.LocalDateTime;

public class GeneralNotification {
    private int id;
    private int userId;
    private String message;
    private LocalDateTime createdAt;
    private boolean seen;

    public GeneralNotification() {
        this.createdAt = LocalDateTime.now();
        this.seen = false;
    }

    public GeneralNotification(int userId, String message) {
        this.userId = userId;
        this.message = message;
        this.createdAt = LocalDateTime.now();
        this.seen = false;
    }

    // ✅ Getters
    public int getId() {
        return id;
    }

    public int getUserId() {
        return userId;
    }

    public String getMessage() {
        return message;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public boolean isSeen() {
        return seen;
    }

    // ✅ Setters
    public void setId(int id) {
        this.id = id;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public void setSeen(boolean seen) {
        this.seen = seen;
    }

}
