package tn.esprit.services;

import tn.esprit.entities.Notification;
import tn.esprit.entities.Post;
import tn.esprit.entities.User;
import tn.esprit.tools.my_db;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class ServiceNotification {

    private final Connection conn;

    public ServiceNotification() {
        conn = my_db.getInstance().getConnection();
    }

    // ✅ Add notification (with or without post)
    public void addNotification(User user, String message, Post post) throws SQLException {
        String sql = "INSERT INTO notification (user_id, post_id, message, created_at, seen) VALUES (?, ?, ?, ?, false)";
        PreparedStatement ps = conn.prepareStatement(sql);
        ps.setInt(1, user.getId());
        if (post != null) {
            ps.setInt(2, post.getId());
        } else {
            ps.setNull(2, Types.INTEGER);
        }
        ps.setString(3, message);
        ps.setTimestamp(4, Timestamp.valueOf(LocalDateTime.now()));
        ps.executeUpdate();
    }

    // ✅ Overloaded: Add notification without a post
    public void addNotification(User user, String message) throws SQLException {
        addNotification(user, message, null);
    }

    // ✅ Get all unread notifications for a user
    public List<Notification> getUnreadNotifications(int userId) throws SQLException {
        String sql = "SELECT * FROM notification WHERE user_id = ? AND seen = false ORDER BY created_at DESC";
        PreparedStatement ps = conn.prepareStatement(sql);
        ps.setInt(1, userId);
        ResultSet rs = ps.executeQuery();

        List<Notification> list = new ArrayList<>();
        while (rs.next()) {
            Notification n = new Notification();
            n.setId(rs.getInt("id"));
            n.setMessage(rs.getString("message"));
            n.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
            n.setSeen(rs.getBoolean("seen"));

            // Minimal user set
            User u = new User();
            u.setId(rs.getInt("user_id"));
            n.setUser(u);

            // If post_id exists
            int postId = rs.getInt("post_id");
            if (!rs.wasNull()) {
                Post p = new Post();
                p.setId(postId);
                n.setPost(p);
            }

            list.add(n);
        }

        return list;
    }

    // ✅ Mark a notification as seen
    public void markAsSeen(int notificationId) throws SQLException {
        String sql = "UPDATE notification SET seen = true WHERE id = ?";
        PreparedStatement ps = conn.prepareStatement(sql);
        ps.setInt(1, notificationId);
        ps.executeUpdate();
    }

    // ✅ Mark all notifications as seen for a user
    public void markAllAsSeen(int userId) throws SQLException {
        String sql = "UPDATE notification SET seen = true WHERE user_id = ?";
        PreparedStatement ps = conn.prepareStatement(sql);
        ps.setInt(1, userId);
        ps.executeUpdate();
    }
}
