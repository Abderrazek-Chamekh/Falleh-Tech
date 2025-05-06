package tn.esprit.entities;


import jakarta.persistence.*;
import javax.validation.constraints.*;   // ✅ This is what YOU should use (based on your dependencies)
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.time.LocalDateTime;

@Entity
public class Candidature {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    // ManyToOne: User (Travailleur)
    @ManyToOne(optional = false)
    @JoinColumn(name = "id_travailleur_id")
    private User idTravailleur;

    // ManyToOne: OffreEmploi
    @ManyToOne(optional = false)
    @JoinColumn(name = "id_offre")
    private OffreEmploi idOffre;

    // OneToOne: Calendar
    @OneToOne(mappedBy = "candidature", cascade = CascadeType.ALL)
    private OuvrierCalendrier calendar;

    // Enum: StatutCandidature
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private StatutCandidature statut;

    @Column(nullable = false)
    private LocalDateTime dateApplied;

    private Integer rating;

    public Candidature() {
        this.dateApplied = LocalDateTime.now();
        this.statut = StatutCandidature.EN_ATTENTE; // Default status
    }

    // Getters and Setters

    public Integer getId() {
        return id;
    }

    public User getIdTravailleur() {
        return idTravailleur;
    }

    public void setIdTravailleur(User idTravailleur) {
        this.idTravailleur = idTravailleur;
    }

    public OffreEmploi getIdOffre() {
        return idOffre;
    }

    public void setIdOffre(OffreEmploi idOffre) {
        this.idOffre = idOffre;
    }

    public OuvrierCalendrier getCalendar() {
        return calendar;
    }

    public void setCalendar(OuvrierCalendrier calendar) {
        this.calendar = calendar;
    }

    public StatutCandidature getStatut() {
        return statut;
    }

    public void setStatut(StatutCandidature statut) {
        this.statut = statut;
    }

    public LocalDateTime getDateApplied() {
        return dateApplied;
    }

    public void setDateApplied(LocalDateTime dateApplied) {
        this.dateApplied = dateApplied;
    }

    public Integer getRating() {
        return rating;
    }

    public void setRating(Integer rating) {
        this.rating = rating;
    }
    public void setId(Integer id) {
        this.id = id;
    }

}
