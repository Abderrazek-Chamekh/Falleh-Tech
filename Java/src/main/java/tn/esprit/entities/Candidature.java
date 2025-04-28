package tn.esprit.entities;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
public class Candidature {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "user_id") // Travailleur
    private User user;

    @ManyToOne(optional = false)
    @JoinColumn(name = "offre_id")
    private OffreEmploi offre;

    @OneToOne(mappedBy = "candidature", cascade = CascadeType.ALL)
    private OuvrierCalendrier calendar;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private StatutCandidature statut;

    @Column(nullable = false)
    private LocalDateTime dateApplied;

    private Integer rating;

    public Candidature() {
        this.dateApplied = LocalDateTime.now();
        this.statut = StatutCandidature.EN_ATTENTE;
    }

    // === Getters and Setters ===

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public OffreEmploi getOffre() {
        return offre;
    }

    public void setOffre(OffreEmploi offre) {
        this.offre = offre;
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

    // Convenience accessors (for UI)
    public String getNom() {
        return user != null ? user.getName() : "Inconnu";
    }

    public String getEmail() {
        return user != null ? user.getEmail() : "Inconnu";
    }

    public LocalDate getDateCandidature() {
        return dateApplied != null ? dateApplied.toLocalDate() : null;
    }

    public void setDateCandidature(LocalDate date) {
        this.dateApplied = date.atStartOfDay();
    }

}
