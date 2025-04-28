package tn.esprit.entities;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Entity
public class SousCategorie {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(length = 100)
    @NotBlank(message = "Le nom de la sous-catégorie ne peut pas être vide.")
    @Size(max = 100, message = "Le nom de la sous-catégorie ne peut dépasser 100 caractères.")
    private String nom;

    // Nom du fichier image stocké en base
    @Column(length = 255)
    private String image;

    // Fichier image uploadé (non stocké en base)
    @Transient
    private MultipartFile imageFile;

    @ManyToOne
    @JoinColumn(name = "categorie_id", nullable = false)
    private Categorie categorie;

    @OneToMany(mappedBy = "sousCategorie")
    private List<Produit> produits;

    // Constructeurs

    public SousCategorie() {}

    public SousCategorie(String nom, String image, Categorie categorie) {
        this.nom = nom;
        this.image = image;
        this.categorie = categorie;
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

    public Categorie getCategorie() {
        return categorie;
    }

    public void setCategorie(Categorie categorie) {
        this.categorie = categorie;
    }

    public List<Produit> getProduits() {
        return produits;
    }

    public void setProduits(List<Produit> produits) {
        this.produits = produits;
    }

    @Override
    public String toString() {
        return nom;
    }
}
