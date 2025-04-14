package tn.esprit.entities;

import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
public class Categorie {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(length = 100, nullable = false)
    private String nom;

    @OneToMany(mappedBy = "categorie", cascade = {CascadeType.PERSIST, CascadeType.REMOVE})
    private List<SousCategorie> sousCategories = new ArrayList<>();

    public Categorie() {
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

    public List<SousCategorie> getSousCategories() {
        return sousCategories;
    }

    public void setSousCategories(List<SousCategorie> sousCategories) {
        this.sousCategories = sousCategories;
    }

    @Override
    public String toString() {
        return nom != null ? nom : "";
    }
}
