package tn.esprit.entities;

import jakarta.persistence.*;
import javax.validation.constraints.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
public class OffreEmploi {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(length = 100, nullable = false)
    @NotBlank(message = "Le titre est obligatoire.")
    @Size(min = 3, max = 100, message = "Le titre doit contenir entre {min} et {max} caractères.")
    private String titre;

    @Lob
    @NotBlank(message = "La description est obligatoire.")
    @Size(min = 10, message = "La description doit contenir au moins {min} caractères.")
    private String description;

    @Column(nullable = false)
    @NotNull(message = "Le salaire est obligatoire.")
    @Positive(message = "Le salaire doit être un nombre positif.")
    @DecimalMin(value = "10.0", message = "Le salaire doit être au moins {value} DT.")
    @DecimalMax(value = "10000.0", message = "Le salaire doit être au plus {value} DT.")
    private Float salaire;

    @Column(length = 255, nullable = false)
    @NotBlank(message = "Le lieu est obligatoire.")
    @Size(max = 255, message = "Le lieu ne peut pas dépasser {max} caractères.")
    private String lieu;

    @Column(nullable = false)
    @NotNull(message = "La date de début est obligatoire.")
    private LocalDate startDate;

    @Column(nullable = false)
    @NotNull(message = "La date d'expiration est obligatoire.")
    private LocalDate dateExpiration;

    @ManyToOne(optional = false, cascade = CascadeType.ALL)
    @JoinColumn(name = "id_employeur_id", nullable = false)
    private User idEmployeur;

    @OneToMany(mappedBy = "offre", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Candidature> candidatures = new ArrayList<>();

    public OffreEmploi() {
        this.startDate = LocalDate.now();
        this.dateExpiration = LocalDate.now().plusMonths(1);
    }

    // Preferred Constructor
    public OffreEmploi(String titre, String lieu, String dateDebut, String dateFin, String description, User employeur) {
        this.titre = titre;
        this.lieu = lieu;
        this.startDate = LocalDate.parse(dateDebut);
        this.dateExpiration = LocalDate.parse(dateFin);
        this.description = description;
        this.salaire = 100f;
        this.idEmployeur = employeur;
    }


    public Integer getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getTitre() {
        return titre;
    }

    public void setTitre(String titre) {
        this.titre = titre;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Float getSalaire() {
        return salaire;
    }

    public void setSalaire(Float salaire) {
        this.salaire = salaire;
    }

    public String getLieu() {
        return lieu;
    }

    public void setLieu(String lieu) {
        this.lieu = lieu;
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public void setStartDate(LocalDate startDate) {
        this.startDate = (startDate != null) ? startDate : LocalDate.now();
    }

    public LocalDate getDateExpiration() {
        return dateExpiration;
    }

    public void setDateExpiration(LocalDate dateExpiration) {
        this.dateExpiration = (dateExpiration != null) ? dateExpiration : LocalDate.now().plusMonths(1);
    }

    public User getIdEmployeur() {
        return idEmployeur;
    }

    public void setIdEmployeur(User idEmployeur) {
        this.idEmployeur = idEmployeur;
    }

    public List<Candidature> getCandidatures() {
        return candidatures;
    }

    public void addCandidature(Candidature candidature) {
        if (!this.candidatures.contains(candidature)) {
            this.candidatures.add(candidature);
            candidature.setOffre(this);
        }
    }

    public void removeCandidature(Candidature candidature) {
        if (this.candidatures.remove(candidature)) {
            if (candidature.getOffre() == this) {
                candidature.setOffre(null);
            }
        }
    }

    @Override
    public String toString() {
        return this.titre;
    }
    public OffreEmploi(String titre, String description, Float salaire, String lieu) {
        this.titre = titre;
        this.description = description;
        this.salaire = salaire;
        this.lieu = lieu;
    }



}