package tn.esprit.entities;

import jakarta.persistence.*;
import javax.validation.constraints.*;
import java.time.LocalDate;
import java.time.LocalDateTime; // ✅ required for updatedAt
import java.util.ArrayList;
import java.util.List;

@Entity
public class CommandeProduit {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "commande_id")
    private Commande commande;

    @ManyToOne(optional = false)
    @JoinColumn(name = "produit_id")
    private Produit produit;

    @Column(nullable = false)
    @Positive(message = "La quantité doit être un nombre positif")
    private Integer quantite;

    @Column(nullable = false)
    @Positive(message = "Le prix unitaire doit être un nombre positif")
    private Float prixUnitaire;

    @Column(nullable = false)
    @Positive(message = "Le prix total doit être un nombre positif")
    private Float prixTotal;

    // Constructors
    public CommandeProduit() {
    }

    // Getters and Setters

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Commande getCommande() {
        return commande;
    }

    public void setCommande(Commande commande) {
        this.commande = commande;
    }

    public Produit getProduit() {
        return produit;
    }

    public void setProduit(Produit produit) {
        this.produit = produit;
    }

    public Integer getQuantite() {
        return quantite;
    }

    public void setQuantite(Integer quantite) {
        this.quantite = quantite;
    }

    public Float getPrixUnitaire() {
        return prixUnitaire;
    }

    public void setPrixUnitaire(Float prixUnitaire) {
        this.prixUnitaire = prixUnitaire;
    }

    public Float getPrixTotal() {
        return prixTotal;
    }

    public void setPrixTotal(Float prixTotal) {
        this.prixTotal = prixTotal;
    }

    // Optional: dynamic total calculation
    public Float calculateTotal() {
        return quantite != null && prixUnitaire != null ? quantite * prixUnitaire : 0f;
    }
}
