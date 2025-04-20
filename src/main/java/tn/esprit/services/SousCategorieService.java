package tn.esprit.services;

import tn.esprit.entities.Categorie;
import tn.esprit.entities.SousCategorie;
import tn.esprit.tools.Database;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class SousCategorieService {

    private final Connection cnx;

    public SousCategorieService() {
        this.cnx = Database.getInstance().getConnection();
    }

    // ✅ Ajouter une sous-catégorie
    public void ajouter(SousCategorie s) {
        String sql = "INSERT INTO sous_categorie (nom, image, categorie_id) VALUES (?, ?, ?)";
        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setString(1, s.getNom());
            if (s.getImage() != null && !s.getImage().isBlank()) {
                ps.setString(2, s.getImage());
            } else {
                ps.setNull(2, Types.VARCHAR);
            }
            ps.setLong(3, s.getCategorie().getId());
            ps.executeUpdate();
        } catch (SQLException e) {
            System.err.println("❌ Erreur ajout sous-catégorie : " + e.getMessage());
        }
    }

    // ✅ Modifier une sous-catégorie
    public void modifier(SousCategorie s) {
        String sql = "UPDATE sous_categorie SET nom=?, image=?, categorie_id=? WHERE id=?";
        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setString(1, s.getNom());
            if (s.getImage() != null && !s.getImage().isBlank()) {
                ps.setString(2, s.getImage());
            } else {
                ps.setNull(2, Types.VARCHAR);
            }
            ps.setLong(3, s.getCategorie().getId());
            ps.setLong(4, s.getId());
            ps.executeUpdate();
        } catch (SQLException e) {
            System.err.println("❌ Erreur modification sous-catégorie : " + e.getMessage());
        }
    }

    // ✅ Supprimer une sous-catégorie
    public void supprimer(SousCategorie s) {
        if (s == null || s.getId() == null) {
            System.out.println("⚠️ Suppression impossible : objet ou ID null");
            return;
        }

        String sql = "DELETE FROM sous_categorie WHERE id=?";
        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setLong(1, s.getId());
            int rows = ps.executeUpdate();
            System.out.println(rows > 0
                    ? "✅ Sous-catégorie supprimée avec succès."
                    : "⚠️ Aucune ligne supprimée.");
        } catch (SQLException e) {
            System.err.println("❌ Erreur lors de la suppression : " + e.getMessage());
        }
    }

    // ✅ Lister toutes les sous-catégories avec leur catégorie liée
    public List<SousCategorie> getAll() {
        List<SousCategorie> list = new ArrayList<>();
        String sql = """
            SELECT sc.id AS sc_id, sc.nom AS sc_nom, sc.image AS sc_image,
                   c.id AS c_id, c.nom AS c_nom
            FROM sous_categorie sc
            JOIN categorie c ON sc.categorie_id = c.id
        """;

        try (Statement st = cnx.createStatement(); ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                SousCategorie s = new SousCategorie();
                s.setId(rs.getLong("sc_id"));
                s.setNom(rs.getString("sc_nom"));
                s.setImage(rs.getString("sc_image"));

                Categorie c = new Categorie();
                c.setId(rs.getLong("c_id"));
                c.setNom(rs.getString("c_nom"));

                s.setCategorie(c);
                list.add(s);
            }
        } catch (SQLException e) {
            System.err.println("❌ Erreur chargement sous-catégories : " + e.getMessage());
        }

        return list;
    }
    public List<SousCategorie> getByCategorie(Long categorieId) {
        return getAll().stream()
                .filter(sc -> sc.getCategorie() != null && sc.getCategorie().getId().equals(categorieId))
                .collect(Collectors.toList());
    }

}
