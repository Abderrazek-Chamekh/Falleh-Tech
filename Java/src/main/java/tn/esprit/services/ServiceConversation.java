package tn.esprit.services;

import tn.esprit.entities.Conversation;
import tn.esprit.entities.User;
import tn.esprit.tools.my_db;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ServiceConversation {
    private final Connection con = my_db.getInstance().getConnection();

    public Conversation findOrCreateConversation(int user1Id, int user2Id) {
        try {
            // ✅ Check existing convo (either order)
            String sql = """
                SELECT * FROM conversation 
                WHERE (user1_id = ? AND user2_id = ?) 
                   OR (user1_id = ? AND user2_id = ?)
            """;
            PreparedStatement ps = con.prepareStatement(sql);
            ps.setInt(1, user1Id);
            ps.setInt(2, user2Id);
            ps.setInt(3, user2Id);
            ps.setInt(4, user1Id);

            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                Conversation convo = new Conversation();
                convo.setId(rs.getInt("id"));
                convo.setUser1Id(rs.getInt("user1_id"));
                convo.setUser2Id(rs.getInt("user2_id"));
                return convo;
            }

            // ✅ Create new if not exists
            String insert = "INSERT INTO conversation (user1_id, user2_id) VALUES (?, ?)";
            PreparedStatement insertStmt = con.prepareStatement(insert, Statement.RETURN_GENERATED_KEYS);
            insertStmt.setInt(1, user1Id);
            insertStmt.setInt(2, user2Id);
            insertStmt.executeUpdate();

            ResultSet keys = insertStmt.getGeneratedKeys();
            if (keys.next()) {
                Conversation convo = new Conversation();
                convo.setId(keys.getInt(1));
                convo.setUser1Id(user1Id);
                convo.setUser2Id(user2Id);
                return convo;
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }
    public List<User> getAllUsersExcept(int userId) {
        List<User> users = new ArrayList<>();
        String sql = "SELECT * FROM user WHERE id != ?";

        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, userId);
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                User u = new User();
                u.setId(rs.getInt("id"));
                u.setName(rs.getString("name"));
                u.setEmail(rs.getString("email"));
                users.add(u);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return users;
    }


    public int getOrCreateConversation(int user1Id, int user2Id) {
        // Always store user1 < user2 for consistency
        int min = Math.min(user1Id, user2Id);
        int max = Math.max(user1Id, user2Id);
        String check = "SELECT id FROM conversation WHERE user1_id = ? AND user2_id = ?";
        String insert = "INSERT INTO conversation (user1_id, user2_id) VALUES (?, ?)";

        try (PreparedStatement ps = con.prepareStatement(check)) {
            ps.setInt(1, min);
            ps.setInt(2, max);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return rs.getInt("id");
        } catch (SQLException e) {
            e.printStackTrace();
        }

        try (PreparedStatement ps = con.prepareStatement(insert, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, min);
            ps.setInt(2, max);
            ps.executeUpdate();
            ResultSet rs = ps.getGeneratedKeys();
            if (rs.next()) return rs.getInt(1);
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return -1;
    }

}
