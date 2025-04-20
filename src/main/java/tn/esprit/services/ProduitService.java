package tn.esprit.services;

import tn.esprit.entities.Categorie;
import tn.esprit.entities.Produit;
import tn.esprit.entities.SousCategorie;
import tn.esprit.tools.Database;

import java.io.File;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ProduitService {

    private final Connection cnx;

    public ProduitService() {
        this.cnx = Database.getInstance().getConnection();
    }

    public boolean existsBySousCategorie(Long sousCategorieId) {
        String sql = "SELECT COUNT(*) FROM produit WHERE sous_categorie_id = ?";
        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setLong(1, sousCategorieId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() && rs.getInt(1) > 0;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public void ajouter(Produit p) {
        String sql = """
            INSERT INTO produit 
            (nom, prix, description, categorie_id, sous_categorie_id, image, stock, is_favorite, updated_at) 
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)
        """;
        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setString(1, p.getNom());
            ps.setBigDecimal(2, p.getPrix());
            ps.setString(3, p.getDescription());
            ps.setLong(4, p.getCategorie().getId());
            ps.setLong(5, p.getSousCategorie().getId());

            // 🔐 Stocker uniquement le nom de fichier
            String imageName = (p.getImage() != null) ? new File(p.getImage()).getName() : null;
            ps.setString(6, imageName);

            ps.setInt(7, p.getStock());
            ps.setBoolean(8, p.isFavorite());
            ps.setTimestamp(9, Timestamp.valueOf(p.getUpdatedAt()));
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void modifier(Produit p) {
        String sql = """
            UPDATE produit SET 
                nom=?, prix=?, description=?, categorie_id=?, sous_categorie_id=?, 
                image=?, stock=?, is_favorite=?, updated_at=? 
            WHERE id=?
        """;
        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setString(1, p.getNom());
            ps.setBigDecimal(2, p.getPrix());
            ps.setString(3, p.getDescription());
            ps.setLong(4, p.getCategorie().getId());
            ps.setLong(5, p.getSousCategorie().getId());

            // 🔐 Nettoyer chemin pour compatibilité Symfony
            String imageName = (p.getImage() != null) ? new File(p.getImage()).getName() : null;
            ps.setString(6, imageName);

            ps.setInt(7, p.getStock());
            ps.setBoolean(8, p.isFavorite());
            ps.setTimestamp(9, Timestamp.valueOf(p.getUpdatedAt()));
            ps.setLong(10, p.getId());
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void supprimer(Produit p) {
        String sql = "DELETE FROM produit WHERE id=?";
        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setLong(1, p.getId());
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public List<Produit> getAll() {
        List<Produit> list = new ArrayList<>();
        String sql = """
            SELECT p.*, 
                   c.id AS cat_id, c.nom AS cat_nom, 
                   sc.id AS sc_id, sc.nom AS sc_nom
            FROM produit p
            JOIN categorie c ON p.categorie_id = c.id
            JOIN sous_categorie sc ON p.sous_categorie_id = sc.id
        """;

        try (Statement st = cnx.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                list.add(mapProduit(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return list;
    }

    public List<Produit> getProduitsParSousCategorie(Long sousCategorieId) {
        List<Produit> produits = new ArrayList<>();

        String sql = """
            SELECT p.*, 
                   c.id AS cat_id, c.nom AS cat_nom, 
                   sc.id AS sc_id, sc.nom AS sc_nom
            FROM produit p
            JOIN categorie c ON p.categorie_id = c.id
            JOIN sous_categorie sc ON p.sous_categorie_id = sc.id
            WHERE sc.id = ?
        """;

        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setLong(1, sousCategorieId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    produits.add(mapProduit(rs));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return produits;
    }

    private Produit mapProduit(ResultSet rs) throws SQLException {
        Produit p = new Produit();
        p.setId(rs.getLong("id"));
        p.setNom(rs.getString("nom"));
        p.setPrix(rs.getBigDecimal("prix"));
        p.setDescription(rs.getString("description"));
        p.setImage(rs.getString("image")); // juste le nom du fichier
        p.setStock(rs.getInt("stock"));
        p.setFavorite(rs.getBoolean("is_favorite"));

        Timestamp ts = rs.getTimestamp("updated_at");
        if (ts != null) {
            p.setUpdatedAt(ts.toLocalDateTime());
        }

        Categorie c = new Categorie();
        c.setId(rs.getLong("cat_id"));
        c.setNom(rs.getString("cat_nom"));
        p.setCategorie(c);

        SousCategorie sc = new SousCategorie();
        sc.setId(rs.getLong("sc_id"));
        sc.setNom(rs.getString("sc_nom"));
        p.setSousCategorie(sc);

        return p;
    }
    public Produit getById(Long id) {
        String sql = """
        SELECT p.*, 
               c.id AS cat_id, c.nom AS cat_nom, 
               sc.id AS sc_id, sc.nom AS sc_nom
        FROM produit p
        JOIN categorie c ON p.categorie_id = c.id
        JOIN sous_categorie sc ON p.sous_categorie_id = sc.id
        WHERE p.id = ?
    """;

        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setLong(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapProduit(rs);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return null;
    }

}
