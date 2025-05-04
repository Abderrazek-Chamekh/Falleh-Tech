package tn.esprit.services;

import tn.esprit.entities.GeneralNotification;
import tn.esprit.entities.Post;
import tn.esprit.entities.User;
import tn.esprit.tools.my_db;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
public class ServiceGeneralNotification {
    private final Connection conn = my_db.getInstance().getConnection();

    public void add(int userId, String message) throws SQLException {
        String sql = "INSERT INTO general_notification (user_id, message, seen) VALUES (?, ?, 0)";
        PreparedStatement ps = conn.prepareStatement(sql);
        ps.setInt(1, userId);
        ps.setString(2, message);
        ps.executeUpdate();
    }

    public List<GeneralNotification> getUnread(int userId) throws SQLException {
        String sql = "SELECT * FROM general_notification WHERE user_id = ? AND seen = 0";
        PreparedStatement ps = conn.prepareStatement(sql);
        ps.setInt(1, userId);
        ResultSet rs = ps.executeQuery();

        List<GeneralNotification> list = new ArrayList<>();
        while (rs.next()) {
            GeneralNotification notif = new GeneralNotification();
            notif.setId(rs.getInt("id"));
            notif.setUserId(rs.getInt("user_id"));
            notif.setMessage(rs.getString("message"));
            notif.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
            notif.setSeen(rs.getBoolean("seen"));
            list.add(notif);
        }
        return list;
    }

    public void markAsSeen(int id) throws SQLException {
        String sql = "UPDATE general_notification SET seen = 1 WHERE id = ?";
        PreparedStatement ps = conn.prepareStatement(sql);
        ps.setInt(1, id);
        ps.executeUpdate();
    }
    public void add(GeneralNotification notification) {
        try {
            Connection connection = my_db.getInstance().getConnection();
            String sql = "INSERT INTO general_notification (user_id, message, created_at, seen) VALUES (?, ?, ?, ?)";
            PreparedStatement ps = connection.prepareStatement(sql);
            ps.setInt(1, notification.getUserId());
            ps.setString(2, notification.getMessage());
            ps.setTimestamp(3, java.sql.Timestamp.valueOf(notification.getCreatedAt()));
            ps.setBoolean(4, notification.isSeen());
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

}
