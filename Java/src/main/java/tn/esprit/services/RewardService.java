package tn.esprit.services;

import tn.esprit.entities.Reward;
import tn.esprit.tools.Database;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class RewardService {

    private final Connection cnx;
    private static RewardService instance;
    public RewardService() {
        this.cnx = Database.getInstance().getConnection();
    }

    // ✅ Ajouter un reward
    public void ajouter(Reward reward) {
        String sql = "INSERT INTO reward (user_id, type, value, claimed) VALUES (?, ?, ?, ?)";
        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setLong(1, reward.getUserId());
            ps.setString(2, reward.getType());
            ps.setString(3, reward.getValue());
            ps.setBoolean(4, reward.isClaimed());
            ps.executeUpdate();
            System.out.println("🎁 Reward ajouté pour l'utilisateur ID " + reward.getUserId());
        } catch (SQLException e) {
            System.err.println("❌ Erreur ajout reward : " + e.getMessage());
        }
    }

    // 🔍 Récupérer les rewards d’un utilisateur
    public List<Reward> getByUser(Long userId) {
        List<Reward> list = new ArrayList<>();
        String sql = "SELECT * FROM reward WHERE user_id = ?";

        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setLong(1, userId);
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                Reward r = new Reward();
                r.setId(rs.getLong("id"));
                r.setUserId(rs.getLong("user_id"));
                r.setType(rs.getString("type"));
                r.setValue(rs.getString("value"));
                r.setClaimed(rs.getBoolean("claimed"));
                r.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
                list.add(r);
            }

        } catch (SQLException e) {
            System.err.println("❌ Erreur récupération rewards : " + e.getMessage());
        }

        return list;
    }

    // ✅ Marquer un reward comme utilisé
    public void marquerCommeUtilise(Long rewardId) {
        String sql = "UPDATE reward SET claimed = TRUE WHERE id = ?";
        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setLong(1, rewardId);
            int rows = ps.executeUpdate();
            if (rows > 0)
                System.out.println("✅ Reward ID " + rewardId + " marqué comme utilisé.");
        } catch (SQLException e) {
            System.err.println("❌ Erreur mise à jour reward : " + e.getMessage());
        }
    }

    // ❌ Supprimer un reward (si besoin)
    public void supprimer(Long id) {
        String sql = "DELETE FROM reward WHERE id = ?";
        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setLong(1, id);
            ps.executeUpdate();
            System.out.println("🗑️ Reward supprimé ID : " + id);
        } catch (SQLException e) {
            System.err.println("❌ Erreur suppression reward : " + e.getMessage());
        }
    }
    public void attribuerSiEligible(Long userId, int totalFlammes) {
        List<Reward> existants = getByUser(userId);
        List<String> existantsTypes = existants.stream()
                .map(r -> r.getType() + ":" + r.getValue())
                .toList();

        if (totalFlammes >= 10 && !existantsTypes.contains("pdf:recette1.pdf"))
            ajouter(new Reward(userId, "pdf", "recette1.pdf"));

        if (totalFlammes >= 20 && !existantsTypes.contains("pdf:recette2.pdf"))
            ajouter(new Reward(userId, "pdf", "recette2.pdf"));

        if (totalFlammes >= 30 && !existantsTypes.contains("pdf:recette3.pdf"))
            ajouter(new Reward(userId, "pdf", "recette3.pdf"));

        if (totalFlammes >= 40 && !existantsTypes.contains("pdf:recette4.pdf"))
            ajouter(new Reward(userId, "pdf", "recette4.pdf"));

        if (totalFlammes >= 50 && !existantsTypes.contains("pdf:recette5.pdf"))
            ajouter(new Reward(userId, "pdf", "recette5.pdf"));

        if (totalFlammes >= 25 && !existantsTypes.contains("code_promo:AGRI5"))
            ajouter(new Reward(userId, "code_promo", "AGRI5"));

        if (totalFlammes >= 75 && !existantsTypes.contains("code_promo:AGRI10"))
            ajouter(new Reward(userId, "code_promo", "AGRI10"));

        if (totalFlammes >= 150 && !existantsTypes.contains("code_promo:AGRI20"))
            ajouter(new Reward(userId, "code_promo", "AGRI20"));
    }


    public List<Reward> getRewardsByUser(Long userId) {
        List<Reward> list = new ArrayList<>();
        String sql = "SELECT * FROM reward WHERE user_id = ?";

        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setLong(1, userId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                Reward r = new Reward();
                r.setId(rs.getLong("id"));
                r.setUserId(rs.getLong("user_id"));
                r.setType(rs.getString("type"));
                r.setValue(rs.getString("value"));
                r.setClaimed(rs.getBoolean("claimed"));
                list.add(r);
            }
        } catch (SQLException e) {
            System.err.println("❌ Erreur récupération rewards : " + e.getMessage());
        }

        return list;
    }
    public static RewardService getInstance() {
        if (instance == null) {
            instance = new RewardService();
        }
        return instance;
    }

}
