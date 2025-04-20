package tn.esprit.entities;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Entity
public class Categorie {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(length = 100)
    @NotBlank(message = "Le nom de la catégorie ne peut pas être vide.")
    @Size(max = 100, message = "Le nom de la catégorie ne peut dépasser 100 caractères.")
    private String nom;

    // Nom du fichier image stocké en base
    @Column(length = 255)
    private String image;

    // Fichier image uploadé (non stocké en base)
    @Transient
    private MultipartFile imageFile;

    @OneToMany(mappedBy = "categorie", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<SousCategorie> sousCategories;

    // Constructeurs

    public Categorie() {}

    public Categorie(String nom, String image) {
        this.nom = nom;
        this.image = image;
    }

    // Getters et setters

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getNom() {
        return nom;
    }

    public void setNom(String nom) {
        this.nom = nom;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public MultipartFile getImageFile() {
        return imageFile;
    }

    public void setImageFile(MultipartFile imageFile) {
        this.imageFile = imageFile;
    }

    public List<SousCategorie> getSousCategories() {
        return sousCategories;
    }

    public void setSousCategories(List<SousCategorie> sousCategories) {
        this.sousCategories = sousCategories;
    }

    @Override
    public String toString() {
        return nom;
    }
}