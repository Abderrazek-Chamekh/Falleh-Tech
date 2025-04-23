package tn.esprit.services;

import tn.esprit.entities.Favoris;
import tn.esprit.entities.Produit;
import tn.esprit.tools.Database;

import java.sql.*;
import java.util.*;

public class FavorisService {

    private final Connection cnx;

    public FavorisService() {
        this.cnx = Database.getInstance().getConnection();
    }

    public boolean ajouterFavoris(Long produitId, int userId) {
        String checkSql = "SELECT COUNT(*) FROM favoris WHERE produit_id = ? AND user_id = ?";
        try (PreparedStatement check = cnx.prepareStatement(checkSql)) {
            check.setLong(1, produitId);
            check.setInt(2, userId);
            ResultSet rs = check.executeQuery();
            if (rs.next() && rs.getInt(1) > 0) {
                return true;
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return true;
        }

        String sql = "INSERT INTO favoris (produit_id, user_id) VALUES (?, ?)";
        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setLong(1, produitId);
            ps.setInt(2, userId);
            ps.executeUpdate();
            return false;
        } catch (SQLException e) {
            e.printStackTrace();
            return true;
        }
    }

    public void supprimer(Long id) {
        if (id == null) {
            System.err.println("❌ ID de favori non fourni.");
            return;
        }

        String sql = "DELETE FROM favoris WHERE id = ?";
        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setLong(1, id);
            ps.executeUpdate();
            System.out.println("🗑️ Favori supprimé (ID = " + id + ")");
        } catch (SQLException e) {
            System.err.println("❌ Erreur lors de la suppression du favori : " + e.getMessage());
        }
    }

    public List<Favoris> getFavorisParUser(int userId) {
        List<Favoris> favorisList = new ArrayList<>();

        String sql = """
            SELECT f.id AS fav_id,
                   p.id AS produit_id, p.nom, p.prix, p.description, p.stock, p.image
            FROM favoris f
            JOIN produit p ON f.produit_id = p.id
            WHERE f.user_id = ?
        """;

        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setInt(1, userId);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Produit produit = new Produit();
                    produit.setId(rs.getLong("produit_id"));
                    produit.setNom(rs.getString("nom"));
                    produit.setPrix(rs.getBigDecimal("prix"));
                    produit.setDescription(rs.getString("description"));
                    produit.setStock(rs.getInt("stock"));
                    produit.setImage(Optional.ofNullable(rs.getString("image")).orElse("").trim());

                    Favoris favoris = new Favoris();
                    favoris.setId(rs.getLong("fav_id"));
                    favoris.setProduit(produit);
                    favoris.setUserId(userId);

                    favorisList.add(favoris);
                }
            }

        } catch (SQLException e) {
            System.err.println("❌ Erreur lors de la récupération des favoris : " + e.getMessage());
        }

        return favorisList;
    }

    public boolean existeDansFavoris(Long produitId, int userId) {
        String sql = "SELECT COUNT(*) FROM favoris WHERE produit_id = ? AND user_id = ?";
        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setLong(1, produitId);
            ps.setInt(2, userId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return rs.getInt(1) > 0;
            }
        } catch (SQLException e) {
            System.err.println("❌ Erreur vérification favoris : " + e.getMessage());
        }
        return false;
    }

    public void supprimerParProduitEtUser(Long produitId, int userId) {
        String sql = "DELETE FROM favoris WHERE produit_id = ? AND user_id = ?";
        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setLong(1, produitId);
            ps.setInt(2, userId);
            ps.executeUpdate();
            System.out.println("🗑️ Favori supprimé (produit_id = " + produitId + ", user_id = " + userId + ")");
        } catch (SQLException e) {
            System.err.println("❌ Erreur lors de la suppression du favori : " + e.getMessage());
        }
    }

    public List<Favoris> getAllFavoris() {
        List<Favoris> favorisList = new ArrayList<>();

        String sql = """
        SELECT f.id AS fav_id, f.user_id,
               p.id AS produit_id, p.nom, p.prix, p.description, p.stock, p.image
        FROM favoris f
        JOIN produit p ON f.produit_id = p.id
        """;

        try (Statement stmt = cnx.createStatement(); ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                Produit produit = new Produit();
                produit.setId(rs.getLong("produit_id"));
                produit.setNom(rs.getString("nom"));
                produit.setPrix(rs.getBigDecimal("prix"));
                produit.setDescription(rs.getString("description"));
                produit.setStock(rs.getInt("stock"));
                produit.setImage(rs.getString("image"));

                Favoris favoris = new Favoris();
                favoris.setId(rs.getLong("fav_id"));
                favoris.setUserId(rs.getInt("user_id"));
                favoris.setProduit(produit);

                favorisList.add(favoris);
            }
        } catch (SQLException e) {
            System.err.println("❌ Erreur lors de la récupération de tous les favoris : " + e.getMessage());
        }

        return favorisList;
    }

    public List<Produit> suggereProduitsSimilaires(int userId) {
        List<Produit> suggestions = new ArrayList<>();
        Set<Long> idsFavoris = new HashSet<>();
        Set<Long> idsDejaSuggeres = new HashSet<>();

        // 🟢 Étape 1 : récupérer les IDs des produits en favoris
        String sqlFavoris = "SELECT produit_id FROM favoris WHERE user_id = ?";
        try (PreparedStatement ps = cnx.prepareStatement(sqlFavoris)) {
            ps.setInt(1, userId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                idsFavoris.add(rs.getLong("produit_id"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        // 🟢 Étape 2 : récupérer les suggestions
        String sql = """
        SELECT DISTINCT p.*
        FROM produit p
        WHERE (p.categorie_id IN (
            SELECT p2.categorie_id
            FROM produit p2
            JOIN favoris f ON p2.id = f.produit_id
            WHERE f.user_id = ?
        )
        OR p.sous_categorie_id IN (
            SELECT p2.sous_categorie_id
            FROM produit p2
            JOIN favoris f ON p2.id = f.produit_id
            WHERE f.user_id = ?
        ))
        AND p.id NOT IN (
            SELECT produit_id FROM favoris WHERE user_id = ?
        )
    """;

        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setInt(1, userId);
            ps.setInt(2, userId);
            ps.setInt(3, userId);

            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                long id = rs.getLong("id");
                if (idsFavoris.contains(id) || idsDejaSuggeres.contains(id)) continue;

                Produit p = new Produit();
                p.setId(id);
                p.setNom(rs.getString("nom"));
                p.setPrix(rs.getBigDecimal("prix"));
                p.setDescription(rs.getString("description"));
                p.setImage(rs.getString("image"));
                p.setStock(rs.getInt("stock"));

                suggestions.add(p);
                idsDejaSuggeres.add(id);

                if (suggestions.size() >= 4) break;
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return suggestions;
    }

}
