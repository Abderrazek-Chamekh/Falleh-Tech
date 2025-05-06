package tn.esprit.services;

import tn.esprit.entities.Candidature;
import tn.esprit.tools.Database;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import tn.esprit.entities.StatutCandidature; // ✅ correct


public class ServiceCandidature implements services<Candidature> {

    private final Connection con;

    public ServiceCandidature() {
        this.con = Database.getInstance().getConnection();
    }
    @Override
    public void ajouter(Candidature c) {
        // Just forward to the real method or throw an exception if unused
        throw new UnsupportedOperationException("Use ajouter(Candidature c, int offreId) instead.");
    }

    @Override
    public void ajouter(Candidature c, int offreId) {
        String sql = "INSERT INTO candidature (id_travailleur_id" +
                ", id_offre_id, statut, date_applied, rating) VALUES (?, ?, ?, ?, ?)";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, 30); // Static worker for now
            ps.setInt(2, offreId); // Selected offer
            ps.setString(3, c.getStatut().name());
            ps.setTimestamp(4, Timestamp.valueOf(LocalDateTime.now()));
            if (c.getRating() != null) {
                ps.setInt(5, c.getRating());
            } else {
                ps.setNull(5, Types.INTEGER);
            }
            ps.executeUpdate();
            System.out.println("✅ Candidature ajoutée pour l'offre ID: " + offreId);
        } catch (SQLException e) {
            System.out.println("Erreur lors de l'ajout: " + e.getMessage());
        }
    }


    @Override
    public void modifier(Candidature c) {
        String sql = "UPDATE candidature SET statut = ?, rating = ? WHERE id = ?";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, c.getStatut().name());
            if (c.getRating() != null) {
                ps.setInt(2, c.getRating());
            } else {
                ps.setNull(2, Types.INTEGER);
            }
            ps.setInt(3, c.getId());
            int rows = ps.executeUpdate();
            System.out.println(rows > 0 ? "✅ Candidature modifiée." : "Aucune candidature trouvée.");
        } catch (SQLException e) {
            System.out.println("Erreur lors de la modification: " + e.getMessage());
        }
    }

    @Override
    public void supprimer(Candidature c) {
        String sql = "DELETE FROM candidature WHERE id = ?";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, c.getId());
            int rows = ps.executeUpdate();
            System.out.println(rows > 0 ? "✅ Candidature supprimée." : "Aucune candidature trouvée.");
        } catch (SQLException e) {
            System.out.println("Erreur lors de la suppression: " + e.getMessage());
        }
    }

    @Override
    public List<Candidature> getAll() {
        List<Candidature> list = new ArrayList<>();
        String sql = "SELECT * FROM candidature";

        try (Statement st = con.createStatement(); ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                Candidature c = new Candidature();

                c.setId(rs.getInt("id"));

                // 🔁 Make enum parsing case-insensitive
                String statutValue = rs.getString("statut");
                if (statutValue != null) {
                    try {
                        c.setStatut(StatutCandidature.valueOf(statutValue.toUpperCase()));
                    } catch (IllegalArgumentException e) {
                        System.out.println("⚠️ Statut inconnu: " + statutValue);
                        continue; // skip this record if statut is invalid
                    }
                }

                c.setDateApplied(rs.getTimestamp("date_applied").toLocalDateTime());

                int rating = rs.getInt("rating");
                c.setRating(rs.wasNull() ? null : rating);

                list.add(c);
            }
        } catch (SQLException e) {
            System.out.println("Erreur de récupération: " + e.getMessage());
        }

        return list;
    }

}
