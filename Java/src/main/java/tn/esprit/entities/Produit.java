package tn.esprit.entities;

import jakarta.persistence.*;
import javax.validation.constraints.*;
import java.time.LocalDate;
import java.time.LocalDateTime; // ✅ required for updatedAt
import java.util.ArrayList;
import java.util.List;

@Entity
public class Produit {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(length = 100, nullable = false)
    @NotBlank(message = "Le nom du produit est obligatoire.")
    @Size(min = 3, max = 100, message = "Le nom doit contenir entre 3 et 100 caractères.")
    @Pattern(regexp = "^[a-zA-Z0-9À-ÿ\\s\\-]+$", message = "Le nom ne doit contenir que des lettres, chiffres, espaces et tirets.")
    private String nom;

    @Column(precision = 10, scale = 2, nullable = false)
    @Min(value = 1, message = "Le prix doit être supérieur à 1 DT.")
    private Float prix;

    @ManyToOne(optional = false)
    @JoinColumn(name = "user_id")
    private User user;

    @Column(columnDefinition = "TEXT", nullable = false)
    @NotBlank(message = "La description est obligatoire.")
    @Size(min = 10, message = "La description doit contenir au moins 10 caractères.")
    @Pattern(regexp = "^[a-zA-Z0-9À-ÿ\\s.,'!?-]+$", message = "La description ne doit contenir que des lettres, chiffres, ponctuation et espaces.")
    private String description;

    @ManyToOne(optional = false)
    @JoinColumn(name = "categorie_id")
    private Categorie categorie;

    @ManyToOne(optional = false)
    @JoinColumn(name = "sous_categorie_id")
    private SousCategorie sousCategorie;

    @Column(length = 255)
    private String image; // Stores filename or path

    @Column(nullable = false)
    private Integer stock;

    @Column(nullable = false)
    private boolean isFavorite = false;

    @Column
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        this.updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    // Getters and Setters

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getNom() {
        return nom;
    }

    public void setNom(String nom) {
        this.nom = nom;
    }

    public Float getPrix() {
        return prix;
    }

    public void setPrix(Float prix) {
        this.prix = prix;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Categorie getCategorie() {
        return categorie;
    }

    public void setCategorie(Categorie categorie) {
        this.categorie = categorie;
    }

    public SousCategorie getSousCategorie() {
        return sousCategorie;
    }

    public void setSousCategorie(SousCategorie sousCategorie) {
        this.sousCategorie = sousCategorie;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public Integer getStock() {
        return stock;
    }

    public void setStock(Integer stock) {
        this.stock = stock;
    }

    public boolean isFavorite() {
        return isFavorite;
    }

    public void setFavorite(boolean favorite) {
        isFavorite = favorite;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}
