package tn.esprit.services;

import tn.esprit.entities.Candidature;
import tn.esprit.entities.OffreEmploi;
import tn.esprit.entities.StatutCandidature;
import tn.esprit.entities.User;
import tn.esprit.tools.my_db;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class ServiceCandidature implements services<Candidature> {

    private final Connection con;
    private final OuvrierCalendrierService calendrierService = new OuvrierCalendrierService();

    public ServiceCandidature() {
        this.con = my_db.getInstance().getConnection();
    }

    // === CREATE ===
    @Override
    public void ajouter(Candidature c) {
        throw new UnsupportedOperationException("Use ajouter(Candidature c, int offreId, int travailleurId) instead.");
    }

    public void ajouter(Candidature c, int offreId, int travailleurId, LocalDateTime offerStart, LocalDateTime offerEnd) {
        OffreEmploi offre = getOffreById(offreId);
        if (offre == null) {
            System.out.println("❌ Offre introuvable (ID " + offreId + ")");
            return;
        }

        c.setOffre(offre);

        // ✅ NEW: check for conflict before applying (for any status)
        System.out.println("📌 Applying to offer ID: " + offreId);
        System.out.println("📅 Offer period passed to ajout(): " + offerStart + " ➡ " + offerEnd);

        if (hasAcceptedOverlap(travailleurId, offerStart, offerEnd)) {
            System.out.println("❌ Conflit détecté : vous avez déjà une offre acceptée durant cette période.");
            return;
        }

        String sql = "INSERT INTO candidature (id_travailleur_id, id_offre_id, statut, date_applied, rating) VALUES (?, ?, ?, ?, ?)";
        try (PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, travailleurId);
            ps.setInt(2, offreId);
            ps.setString(3, c.getStatut().toString());
            ps.setTimestamp(4, Timestamp.valueOf(c.getDateApplied()));
            if (c.getRating() != null) ps.setInt(5, c.getRating());
            else ps.setNull(5, Types.INTEGER);

            ps.executeUpdate();

            ResultSet rs = ps.getGeneratedKeys();
            if (rs.next() && c.getStatut() == StatutCandidature.ACCEPTEE) {
                int lastId = rs.getInt(1);
                calendrierService.ajouterCalendrier(travailleurId, lastId, offerStart, offerEnd, "accepted");
            }

        } catch (SQLException e) {
            System.out.println("Erreur lors de l'ajout: " + e.getMessage());
        }
    }


    // === UPDATE ===
    @Override
    public void modifier(Candidature c) {
        try {
            OffreEmploi offre = getOffreById(c.getOffre().getId());
            if (offre == null) {
                System.out.println("❌ Offre introuvable pour ID " + c.getId());
                return;
            }

            c.setOffre(offre);
            LocalDateTime start = offre.getStartDate().atStartOfDay();
            LocalDateTime end = offre.getDateExpiration().atTime(23, 59);

            if (c.getStatut() == StatutCandidature.ACCEPTEE &&
                    calendrierService.hasAcceptedOverlap(c.getUser().getId(), start, end)) {
                System.out.println("❌ Conflit de calendrier.");
                return;
            }

            String sql = "UPDATE candidature SET statut = ?, rating = ? WHERE id = ?";
            try (PreparedStatement ps = con.prepareStatement(sql)) {
                ps.setString(1, c.getStatut().toString());
                if (c.getRating() != null) ps.setInt(2, c.getRating());
                else ps.setNull(2, Types.INTEGER);
                ps.setInt(3, c.getId());
                ps.executeUpdate();

                if (c.getStatut() == StatutCandidature.ACCEPTEE) {
                    calendrierService.ajouterCalendrier(c.getUser().getId(), c.getId(), start, end, "accepted");
                }
            }

        } catch (SQLException e) {
            System.out.println("Erreur lors de la modification: " + e.getMessage());
        }
    }

    public void updateStatut(int id, StatutCandidature newStatut) {
        try {
            String sql = "UPDATE candidature SET statut = ? WHERE id = ?";
            PreparedStatement ps = con.prepareStatement(sql);
            ps.setString(1, newStatut.toString());
            ps.setInt(2, id);
            ps.executeUpdate();

            if (newStatut == StatutCandidature.ACCEPTEE) {
                Candidature c = getById(id);
                if (c != null && c.getOffre() != null) {
                    calendrierService.ajouterCalendrier(
                            c.getUser().getId(),
                            c.getId(),
                            c.getOffre().getStartDate().atStartOfDay(),
                            c.getOffre().getDateExpiration().atStartOfDay(),
                            "accepted"
                    );
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // === DELETE ===
    @Override
    public void supprimer(Candidature c) {
        supprimer(c.getId());
    }

    public void supprimer(int id) {
        String sql = "DELETE FROM candidature WHERE id = ?";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, id);
            ps.executeUpdate();
            System.out.println("❌ Candidature supprimée ID: " + id);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // === RETRIEVE ===
    @Override
    public List<Candidature> getAll() {
        List<Candidature> list = new ArrayList<>();
        String sql = "SELECT * FROM candidature";

        try (Statement st = con.createStatement(); ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) list.add(mapCandidature(rs));
        } catch (SQLException e) {
            System.out.println("Erreur récupération: " + e.getMessage());
        }

        return list;
    }

    public List<Candidature> getByUserId(int userId) {
        List<Candidature> list = new ArrayList<>();
        String sql = "SELECT * FROM candidature WHERE id_travailleur_id = ?";

        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, userId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                Candidature c = mapCandidature(rs);
                c.setOffre(getOffreById(rs.getInt("id_offre_id")));
                list.add(c);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return list;
    }

    public List<Candidature> getByOffreId(int offreId) {
        List<Candidature> list = new ArrayList<>();
        String sql = """
            SELECT 
                c.id AS candidature_id,
                c.date_applied,
                c.statut,
                u.id AS user_id,
                u.name,
                u.email,
                u.experience
            FROM candidature c
            JOIN user u ON c.id_travailleur_id = u.id
            WHERE c.id_offre_id = ?
        """;

        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, offreId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                Candidature c = new Candidature();
                c.setId(rs.getInt("candidature_id"));
                c.setDateApplied(rs.getTimestamp("date_applied").toLocalDateTime());
                c.setStatut(StatutCandidature.fromString(rs.getString("statut")));

                User ouvrier = new User();
                ouvrier.setId(rs.getInt("user_id"));
                ouvrier.setName(rs.getString("name"));
                ouvrier.setEmail(rs.getString("email"));
                ouvrier.setExperience(rs.getString("experience"));

                c.setUser(ouvrier);
                list.add(c);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return list;
    }

    public Candidature getById(int id) {
        String sql = "SELECT * FROM candidature WHERE id = ?";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return mapCandidature(rs);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public Candidature getCandidatureByUserAndOffre(int userId, int offreId) {
        String sql = "SELECT * FROM candidature WHERE id_travailleur_id = ? AND id_offre_id = ?";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, userId);
            ps.setInt(2, offreId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return mapCandidature(rs);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    // === HELPERS ===
    private Candidature mapCandidature(ResultSet rs) throws SQLException {
        Candidature c = new Candidature();
        c.setId(rs.getInt("id"));
        c.setStatut(StatutCandidature.fromString(rs.getString("statut")));
        c.setDateApplied(rs.getTimestamp("date_applied").toLocalDateTime());
        c.setRating(rs.getInt("rating"));
        c.setUser(getUserById(rs.getInt("id_travailleur_id")));
        c.setOffre(getOffreById(rs.getInt("id_offre_id")));
        return c;
    }

    private User getUserById(int id) {
        String sql = "SELECT * FROM user WHERE id = ?";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                User u = new User();
                u.setId(rs.getInt("id"));
                u.setName(rs.getString("name"));
                u.setEmail(rs.getString("email"));
                return u;
            }
        } catch (SQLException e) {
            System.err.println("Erreur récupération utilisateur: " + e.getMessage());
        }
        return null;
    }

    public OffreEmploi getOffreById(int id) {
        String sql = "SELECT * FROM offre_emploi WHERE id = ?";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                OffreEmploi o = new OffreEmploi();
                o.setId(rs.getInt("id"));
                o.setTitre(rs.getString("titre"));
                return o;
            }
        } catch (SQLException e) {
            System.err.println("Erreur récupération offre: " + e.getMessage());
        }
        return null;
    }

    public List<Candidature> getCandidaturesForOffer(int offerId) {
        List<Candidature> list = new ArrayList<>();
        String sql = """
        SELECT c.*, u.nom AS user_nom, o.titre AS offre_titre 
        FROM candidature c 
        JOIN user u ON c.id_travailleur_id = u.id 
        JOIN offre_emploi o ON c.id_offre_id = o.id
        WHERE c.id_offre_id = ?
    """;

        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, offerId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Candidature c = new Candidature();
                    c.setId(rs.getInt("id"));
                    c.setDateApplied(rs.getTimestamp("date_applied").toLocalDateTime());
                    c.setRating(rs.getInt("rating"));
                    c.setStatut(StatutCandidature.fromString(rs.getString("statut")));

                    User u = new User();
                    u.setId(rs.getInt("id_travailleur_id"));
                    u.setName(rs.getString("user_nom"));
                    c.setUser(u);

                    OffreEmploi o = new OffreEmploi();
                    o.setId(rs.getInt("id_offre_id"));
                    o.setTitre(rs.getString("offre_titre"));
                    c.setOffre(o);

                    list.add(c);
                }
            }
        } catch (SQLException e) {
            System.out.println("Erreur lors de la récupération des candidatures: " + e.getMessage());
        }

        return list;
    }
    public boolean hasAcceptedOverlap(int userId, LocalDateTime newStart, LocalDateTime newEnd) {
        String sql = """
        SELECT o.start_date, o.date_expiration
        FROM candidature c
        JOIN offre_emploi o ON c.id_offre_id = o.id
        WHERE c.id_travailleur_id = ?
        AND c.statut = 'ACCEPTEE'
        AND (
            o.start_date < ? AND o.date_expiration > ?
        )
    """;

        try (PreparedStatement ps = con.prepareStatement(sql)) {
            java.sql.Date sqlNewEnd = java.sql.Date.valueOf(newEnd.toLocalDate());
            java.sql.Date sqlNewStart = java.sql.Date.valueOf(newStart.toLocalDate());

            ps.setInt(1, userId);
            ps.setDate(2, sqlNewEnd);
            ps.setDate(3, sqlNewStart);

            System.out.println("🕵️‍♂️ Checking overlap for user ID: " + userId);
            System.out.println("🟡 New Job Start: " + sqlNewStart + ", End: " + sqlNewEnd);

            ResultSet rs = ps.executeQuery();
            int conflictCount = 0;

            while (rs.next()) {
                Date start = rs.getDate("start_date");
                Date end = rs.getDate("date_expiration");
                conflictCount++;
                System.out.println("❗ Conflict with existing accepted job: " + start + " ➡ " + end);
            }

            System.out.println("🔍 Total conflicts found: " + conflictCount);
            return conflictCount > 0;

        } catch (SQLException e) {
            System.err.println("❌ SQL Error in hasAcceptedOverlap: " + e.getMessage());
        }

        return false;
    }
    public int countCandidaturesByGovernorate(String governorate) {
        int count = 0;
        String sql = """
        SELECT COUNT(*) AS total 
        FROM candidature c
        JOIN offre_emploi o ON c.id_offre_id = o.id
        WHERE o.lieu = ?
    """;

        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, governorate);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                count = rs.getInt("total");
            }
        } catch (SQLException e) {
            System.out.println("Erreur lors du comptage des candidatures: " + e.getMessage());
        }
        return count;
    }

}
