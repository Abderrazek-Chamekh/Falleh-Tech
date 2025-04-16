package tn.esprit.entities;


import jakarta.persistence.*;
import javax.validation.constraints.*;   // ✅ This is what YOU should use (based on your dependencies)
import java.time.LocalDateTime; // ✅ required for updatedAt
import java.util.ArrayList;
import java.util.List;

@Entity
public class Livraison {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @OneToOne(optional = false)
    @JoinColumn(name = "commande_id")
    @NotNull(message = "La commande associée est obligatoire")
    private Commande commande;

    @Column(length = 50, nullable = false)
    @NotBlank(message = "Le statut de la livraison est obligatoire")
    @Pattern(
            regexp = "En Cours|Livrée|Annulée",
            message = "Statut de livraison invalide"
    )
    private String statut;

    @Column(length = 100, nullable = false)
    @NotBlank(message = "Le transporteur est obligatoire")
    @Size(
            min = 3,
            max = 100,
            message = "Le nom du transporteur doit contenir entre 3 et 100 caractères"
    )
    private String transporteur;

    @Column(length = 20, nullable = false)
    @NotBlank(message = "Le numéro de téléphone du transporteur est obligatoire")
    @Pattern(
            regexp = "^\\+?[0-9\\s\\-]+$",
            message = "Le numéro de téléphone est invalide"
    )
    private String numTelTransporteur;

    @Column
    private LocalDateTime dateLivraison;

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

    public String getStatut() {
        return statut;
    }

    public void setStatut(String statut) {
        this.statut = statut;
    }

    public String getTransporteur() {
        return transporteur;
    }

    public void setTransporteur(String transporteur) {
        this.transporteur = transporteur;
    }

    public String getNumTelTransporteur() {
        return numTelTransporteur;
    }

    public void setNumTelTransporteur(String numTelTransporteur) {
        this.numTelTransporteur = numTelTransporteur;
    }

    public LocalDateTime getDateLivraison() {
        return dateLivraison;
    }

    public void setDateLivraison(LocalDateTime dateLivraison) {
        this.dateLivraison = dateLivraison;
    }
}
