package tn.esprit.services;

import tn.esprit.entities.Categorie;
import tn.esprit.entities.SousCategorie;
import tn.esprit.tools.Database;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class CategorieService {

    private final Connection cnx;

    public CategorieService() {
        cnx = Database.getInstance().getConnection();
    }

    public void ajouter(Categorie c) {
        String sql = "INSERT INTO categorie (nom, image) VALUES (?, ?)";
        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setString(1, c.getNom());
            if (c.getImage() != null && !c.getImage().isBlank()) {
                ps.setString(2, c.getImage());
            } else {
                ps.setNull(2, Types.VARCHAR);
            }
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
            System.out.println("❌ Erreur lors de l'ajout de catégorie.");
        }
    }

    public void modifier(Categorie c) {
        String sql = "UPDATE categorie SET nom = ?, image = ? WHERE id = ?";
        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setString(1, c.getNom());
            if (c.getImage() != null && !c.getImage().isBlank()) {
                ps.setString(2, c.getImage());
            } else {
                ps.setNull(2, Types.VARCHAR);
            }
            ps.setLong(3, c.getId());
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
            System.out.println("❌ Erreur lors de la modification de catégorie.");
        }
    }

    public void supprimer(Categorie c) {
        String sql = "DELETE FROM categorie WHERE id = ?";
        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setLong(1, c.getId());
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
            System.out.println("❌ Erreur lors de la suppression de catégorie.");
        }
    }

    public List<Categorie> getAll() {
        List<Categorie> list = new ArrayList<>();

        String sql = """
            SELECT c.id, c.nom, c.image, COUNT(sc.id) AS nb_sous_categories
            FROM categorie c
            LEFT JOIN sous_categorie sc ON sc.categorie_id = c.id
            GROUP BY c.id, c.nom, c.image
        """;

        try (Statement st = cnx.createStatement();
             ResultSet rs = st.executeQuery(sql)) {

            while (rs.next()) {
                Categorie c = new Categorie();
                c.setId(rs.getLong("id"));
                c.setNom(rs.getString("nom"));
                c.setImage(rs.getString("image"));

                // Simuler les sous-catégories uniquement pour le comptage
                int count = rs.getInt("nb_sous_categories");
                List<SousCategorie> fakeList = new ArrayList<>();
                for (int i = 0; i < count; i++) {
                    fakeList.add(new SousCategorie()); // objet vide juste pour indiquer le nombre
                }
                c.setSousCategories(fakeList);

                list.add(c);
            }

        } catch (SQLException e) {
            e.printStackTrace();
            System.out.println("❌ Erreur lors de la récupération des catégories.");
        }

        return list;
    }

    public List<SousCategorie> getSousCategoriesParCategorie(Long categorieId) {
        List<SousCategorie> list = new ArrayList<>();
        String sql = "SELECT id, nom, image FROM sous_categorie WHERE categorie_id = ?";
        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setLong(1, categorieId);
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                SousCategorie sc = new SousCategorie();
                sc.setId(rs.getLong("id"));
                sc.setNom(rs.getString("nom"));
                sc.setImage(rs.getString("image")); // ici aussi, juste le nom de fichier
                list.add(sc);
            }
        } catch (SQLException e) {
            e.printStackTrace();
            System.out.println("❌ Erreur récupération des sous-catégories de la catégorie " + categorieId);
        }

        return list;
    }
}
