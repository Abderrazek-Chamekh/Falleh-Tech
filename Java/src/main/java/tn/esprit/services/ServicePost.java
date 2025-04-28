package tn.esprit.services;

import tn.esprit.entities.Post;
import tn.esprit.entities.User;
import tn.esprit.tools.Database;

import java.sql.*;
import java.time.LocalDate;
import java.time.Month;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ServicePost implements services<Post> {

    private final Connection con;

    public ServicePost() {
        this.con = Database.getInstance().getConnection();
    }

    // Méthode de validation des données d'un Post
    private boolean isValidPost(Post post) {
        // Validation du titre : non nul, au moins 5 caractères et uniquement des lettres (espaces autorisés)
        if (post.getTitre() == null || post.getTitre().trim().isEmpty()) {
            System.out.println("Erreur: Le titre ne doit pas être vide.");
            return false;
        }
        if (post.getTitre().trim().length() < 5) {
            System.out.println("Erreur: Le titre doit comporter au moins 5 caractères.");
            return false;
        }
        if (!post.getTitre().matches("[A-Za-z\\s]+")) {
            System.out.println("Erreur: Le titre doit contenir uniquement des lettres.");
            return false;
        }

        // Validation du contenu : non nul, plus de 10 caractères et ne doit pas être composé uniquement de chiffres
        if (post.getContenu() == null || post.getContenu().trim().isEmpty()) {
            System.out.println("Erreur: Le contenu ne doit pas être vide.");
            return false;
        }
        if (post.getContenu().trim().length() <= 10) {
            System.out.println("Erreur: Le contenu doit comporter plus de 10 caractères.");
            return false;
        }
        if (post.getContenu().matches("[0-9]+")) {
            System.out.println("Erreur: Le contenu ne doit pas être composé uniquement de chiffres.");
            return false;
        }

        // Validation de l'image : non nulle ou vide
        if (post.getImage() == null || post.getImage().trim().isEmpty()) {
            System.out.println("Erreur: L'image ne doit pas être vide.");
            return false;
        }

        // Validation de la catégorie : non nulle, plus de 10 caractères et ne doit pas être composée uniquement de chiffres
        if (post.getCategory() == null || post.getCategory().trim().isEmpty()) {
            System.out.println("Erreur: La catégorie ne doit pas être vide.");
            return false;
        }
        if (post.getCategory().trim().length() <= 10) {
            System.out.println("Erreur: La catégorie doit comporter plus de 10 caractères.");
            return false;
        }
        if (post.getCategory().matches("[0-9]+")) {
            System.out.println("Erreur: La catégorie ne doit pas être composée uniquement de chiffres.");
            return false;
        }

        return true;
    }

    @Override
    public void ajouter(Post post) {
        // Contrôle de saisie
        if (!isValidPost(post)) {
            System.out.println("Validation échouée, le post n'a pas été ajouté.");
            return;
        }
        String sql = "INSERT INTO post (titre, contenu, date, image, category, user_id) VALUES (?, ?, ?, ?, ?, ?)";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, post.getTitre());
            ps.setString(2, post.getContenu());
            ps.setDate(3, Date.valueOf(post.getDate() != null ? post.getDate() : LocalDate.now()));
            ps.setString(4, post.getImage());
            ps.setString(5, post.getCategory());

            // Vérifier que l'utilisateur est défini
            if (post.getUser() == null) {
                throw new SQLException("User non défini pour le post.");
            }
            ps.setInt(6, post.getUser().getId());
            int rows = ps.executeUpdate();
            if (rows > 0) {
                System.out.println("✅ Post ajouté avec succès !");
            } else {
                System.out.println("❌ Aucun post inséré.");
            }
        } catch (SQLException e) {
            System.out.println("Erreur lors de l'ajout du post: " + e.getMessage());
        }
    }

    @Override
    public void modifier(Post post) {
        // Contrôle de saisie
        if (!isValidPost(post)) {
            System.out.println("Validation échouée, le post n'a pas été modifié.");
            return;
        }
        String sql = "UPDATE post SET titre = ?, contenu = ?, date = ?, image = ?, category = ?, user_id = ? WHERE id = ?";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, post.getTitre());
            ps.setString(2, post.getContenu());
            ps.setDate(3, Date.valueOf(post.getDate() != null ? post.getDate() : LocalDate.now()));
            ps.setString(4, post.getImage());
            ps.setString(5, post.getCategory());
            ps.setInt(6, post.getUser().getId());
            ps.setInt(7, post.getId());

            int rows = ps.executeUpdate();
            System.out.println(rows > 0
                    ? "✅ Post modifié avec succès !"
                    : "❌ Aucun post trouvé avec l'ID: " + post.getId());
        } catch (SQLException e) {
            System.out.println("Erreur lors de la modification du post: " + e.getMessage());
        }
    }

    @Override
    public void supprimer(Post post) {
        String sql = "DELETE FROM post WHERE id = ?";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, post.getId());
            int rows = ps.executeUpdate();
            System.out.println(rows > 0
                    ? "✅ Post supprimé avec succès !"
                    : "❌ Aucun Post trouvé avec l'ID: " + post.getId());
        } catch (SQLException e) {
            System.out.println("Erreur lors de la suppression du post: " + e.getMessage());
        }
    }

    @Override
    public List<Post> getAll() {
        List<Post> posts = new ArrayList<>();
        String sql = "SELECT * FROM post";

        try (Statement st = con.createStatement();
             ResultSet rs = st.executeQuery(sql)) {

            while (rs.next()) {
                Post p = new Post();
                p.setId(rs.getInt("id"));
                p.setTitre(rs.getString("titre"));
                p.setContenu(rs.getString("contenu"));
                if (rs.getDate("date") != null) {
                    p.setDate(rs.getDate("date").toLocalDate());
                }
                p.setImage(rs.getString("image"));
                p.setCategory(rs.getString("category"));

                UserService us = new UserService();
                p.setUser(us.getById(rs.getInt("user_id")));

                posts.add(p);
            }
        } catch (SQLException e) {
            System.out.println("Erreur lors de la récupération des posts: " + e.getMessage());
        }
        return posts;
    }

    public Post getById(int id) throws Exception {
        String query = """
            SELECT p.*, u.id as user_id, u.name, u.last_name, u.email, u.role
            FROM post p
            JOIN user u ON p.user_id = u.id
            WHERE p.id = ?
            """;

        try (PreparedStatement pst = con.prepareStatement(query)) {
            pst.setInt(1, id);

            try (ResultSet rs = pst.executeQuery()) {
                if (rs.next()) {
                    User user = new User();
                    user.setId(rs.getInt("user_id"));
                    user.setName(rs.getString("name"));
                    user.setLastName(rs.getString("last_name"));
                    user.setEmail(rs.getString("email"));
                    user.setRole(rs.getString("role"));

                    Post post = new Post();
                    post.setId(rs.getInt("id"));
                    post.setTitre(rs.getString("titre"));
                    post.setContenu(rs.getString("contenu"));
                    post.setDate(rs.getDate("date").toLocalDate());
                    post.setImage(rs.getString("image"));
                    post.setCategory(rs.getString("category"));
                    post.setUser(user);

                    return post;
                }
            }
        } catch (SQLException e) {
            throw new Exception("❌ Error while fetching post by ID", e);
        }
        return null;
    }

    public List<Post> getPostsByUser(User user) throws Exception {
        List<Post> posts = new ArrayList<>();
        String query = "SELECT * FROM post WHERE user_id = ? ORDER BY date DESC";

        try (PreparedStatement pst = con.prepareStatement(query)) {
            pst.setInt(1, user.getId());

            try (ResultSet rs = pst.executeQuery()) {
                while (rs.next()) {
                    Post post = new Post();
                    post.setId(rs.getInt("id"));
                    post.setTitre(rs.getString("titre"));
                    post.setContenu(rs.getString("contenu"));
                    post.setDate(rs.getDate("date").toLocalDate());
                    post.setImage(rs.getString("image"));
                    post.setCategory(rs.getString("category"));
                    post.setUser(user);

                    posts.add(post);
                }
            }
        } catch (SQLException e) {
            throw new Exception("❌ Error while fetching posts by user", e);
        }
        return posts;
    }

    public List<Post> getPostsByDate(LocalDate date) throws Exception {
        List<Post> posts = new ArrayList<>();
        String query = """
            SELECT p.*, u.id as user_id, u.name, u.last_name, u.email, u.role
            FROM post p
            JOIN user u ON p.user_id = u.id
            WHERE p.date = ?
            ORDER BY p.date DESC
            """;

        try (PreparedStatement pst = con.prepareStatement(query)) {
            pst.setDate(1, Date.valueOf(date));

            try (ResultSet rs = pst.executeQuery()) {
                while (rs.next()) {
                    User user = new User();
                    user.setId(rs.getInt("user_id"));
                    user.setName(rs.getString("name"));
                    user.setLastName(rs.getString("last_name"));
                    user.setEmail(rs.getString("email"));
                    user.setRole(rs.getString("role"));

                    Post post = new Post();
                    post.setId(rs.getInt("id"));
                    post.setTitre(rs.getString("titre"));
                    post.setContenu(rs.getString("contenu"));
                    post.setDate(rs.getDate("date").toLocalDate());
                    post.setImage(rs.getString("image"));
                    post.setCategory(rs.getString("category"));
                    post.setUser(user);

                    posts.add(post);
                }
            }
        } catch (SQLException e) {
            throw new Exception("❌ Error while fetching posts by date", e);
        }
        return posts;
    }

    public List<Post> searchPosts(String keyword) throws Exception {
        List<Post> posts = new ArrayList<>();
        String query = """
            SELECT p.*, u.id as user_id, u.name, u.last_name, u.email, u.role
            FROM post p
            JOIN user u ON p.user_id = u.id
            WHERE p.titre LIKE ? OR p.contenu LIKE ? OR p.category LIKE ?
            ORDER BY p.date DESC
            """;

        try (PreparedStatement pst = con.prepareStatement(query)) {
            String searchPattern = "%" + keyword + "%";
            pst.setString(1, searchPattern);
            pst.setString(2, searchPattern);
            pst.setString(3, searchPattern);

            try (ResultSet rs = pst.executeQuery()) {
                while (rs.next()) {
                    User user = new User();
                    user.setId(rs.getInt("user_id"));
                    user.setName(rs.getString("name"));
                    user.setLastName(rs.getString("last_name"));
                    user.setEmail(rs.getString("email"));
                    user.setRole(rs.getString("role"));

                    Post post = new Post();
                    post.setId(rs.getInt("id"));
                    post.setTitre(rs.getString("titre"));
                    post.setContenu(rs.getString("contenu"));
                    post.setDate(rs.getDate("date").toLocalDate());
                    post.setImage(rs.getString("image"));
                    post.setCategory(rs.getString("category"));
                    post.setUser(user);

                    posts.add(post);
                }
            }
        } catch (SQLException e) {
            throw new Exception("❌ Error while searching posts", e);
        }
        return posts;
    }
    public List<Post> getPostsByCategory(String categoryValue) throws SQLException {
        String query;
        if (categoryValue.equals("All")) {
            query = """
            SELECT p.*, u.id as user_id, u.name, u.last_name, u.email, u.role
            FROM post p
            JOIN user u ON p.user_id = u.id
            ORDER BY p.date DESC
            """;
        } else {
            query = """
            SELECT p.*, u.id as user_id, u.name, u.last_name, u.email, u.role
            FROM post p
            JOIN user u ON p.user_id = u.id
            WHERE p.category = ?
            ORDER BY p.date DESC
            """;
        }


        try (PreparedStatement stmt = con.prepareStatement(query)) {
            if (!categoryValue.equals("All")) {
                stmt.setString(1, categoryValue);
            }

            ResultSet rs = stmt.executeQuery();
            List<Post> posts = new ArrayList<>();
            while (rs.next()) {
                User user = new User();
                user.setId(rs.getInt("user_id"));
                user.setName(rs.getString("name"));
                user.setLastName(rs.getString("last_name"));
                user.setEmail(rs.getString("email"));
                user.setRole(rs.getString("role"));

                Post post = new Post();
                post.setId(rs.getInt("id"));
                post.setTitre(rs.getString("titre"));
                post.setContenu(rs.getString("contenu"));
                post.setDate(rs.getDate("date").toLocalDate());
                post.setImage(rs.getString("image"));
                post.setCategory(rs.getString("category"));
                post.setUser(user);

                posts.add(post);
            }
            return posts;
        }
    }

    private List<Post> executePostQueryWithCount(String query, int limit) throws SQLException {
        List<Post> posts = new ArrayList<>();

        try (PreparedStatement pst = con.prepareStatement(query)) {
            pst.setInt(1, limit);

            try (ResultSet rs = pst.executeQuery()) {
                while (rs.next()) {
                    UserService us = new UserService();
                    User user = us.getById(rs.getInt("user_id"));

                    Post post = new Post();
                    post.setId(rs.getInt("id"));
                    post.setTitre(rs.getString("titre"));
                    post.setContenu(rs.getString("contenu"));
                    post.setDate(rs.getDate("date").toLocalDate());
                    post.setImage(rs.getString("image"));
                    post.setCategory(rs.getString("category"));
                    post.setUser(user);

                    posts.add(post);
                }
            }
        }
        return posts;
    }

    public Map<String, Integer> getPostCountByCategory() throws SQLException {
        Map<String, Integer> categoryCounts = new HashMap<>();
        String query = "SELECT category, COUNT(*) as count FROM post GROUP BY category";

        try (Statement stmt = con.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {

            while (rs.next()) {
                categoryCounts.put(rs.getString("category"), rs.getInt("count"));
            }
        }

        return categoryCounts;
    }

    public Map<Month, Integer> getPostCountByMonth() throws SQLException {
        Map<Month, Integer> monthlyCounts = new HashMap<>();
        String query = "SELECT MONTH(date) as month, COUNT(*) as count FROM post GROUP BY MONTH(date)";

        try (Statement stmt = con.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {

            while (rs.next()) {
                int monthValue = rs.getInt("month");
                Month month = Month.of(monthValue);
                monthlyCounts.put(month, rs.getInt("count"));
            }
        }

        // Ensure all months are represented, even with zero counts
        for (Month month : Month.values()) {
            monthlyCounts.putIfAbsent(month, 0);
        }

        return monthlyCounts;
    }
    public int getTotalPostCount() throws SQLException {
        String query = "SELECT COUNT(*) as total FROM post";
        try (Statement stmt = con.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {
            return rs.next() ? rs.getInt("total") : 0;
        }
    }

    public int getCategoryCount() throws SQLException {
        String query = "SELECT COUNT(DISTINCT category) as count FROM post";
        try (Statement stmt = con.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {
            return rs.next() ? rs.getInt("count") : 0;
        }
    }

    public String getMostActiveMonth() throws SQLException {
        String query = """
        SELECT MONTHNAME(date) as month, COUNT(*) as count 
        FROM post 
        GROUP BY MONTH(date) 
        ORDER BY count DESC 
        LIMIT 1
        """;
        try (Statement stmt = con.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {
            return rs.next() ? rs.getString("month") : "N/A";
        }
    }

    public List<Post> getFilteredPosts(String category, String searchTerm) throws SQLException {
        String query = """
        SELECT p.*, u.id as user_id, u.name, u.last_name, u.email, u.role
        FROM post p
        JOIN user u ON p.user_id = u.id
        WHERE (? = 'All' OR p.category = ?)
        AND (? = '' OR 
            LOWER(p.titre) LIKE LOWER(CONCAT('%',?,'%')) OR 
            LOWER(p.contenu) LIKE LOWER(CONCAT('%',?,'%')) OR
            LOWER(p.category) LIKE LOWER(CONCAT('%',?,'%')))
        ORDER BY p.date DESC
        """;

        try (PreparedStatement stmt = con.prepareStatement(query)) {
            stmt.setString(1, category);
            stmt.setString(2, category);
            stmt.setString(3, searchTerm);
            stmt.setString(4, searchTerm);
            stmt.setString(5, searchTerm);
            stmt.setString(6, searchTerm);

            ResultSet rs = stmt.executeQuery();
            List<Post> posts = new ArrayList<>();

            while (rs.next()) {
                User user = new User();
                user.setId(rs.getInt("user_id"));
                user.setName(rs.getString("name"));
                user.setLastName(rs.getString("last_name"));
                user.setEmail(rs.getString("email"));
                user.setRole(rs.getString("role"));

                Post post = new Post();
                post.setId(rs.getInt("id"));
                post.setTitre(rs.getString("titre"));
                post.setContenu(rs.getString("contenu"));
                post.setDate(rs.getDate("date").toLocalDate());
                post.setImage(rs.getString("image"));
                post.setCategory(rs.getString("category"));
                post.setUser(user);

                posts.add(post);
            }
            return posts;
        }
    }

    public List<Post> getTopEngagedPosts(int limit) throws SQLException {
        String query = """
    SELECT p.*, 
           COUNT(DISTINCT l.id) AS like_count, 
           COUNT(DISTINCT c.id) AS comment_count, 
           (COUNT(DISTINCT l.id) + COUNT(DISTINCT c.id)) AS total_engagement
    FROM post p
    LEFT JOIN `like` l ON p.id = l.post_id
    LEFT JOIN comment c ON p.id = c.post_id
    GROUP BY p.id
    ORDER BY total_engagement DESC
    LIMIT ?
    """;

        return executePostQueryWithCount(query, limit);
    }
}
