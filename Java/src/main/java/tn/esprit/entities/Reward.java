package tn.esprit.entities;

import java.time.LocalDateTime;

public class Reward {
    private Long id;
    private Long userId;
    private String type;    // "pdf" ou "code_promo"
    private String value;   // par exemple "recette1.pdf" ou "AGRI5OFF"
    private boolean claimed;
    private LocalDateTime createdAt;

    public Reward() {}

    public Reward(Long userId, String type, String value) {
        this.userId = userId;
        this.type = type;
        this.value = value;
        this.claimed = false;
        this.createdAt = LocalDateTime.now();
    }

    // Getters et Setters
    public Long getId() {
        return id;
    }
    public void setId(Long id) {
        this.id = id;
    }

    public Long getUserId() {
        return userId;
    }
    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getType() {
        return type;
    }
    public void setType(String type) {
        this.type = type;
    }

    public String getValue() {
        return value;
    }
    public void setValue(String value) {
        this.value = value;
    }

    public boolean isClaimed() {
        return claimed;
    }
    public void setClaimed(boolean claimed) {
        this.claimed = claimed;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
