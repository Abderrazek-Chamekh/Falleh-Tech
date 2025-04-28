package tn.esprit.services;

import tn.esprit.entities.Flamme;
import tn.esprit.tools.Database;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class FlammeService {
    private static FlammeService instance;
    private final Connection cnx;

    private FlammeService() {
        this.cnx = Database.getInstance().getConnection();
    }

    public static FlammeService getInstance() {
        if (instance == null) {
            instance = new FlammeService();
        }
        return instance;
    }

    // ✅ Ajouter une flamme
    public void ajouter(Flamme f) {
        String sql = "INSERT INTO flamme (user_id, count) VALUES (?, ?)";
        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setLong(1, f.getUserId());
            ps.setInt(2, f.getCount());
            ps.executeUpdate();
            System.out.println("🔥 Flamme ajoutée pour user ID " + f.getUserId());
        } catch (SQLException e) {
            System.err.println("❌ Erreur ajout flamme : " + e.getMessage());
        }
    }

    // 🔥 Récupérer les flammes pour un utilisateur donné
    public List<Flamme> getFlammesByUser(Long userId) {
        List<Flamme> list = new ArrayList<>();
        String sql = "SELECT * FROM flamme WHERE user_id = ?";

        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setLong(1, userId);
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                Flamme f = new Flamme();
                f.setId(rs.getLong("id"));
                f.setUserId(rs.getLong("user_id"));
                f.setCount(rs.getInt("count"));

                list.add(f);
            }
        } catch (SQLException e) {
            System.err.println("❌ Erreur récupération flammes : " + e.getMessage());
        }

        return list;
    }

    // ✅ Supprimer une flamme par ID
    public void supprimer(Long flammeId) {
        String sql = "DELETE FROM flamme WHERE id = ?";
        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setLong(1, flammeId);
            int rows = ps.executeUpdate();
            System.out.println(rows > 0 ? "🔥 Flamme supprimée" : "⚠️ Aucune flamme supprimée");
        } catch (SQLException e) {
            System.err.println("❌ Erreur suppression flamme : " + e.getMessage());
        }
    }
    // 🔥 Vérifier si on peut encore ajouter une flamme (max 3 en 24h)
    public boolean peutAjouterFlammes(Long userId) {
        String sql = "SELECT COUNT(*) FROM flamme WHERE user_id = ? AND created_at >= ?";
        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            LocalDateTime ilYA24h = LocalDateTime.now().minusHours(24);
            Timestamp ilYA24hSQL = Timestamp.valueOf(ilYA24h);

            ps.setLong(1, userId);
            ps.setTimestamp(2, ilYA24hSQL);

            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                int nbFlammes = rs.getInt(1);
                System.out.println("🔥 Flammes gagnées dans les dernières 24h : " + nbFlammes);
                return nbFlammes < 3;
            }
        } catch (SQLException e) {
            System.err.println("❌ Erreur vérification flammes : " + e.getMessage());
        }
        return false;
    }

}
