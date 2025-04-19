package tn.esprit.entities;

import jakarta.persistence.*;
import javax.validation.constraints.*;   // ✅ This is what YOU should use (based on your dependencies)
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Entity
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(length = 20, nullable = false)
    @NotBlank(message = "Veuillez entrer votre nom")
    @Size(min = 2, max = 50, message = "Le nom doit comporter entre 2 et 50 caractères")
    @Pattern(regexp = "^[a-zA-ZÀ-ÿ\s'-]+$", message = "Le nom ne doit pas contenir des chiffres")
    private String name;

    @Column(length = 20, nullable = false)
    @NotBlank(message = "Veuillez entrer votre prénom")
    @Size(min = 2, max = 50, message = "Le prénom doit comporter entre 2 et 50 caractères")
    @Pattern(regexp = "^[a-zA-ZÀ-ÿ\s'-]+$", message = "Le prénom ne doit pas contenir des chiffres")
    private String lastName;

    @Column(length = 50, nullable = false, unique = true)
    @NotBlank(message = "Veuillez entrer votre email")
    @Email(message = "L'email doit être sous la forme: exemple@exemple.exemple")
    private String email;

    @Column(length = 255, nullable = false)
    @NotBlank(message = "Veuillez entrer votre mot de passe")
    @Size(min = 8, max = 20, message = "Le mot de passe doit comporter entre 8 et 20 caractères")
    @Pattern(regexp = ".*(?=.*[A-Z])(?=.*\\d).+", message = "Le mot de passe doit contenir au moins une lettre majuscule et un chiffre")
    private String password;

    @Column(length = 20, nullable = false)
    @NotBlank(message = "Veuillez entrer votre numéro de téléphone")
    @Pattern(regexp = "^\\d{8}$", message = "Le numéro de téléphone doit contenir exactement 8 chiffres")
    private String phoneNumber;

    @Column(length = 20)
    private String role;

    @Column(length = 8, nullable = false)
    @NotBlank(message = "Veuillez entrer votre numéro de carte d'identité")
    @Pattern(regexp = "^\\d{8}$", message = "Le numéro de carte d'identité doit contenir exactement 8 chiffres")
    private String carteIdentite;

    @Column
    private LocalDateTime disponibility;

    @Column(length = 255)
    private String location;

    @Column(length = 255)
    private String experience;

    @Column(nullable = false)
    private boolean active = true;

   /* @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<Favoris> favoris = new HashSet<>();*/

    // Getters and setters
    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getLastName() { return lastName; }
    public void setLastName(String lastName) { this.lastName = lastName; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public String getPhoneNumber() { return phoneNumber; }
    public void setPhoneNumber(String phoneNumber) { this.phoneNumber = phoneNumber; }

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }

    public String getCarteIdentite() { return carteIdentite; }
    public void setCarteIdentite(String carteIdentite) { this.carteIdentite = carteIdentite; }

    public LocalDateTime getDisponibility() { return disponibility; }
    public void setDisponibility(LocalDateTime disponibility) { this.disponibility = disponibility; }

    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }

    public String getExperience() { return experience; }
    public void setExperience(String experience) { this.experience = experience; }

    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }
    public String getStatus() {
        return active ? "Active" : "Inactive";
    }
    public String getFullName() {
        return name + " " + lastName;
    }

  /*  public Set<Favoris> getFavoris() { return favoris; }
    public void addFavoris(Favoris favori) {
        favoris.add(favori);
        favori.setUser(this);
    }
    public void removeFavoris(Favoris favori) {
        favoris.remove(favori);
        favori.setUser(null);
    }*/

    @Override
    public String toString() {
        return name + " " + lastName;
    }

    public String[] getRolesArray() {
        Set<String> roles = new HashSet<>();
        roles.add("ROLE_USER");
        if ("admin".equals(role)) roles.add("ROLE_ADMIN");
        else if ("ouvrier".equals(role)) roles.add("ROLE_OUVRIER");
        else if ("agriculteur".equals(role)) roles.add("ROLE_AGRICULTEUR");
        return roles.toArray(new String[0]);
    }

}
