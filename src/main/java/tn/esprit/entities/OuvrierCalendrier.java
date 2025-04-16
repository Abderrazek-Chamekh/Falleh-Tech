package tn.esprit.entities;


import jakarta.persistence.*;
import javax.validation.constraints.*;   // ✅ This is what YOU should use (based on your dependencies)
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.time.LocalDateTime;

@Entity
public class OuvrierCalendrier {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    // ManyToOne: Ouvrier (User)
    @ManyToOne(optional = false)
    @JoinColumn(name = "ouvrier_id")
    private User ouvrier;

    // OneToOne: Candidature
    @OneToOne(optional = false, cascade = CascadeType.ALL)
    @JoinColumn(name = "candidature_id", nullable = false)
    private Candidature candidature;

    // Start & End Dates
    @Column(nullable = false)
    private LocalDateTime startDate;

    @Column(nullable = false)
    private LocalDateTime endDate;

    // Status with default value
    @Column(length = 50, nullable = false)
    private String status = "en_attente"; // Default value

    // Constructors
    public OuvrierCalendrier() {}

    // Getters and Setters
    public Integer getId() {
        return id;
    }

    public User getOuvrier() {
        return ouvrier;
    }

    public void setOuvrier(User ouvrier) {
        this.ouvrier = ouvrier;
    }

    public Candidature getCandidature() {
        return candidature;
    }

    public void setCandidature(Candidature candidature) {
        this.candidature = candidature;
    }

    public LocalDateTime getStartDate() {
        return startDate;
    }

    public void setStartDate(LocalDateTime startDate) {
        this.startDate = startDate;
    }

    public LocalDateTime getEndDate() {
        return endDate;
    }

    public void setEndDate(LocalDateTime endDate) {
        this.endDate = endDate;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
