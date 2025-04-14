package tn.esprit.entities;

import jakarta.persistence.*;
import javax.validation.constraints.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
public class Post {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(length = 255, nullable = false)
    @NotBlank(message = "Le titre ne peut pas être vide.")
    @Size(min = 5, max = 255, message = "Le titre doit contenir entre 5 et 255 caractères.")
    @Pattern(regexp = "\\D+", message = "Le titre ne peut pas être composé uniquement de chiffres.")
    private String titre;

    @Column(length = 255, nullable = false)
    @NotBlank(message = "Le contenu ne peut pas être vide.")
    @Size(min = 10, max = 255, message = "Le contenu doit contenir entre 10 et 255 caractères.")
    @Pattern(regexp = "\\D+", message = "Le contenu ne peut pas être composé uniquement de chiffres.")
    private String contenu;

    @Column(nullable = false)
    private LocalDate date;

    @Column(length = 255)
    private String image; // Stores the file name or relative path to the image

    @Column(length = 255, nullable = false)
    @NotBlank(message = "La catégorie ne peut pas être vide.")
    private String category;

    @ManyToOne(optional = false)
    @JoinColumn(name = "user_id")
    private User user;

    @OneToMany(mappedBy = "post", cascade = CascadeType.REMOVE)
    private List<Comment> comments = new ArrayList<>();

    @OneToMany(mappedBy = "post", cascade = CascadeType.REMOVE)
    private List<Like> likes = new ArrayList<>();

    @OneToMany(mappedBy = "post")
    private List<Notification> notifications = new ArrayList<>();

    @PrePersist
    public void onCreate() {
        this.date = LocalDate.now();
    }

    // Getters and Setters

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getTitre() {
        return titre;
    }

    public void setTitre(String titre) {
        this.titre = titre;
    }

    public String getContenu() {
        return contenu;
    }

    public void setContenu(String contenu) {
        this.contenu = contenu;
    }

    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public List<Comment> getComments() {
        return comments;
    }

    public void setComments(List<Comment> comments) {
        this.comments = comments;
    }

    public List<Like> getLikes() {
        return likes;
    }

    public void setLikes(List<Like> likes) {
        this.likes = likes;
    }

    public List<Notification> getNotifications() {
        return notifications;
    }

    public void setNotifications(List<Notification> notifications) {
        this.notifications = notifications;
    }

    @Override
    public String toString() {
        return titre;
    }
}
