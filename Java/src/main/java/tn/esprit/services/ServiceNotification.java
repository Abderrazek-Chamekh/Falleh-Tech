package tn.esprit.services;

import tn.esprit.entities.Notification;
import tn.esprit.entities.Post;
import tn.esprit.entities.User;
import tn.esprit.tools.Database;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class ServiceNotification {
    private final Connection connection;

    public ServiceNotification() {
        this.connection = Database.getInstance().getConnection();
    }

    public void addNotification(String message, User user, Post post) throws SQLException {
        String query = "INSERT INTO notification (message, user_id, post_id, created_at) VALUES (?, ?, ?, ?)";
        try (PreparedStatement pst = connection.prepareStatement(query)) {
            pst.setString(1, message);
            pst.setInt(2, user.getId());
            pst.setInt(3, post.getId());
            pst.setTimestamp(4, Timestamp.valueOf(LocalDateTime.now()));
            pst.executeUpdate();
        }
    }

    public List<Notification> getUnreadNotificationsForAdmin() throws SQLException {
        List<Notification> notifications = new ArrayList<>();
        String query = """
            SELECT n.*, u.name as user_name, p.titre as post_title 
            FROM notification n
            JOIN user u ON n.user_id = u.id
            JOIN post p ON n.post_id = p.id
            ORDER BY n.created_at DESC
            """;

        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {

            while (rs.next()) {
                Notification notification = new Notification();
                notification.setId(rs.getInt("id"));
                notification.setMessage(rs.getString("message"));

                User user = new User();
                user.setId(rs.getInt("user_id"));
                user.setName(rs.getString("user_name"));
                notification.setUser(user);

                Post post = new Post();
                post.setId(rs.getInt("post_id"));
                post.setTitre(rs.getString("post_title"));
                notification.setPost(post);

                notification.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());

                notifications.add(notification);
            }
        }
        return notifications;
    }

    public int getUnreadNotificationCount() throws SQLException {
        String query = "SELECT COUNT(*) as count FROM notification";
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {
            return rs.next() ? rs.getInt("count") : 0;
        }
    }
}
