package tn.esprit.entities;

import jakarta.persistence.*;

@Entity
public class Favoris {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "produit_id", nullable = false, foreignKey = @ForeignKey(name = "fk_favoris_produit"))
    private Produit produit;

    @Column(nullable = false)
    private Integer userId;

    // Constructors
    public Favoris() {}

    public Favoris(Produit produit, Integer userId) {
        this.produit = produit;
        this.userId = userId;
    }

    // Getters and setters

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Produit getProduit() {
        return produit;
    }

    public void setProduit(Produit produit) {
        this.produit = produit;
    }

    public Integer getUserId() {
        return userId;
    }

    public void setUserId(Integer userId) {
        this.userId = userId;
    }

    public boolean isFavorite() {
        return isFavorite;
    }

    public void setFavorite(boolean favorite) {
        isFavorite = favorite;
    }
}
