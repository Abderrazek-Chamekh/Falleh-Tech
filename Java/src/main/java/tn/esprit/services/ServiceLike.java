package tn.esprit.services;

import tn.esprit.entities.Like;
import tn.esprit.entities.Post;
import tn.esprit.entities.User;
import tn.esprit.tools.Database;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ServiceLike implements services<Like> {

    private final Connection con;

    public ServiceLike() {
        this.con = Database.getInstance().getConnection();
    }

    @Override
    public void ajouter(Like like) {
        String sql = "INSERT INTO `like` (post_id, user_id) VALUES (?, ?)";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            if (like.getPost() == null || like.getUser() == null) {
                throw new SQLException("Le like doit être lié à un post et à un utilisateur.");
            }

            ps.setInt(1, like.getPost().getId());
            ps.setInt(2, like.getUser().getId());

            int rows = ps.executeUpdate();
            if (rows > 0) {
                System.out.println("✅ Like ajouté avec succès !");
            } else {
                System.out.println("❌ Aucun like inséré.");
            }

        } catch (SQLException e) {
            System.out.println("Erreur lors de l'ajout du like: " + e.getMessage());
        }
    }

    @Override
    public void modifier(Like like) {
        String sql = "UPDATE `like` SET post_id = ?, user_id = ? WHERE id = ?";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, like.getPost().getId());
            ps.setInt(2, like.getUser().getId());
            ps.setInt(3, like.getId());

            int rows = ps.executeUpdate();
            System.out.println(rows > 0
                    ? "✅ Like modifié avec succès !"
                    : "❌ Aucun like trouvé avec l'ID: " + like.getId());
        } catch (SQLException e) {
            System.out.println("Erreur lors de la modification du like: " + e.getMessage());
        }
    }

    @Override
    public void supprimer(Like like) {
        String sql = "DELETE FROM `like` WHERE id = ?";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, like.getId());
            int rows = ps.executeUpdate();
            System.out.println(rows > 0
                    ? "✅ Like supprimé avec succès !"
                    : "❌ Aucun like trouvé avec l'ID: " + like.getId());
        } catch (SQLException e) {
            System.out.println("Erreur lors de la suppression du like: " + e.getMessage());
        }
    }

    @Override
    public List<Like> getAll() {
        List<Like> likes = new ArrayList<>();
        String sql = "SELECT * FROM `like`";
        try (Statement st = con.createStatement();
             ResultSet rs = st.executeQuery(sql)) {

            while (rs.next()) {
                Like l = new Like();
                l.setId(rs.getInt("id"));

                Post p = new Post();
                p.setId(rs.getInt("post_id"));
                l.setPost(p);

                User u = new User();
                u.setId(rs.getInt("user_id"));
                l.setUser(u);

                likes.add(l);
            }
        } catch (SQLException e) {
            System.out.println("Erreur lors de la récupération des likes: " + e.getMessage());
        }
        return likes;
    }

    // ✅ Récupère les likes pour un post spécifique
    public List<Like> getLikesByPost(int postId) {
        List<Like> likes = new ArrayList<>();
        String sql = "SELECT * FROM `like` WHERE post_id = ?";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, postId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Like l = new Like();
                    l.setId(rs.getInt("id"));

                    Post p = new Post();
                    p.setId(postId);
                    l.setPost(p);

                    User u = new User();
                    u.setId(rs.getInt("user_id"));
                    l.setUser(u);

                    likes.add(l);
                }
            }
        } catch (SQLException e) {
            System.out.println("Erreur lors de la récupération des likes du post " + postId + ": " + e.getMessage());
        }
        return likes;
    }

    public int countReactions(int Id_pub) {
        int count = 0;
        try {
            String query = "SELECT COUNT(*) AS count FROM `like` WHERE post_id = ?";
            PreparedStatement stmt = con.prepareStatement(query);
            stmt.setInt(1, Id_pub);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                count = rs.getInt("count");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return count;
    }

    public boolean hasUserLikedPost(int userId, int postId) throws SQLException {
        String query = "SELECT COUNT(*) FROM `like` WHERE user_id = ? AND post_id = ?";
        try (PreparedStatement pst = con.prepareStatement(query)) {
            pst.setInt(1, userId);
            pst.setInt(2, postId);
            try (ResultSet rs = pst.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
        }
        return false;
    }

}
