package tn.esprit.services;

import tn.esprit.entities.Message;
import tn.esprit.tools.my_db;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class ServiceMessage {
    private final Connection con = my_db.getInstance().getConnection();

    // ✅ Send a new message
    public int sendMessage(Message message) {
        String sql = """
        INSERT INTO message (sender_id, receiver_id, conversation_id, content, created_at, is_read)
        VALUES (?, ?, ?, ?, ?, ?)
    """;

        try (PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, message.getSenderId());
            ps.setInt(2, message.getReceiverId());
            ps.setInt(3, message.getConversationId());
            ps.setString(4, message.getContent());
            ps.setTimestamp(5, Timestamp.valueOf(message.getCreatedAt()));
            ps.setBoolean(6, message.isRead());

            int rows = ps.executeUpdate();

            if (rows > 0) {
                ResultSet rs = ps.getGeneratedKeys();
                if (rs.next()) {
                    return rs.getInt(1); // ✅ return generated ID
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return -1; // ❌ failed
    }

    // ✅ Fetch conversation messages between two users (Java version of Symfony logic)
    public List<Message> getMessagesBetweenUsers(int userId1, int userId2) {
        List<Message> messages = new ArrayList<>();
        String sql = """
        SELECT * FROM message
        WHERE (sender_id = ? AND receiver_id = ?)
           OR (sender_id = ? AND receiver_id = ?)
        ORDER BY created_at ASC
    """;

        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, userId1);
            ps.setInt(2, userId2);
            ps.setInt(3, userId2);
            ps.setInt(4, userId1);
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                Message msg = new Message();
                msg.setId(rs.getInt("id"));
                msg.setSenderId(rs.getInt("sender_id"));
                msg.setReceiverId(rs.getInt("receiver_id"));
                msg.setConversationId(rs.getInt("conversation_id"));
                msg.setContent(rs.getString("content"));
                msg.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
                msg.setRead(rs.getBoolean("is_read"));
                messages.add(msg);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return messages;
    }


    // ✅ Mark all messages from a sender to receiver as read (used in conversation open)
    public void markMessagesAsRead(int senderId, int receiverId) {
        String sql = """
            UPDATE message
            SET is_read = true
            WHERE sender_id = ? AND receiver_id = ? AND is_read = false
        """;

        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, senderId);
            ps.setInt(2, receiverId);
            int rows = ps.executeUpdate();
            System.out.println("📨 Messages marked as read: " + rows);
        } catch (SQLException e) {
            System.err.println("❌ Erreur lors de la mise à jour: " + e.getMessage());
        }
    }

    // ✅ Count unread messages grouped by sender
    public List<int[]> getUnreadCountGroupedBySender(int receiverId) {
        List<int[]> results = new ArrayList<>();
        String sql = """
    SELECT * FROM message
    WHERE (sender_id = ? AND receiver_id = ?)
       OR (sender_id = ? AND receiver_id = ?)
    ORDER BY created_at ASC, id ASC
""";

        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, receiverId);
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                results.add(new int[]{rs.getInt("sender_id"), rs.getInt("unread_count")});
            }
        } catch (SQLException e) {
            System.err.println("❌ Erreur lors du comptage des messages non lus: " + e.getMessage());
        }

        return results;
    }
}
