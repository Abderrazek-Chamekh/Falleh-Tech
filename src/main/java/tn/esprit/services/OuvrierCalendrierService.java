package tn.esprit.services;

import tn.esprit.entities.OuvrierCalendrier;
import tn.esprit.entities.User;
import tn.esprit.tools.my_db;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class OuvrierCalendrierService {

    private final Connection connection;

    public OuvrierCalendrierService() {
        this.connection = my_db.getInstance().getConnection();
    }

    // ✅ Check if an accepted candidature exists in the same date range
    public boolean hasConflict(User ouvrier, LocalDateTime start, LocalDateTime end) {
        String query = "SELECT COUNT(*) FROM ouvrier_calendrier WHERE ouvrier_id = ? " +
                "AND status = 'acceptee' " +
                "AND ((start_date <= ? AND end_date >= ?) " +
                "OR (start_date >= ? AND start_date <= ?))";

        try (PreparedStatement ps = connection.prepareStatement(query)) {
            ps.setInt(1, ouvrier.getId());
            ps.setTimestamp(2, Timestamp.valueOf(end));
            ps.setTimestamp(3, Timestamp.valueOf(start));
            ps.setTimestamp(4, Timestamp.valueOf(start));
            ps.setTimestamp(5, Timestamp.valueOf(end));

            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return rs.getInt(1) > 0;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    // ✅ Add if no conflict OR status is not 'acceptee'
    public boolean addCalendrier(OuvrierCalendrier calendrier) {
        if (hasConflict(calendrier.getOuvrier(), calendrier.getStartDate(), calendrier.getEndDate())
                && calendrier.getStatus().equalsIgnoreCase("acceptee")) {
            System.out.println("❌ You already have an accepted candidature during this time.");
            return false;
        }

        String query = "INSERT INTO ouvrier_calendrier (ouvrier_id, candidature_id, start_date, end_date, status) " +
                "VALUES (?, ?, ?, ?, ?)";

        try (PreparedStatement ps = connection.prepareStatement(query)) {
            ps.setInt(1, calendrier.getOuvrier().getId());
            ps.setInt(2, calendrier.getCandidature().getId());
            ps.setTimestamp(3, Timestamp.valueOf(calendrier.getStartDate()));
            ps.setTimestamp(4, Timestamp.valueOf(calendrier.getEndDate()));
            ps.setString(5, calendrier.getStatus().toLowerCase());

            ps.executeUpdate();
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // ✅ Get all calendar events for an ouvrier
    public List<OuvrierCalendrier> getCalendrierByUser(int ouvrierId) {
        List<OuvrierCalendrier> list = new ArrayList<>();
        String query = "SELECT * FROM ouvrier_calendrier WHERE ouvrier_id = ?";

        try (PreparedStatement ps = connection.prepareStatement(query)) {
            ps.setInt(1, ouvrierId);
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                OuvrierCalendrier cal = new OuvrierCalendrier();
                cal.setStartDate(rs.getTimestamp("start_date").toLocalDateTime());
                cal.setEndDate(rs.getTimestamp("end_date").toLocalDateTime());
                cal.setStatus(rs.getString("status").toLowerCase());

                // Dummy user mapping
                User user = new User();
                user.setId(rs.getInt("ouvrier_id"));
                cal.setOuvrier(user);

                list.add(cal);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        System.out.println("🔍 Fetching calendrier for ouvrier ID: " + ouvrierId);
        System.out.println("📋 Result size: " + list.size());
        for (OuvrierCalendrier cal : list) {
            System.out.println("🗓 " + cal.getStartDate() + " → " + cal.getEndDate() + " | status=" + cal.getStatus());
        }

        return list;
    }

    // ✅ Check for overlapping accepted entries (status = 'acceptee')
    public boolean hasAcceptedOverlap(int ouvrierId, LocalDateTime newStart, LocalDateTime newEnd) {
        String sql = """
            SELECT COUNT(*) FROM ouvrier_calendrier 
            WHERE ouvrier_id = ? 
            AND status = 'acceptee'
            AND (
                (start_date <= ? AND end_date >= ?) OR
                (start_date <= ? AND end_date >= ?) OR
                (start_date >= ? AND end_date <= ?)
            )
        """;

        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, ouvrierId);
            ps.setTimestamp(2, Timestamp.valueOf(newStart));
            ps.setTimestamp(3, Timestamp.valueOf(newStart));
            ps.setTimestamp(4, Timestamp.valueOf(newEnd));
            ps.setTimestamp(5, Timestamp.valueOf(newEnd));
            ps.setTimestamp(6, Timestamp.valueOf(newStart));
            ps.setTimestamp(7, Timestamp.valueOf(newEnd));

            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return rs.getInt(1) > 0;
            }
        } catch (SQLException e) {
            System.err.println("❌ Erreur vérification overlap: " + e.getMessage());
        }
        return false;
    }

    // ✅ Insert with correct lowercase status
    public void ajouterCalendrier(int ouvrierId, int candidatureId, LocalDateTime start, LocalDateTime end, String status) {
        String sql = """
        INSERT INTO ouvrier_calendrier (ouvrier_id, candidature_id, start_date, end_date, status)
        VALUES (?, ?, ?, ?, ?)
        """;

        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, ouvrierId);
            ps.setInt(2, candidatureId);
            ps.setTimestamp(3, Timestamp.valueOf(start));
            ps.setTimestamp(4, Timestamp.valueOf(end));
            ps.setString(5, status.toLowerCase());

            ps.executeUpdate();
            System.out.println("✅ OuvrierCalendrier ajouté avec succès.");
        } catch (SQLException e) {
            System.err.println("❌ Erreur lors de l'ajout du calendrier: " + e.getMessage());
        }
    }
}
