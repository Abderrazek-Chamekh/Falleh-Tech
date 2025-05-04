package tn.esprit.entities;

import jakarta.persistence.*;
import javax.validation.constraints.*;
import java.time.LocalDate;
import java.time.LocalDateTime; // ✅ required for updatedAt
import java.util.ArrayList;
import java.util.List;


@Entity
public class Commande {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "user_id")
    private User user;

    @Column(nullable = false)
    @NotNull(message = "La date de création est obligatoire")
    private LocalDateTime dateCreation;

    @Column(nullable = false)
    @NotNull(message = "Le total est obligatoire")
    @Positive(message = "Le total doit être un nombre positif")
    private Float total;

    @Column(length = 255)
    @Pattern(regexp = "En Attente|Confirmée|Annulée|Remboursée", message = "Statut invalide")
    private String status;

    @Column(length = 255)
    @NotBlank(message = "L'adresse de livraison ne peut pas être vide")
    private String adresseLivraison;

    @Column(length = 50, nullable = false)
    @NotBlank(message = "Le mode de paiement est obligatoire")
    @Pattern(regexp = "Espèces|Carte_Bancaire|e_DINAR", message = "Mode de paiement invalide")
    private String modePaiement;

    @Column
    private LocalDateTime datePaiement;

    @Column(length = 50, nullable = false)
    @NotBlank(message = "Le statut du paiement est obligatoire")
    @Pattern(regexp = "En Attente|Payé|Échoué|Remboursé", message = "Statut de paiement invalide")
    private String statusPaiement;

    @OneToOne(mappedBy = "commande", cascade = CascadeType.ALL)
    private Livraison livraison;

    @OneToMany(mappedBy = "commande", cascade = {CascadeType.PERSIST, CascadeType.REMOVE})
    private List<CommandeProduit> commandeProduits = new ArrayList<>();

    // Constructors

    public Commande() {}

    // Getters and Setters

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public LocalDateTime getDateCreation() {
        return dateCreation;
    }

    public void setDateCreation(LocalDateTime dateCreation) {
        this.dateCreation = dateCreation;
    }

    public Float getTotal() {
        return total;
    }

    public void setTotal(Float total) {
        this.total = total;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getAdresseLivraison() {
        return adresseLivraison;
    }

    public void setAdresseLivraison(String adresseLivraison) {
        this.adresseLivraison = adresseLivraison;
    }

    public String getModePaiement() {
        return modePaiement;
    }

    public void setModePaiement(String modePaiement) {
        this.modePaiement = modePaiement;
    }

    public LocalDateTime getDatePaiement() {
        return datePaiement;
    }

    public void setDatePaiement(LocalDateTime datePaiement) {
        this.datePaiement = datePaiement;
    }

    public String getStatusPaiement() {
        return statusPaiement;
    }

    public void setStatusPaiement(String statusPaiement) {
        this.statusPaiement = statusPaiement;
    }

    public Livraison getLivraison() {
        return livraison;
    }

    public void setLivraison(Livraison livraison) {
        this.livraison = livraison;
        if (livraison.getCommande() != this) {
            livraison.setCommande(this);
        }
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public List<CommandeProduit> getCommandeProduits() {
        return commandeProduits;
    }

    public void setCommandeProduits(List<CommandeProduit> commandeProduits) {
        this.commandeProduits = commandeProduits;
    }

    public void addCommandeProduit(CommandeProduit produit) {
        if (!commandeProduits.contains(produit)) {
            commandeProduits.add(produit);
            produit.setCommande(this);
        }
    }

    public void removeCommandeProduit(CommandeProduit produit) {
        if (commandeProduits.remove(produit)) {
            if (produit.getCommande() == this) {
                produit.setCommande(null);
            }
        }
    }
}
