package tn.esprit.services;

import tn.esprit.entities.Comment;
import tn.esprit.entities.Post;
import tn.esprit.entities.User;
import tn.esprit.tools.Database;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class ServiceComment implements services<Comment> {

    private final Connection con;

    public ServiceComment() {
        this.con = Database.getInstance().getConnection();
    }

    // Méthode de validation du contenu d'un commentaire
    private boolean isValidComment(Comment comment) {
        String contenu = comment.getContenu();
        if (contenu == null || contenu.trim().isEmpty()) {
            System.out.println("Erreur: Le contenu du commentaire ne doit pas être vide.");
            return false;
        }
        if (contenu.trim().length() < 5) {
            System.out.println("Erreur: Le contenu du commentaire doit comporter au moins 5 caractères.");
            return false;
        }
        if (contenu.matches("[0-9]+")) {
            System.out.println("Erreur: Le contenu du commentaire ne doit pas être composé uniquement de chiffres.");
            return false;
        }
        return true;
    }

    @Override
    public void ajouter(Comment comment) {
        // Validation du contenu du commentaire
        if (!isValidComment(comment)) {
            System.out.println("Validation échouée, le commentaire n'a pas été ajouté.");
            return;
        }
        String sql = "INSERT INTO comment (contenu, date, post_id, user_id) VALUES (?, ?, ?, ?)";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, comment.getContenu());
            ps.setDate(2, Date.valueOf(comment.getDate() != null ? comment.getDate() : LocalDate.now()));

            if (comment.getPost() == null || comment.getUser() == null) {
                throw new SQLException("Le commentaire doit être lié à un post et à un utilisateur.");
            }
            ps.setInt(3, comment.getPost().getId());
            ps.setInt(4, comment.getUser().getId());

            int rows = ps.executeUpdate();
            if (rows > 0) {
                System.out.println("✅ Commentaire ajouté avec succès !");
            } else {
                System.out.println("❌ Aucun commentaire inséré.");
            }

        } catch (SQLException e) {
            System.out.println("Erreur lors de l'ajout du commentaire: " + e.getMessage());
        }
    }

    @Override
    public void modifier(Comment comment) {
        // Validation du contenu du commentaire
        if (!isValidComment(comment)) {
            System.out.println("Validation échouée, le commentaire n'a pas été modifié.");
            return;
        }
        String sql = "UPDATE comment SET contenu = ?, date = ?, post_id = ?, user_id = ? WHERE id = ?";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, comment.getContenu());
            ps.setDate(2, Date.valueOf(comment.getDate() != null ? comment.getDate() : LocalDate.now()));
            ps.setInt(3, comment.getPost().getId());
            ps.setInt(4, comment.getUser().getId());
            ps.setInt(5, comment.getId());

            int rows = ps.executeUpdate();
            System.out.println(rows > 0
                    ? "✅ Commentaire modifié avec succès !"
                    : "❌ Aucun commentaire trouvé avec l'ID: " + comment.getId());
        } catch (SQLException e) {
            System.out.println("Erreur lors de la modification du commentaire: " + e.getMessage());
        }
    }

    @Override
    public void supprimer(Comment comment) {
        String sql = "DELETE FROM comment WHERE id = ?";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, comment.getId());
            int rows = ps.executeUpdate();
            System.out.println(rows > 0
                    ? "✅ Commentaire supprimé avec succès !"
                    : "❌ Aucun commentaire trouvé avec l'ID: " + comment.getId());
        } catch (SQLException e) {
            System.out.println("Erreur lors de la suppression du commentaire: " + e.getMessage());
        }
    }

    @Override
    public List<Comment> getAll() {
        List<Comment> comments = new ArrayList<>();
        String sql = "SELECT * FROM comment";
        try (Statement st = con.createStatement();
             ResultSet rs = st.executeQuery(sql)) {

            while (rs.next()) {
                Comment c = new Comment();
                c.setId(rs.getInt("id"));
                c.setContenu(rs.getString("contenu"));
                if (rs.getDate("date") != null) {
                    c.setDate(rs.getDate("date").toLocalDate());
                }


                ServicePost p = new ServicePost();
                c.setPost(p.getById(rs.getInt("post_id")));

                UserService u = new UserService();
                c.setUser(u.getById(rs.getInt("user_id")));

                comments.add(c);
            }
        } catch (SQLException e) {
            System.out.println("Erreur lors de la récupération des commentaires: " + e.getMessage());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return comments;
    }

    // Récupère les commentaires pour un post spécifique
    public List<Comment> getCommentsByPost(int postId) {
        List<Comment> comments = new ArrayList<>();
        String sql = "SELECT * FROM comment WHERE post_id = ?";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, postId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Comment c = new Comment();
                    c.setId(rs.getInt("id"));
                    c.setContenu(rs.getString("contenu"));
                    if (rs.getDate("date") != null) {
                        c.setDate(rs.getDate("date").toLocalDate());
                    }

                    ServicePost p = new ServicePost();
                    c.setPost(p.getById(postId));

                    UserService u = new UserService();
                    c.setUser(u.getById(rs.getInt("user_id")));

                    comments.add(c);
                }
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        } catch (SQLException e) {
            System.out.println("Erreur lors de la récupération des commentaires du post " + postId + ": " + e.getMessage());
        }
        return comments;
    }
}
