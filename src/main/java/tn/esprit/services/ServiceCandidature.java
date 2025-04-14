package tn.esprit.services;

import tn.esprit.entities.Candidature;
import tn.esprit.entities.OffreEmploi;
import tn.esprit.entities.StatutCandidature;
import tn.esprit.entities.User;
import tn.esprit.tools.my_db;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ServiceCandidature implements services<Candidature> {

    private final Connection con;

    public ServiceCandidature() {
        this.con = my_db.getInstance().getConnection();
    }

    @Override
    public void ajouter(Candidature c) {
        // Forward to the real method using an offerId.
        throw new UnsupportedOperationException("Use ajouter(Candidature c, int offreId) instead.");
    }

    @Override
    public void ajouter(Candidature c, int offreId) {
        String sql = "INSERT INTO candidature (id_travailleur_id, id_offre_id, statut, date_applied, rating) VALUES (?, ?, ?, ?, ?)";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, 30); // Static worker for now
            ps.setInt(2, offreId); // Selected offer
            ps.setString(3, c.getStatut().name());
            ps.setTimestamp(4, Timestamp.valueOf(c.getDateApplied()));
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

                // Convert statut safely
                String statutFromDB = rs.getString("statut");
                try {
                    c.setStatut(StatutCandidature.valueOf(statutFromDB.toUpperCase()));
                } catch (IllegalArgumentException e) {
                    System.err.println("⚠️ Statut inconnu: " + statutFromDB);
                    c.setStatut(null);
                }

                c.setDateApplied(rs.getTimestamp("date_applied").toLocalDateTime());
                c.setRating(rs.getInt("rating"));

                // NEW: retrieve related user and offer
                int userId = rs.getInt("id_travailleur_id");
                int offreId = rs.getInt("id_offre_id");

                User travailleur = getUserById(userId);
                OffreEmploi offre = getOffreById(offreId);

                c.setUser(travailleur);
                c.setOffre(offre);

                list.add(c);
            }
        } catch (SQLException e) {
            System.out.println("Erreur de récupération: " + e.getMessage());
        }

        return list;
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
            System.err.println("⚠️ Erreur récupération utilisateur: " + e.getMessage());
        }
        return null;
    }

    private OffreEmploi getOffreById(int id) {
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
            System.err.println("⚠️ Erreur récupération offre: " + e.getMessage());
        }
        return null;
    }

    // Retrieves candidatures for a specific offer.
    public List<Candidature> getCandidaturesForOffer(int offerId) {
        List<Candidature> list = new ArrayList<>();
        String sql = "SELECT c.*, u.nom AS user_nom, o.titre AS offre_titre " +
                "FROM candidature c " +
                "JOIN user u ON c.id_travailleur_id = u.id " +
                "JOIN offre_emploi o ON c.id_offre_id = o.id";


        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, offerId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Candidature c = new Candidature();
                    c.setId(rs.getInt("id"));

                    // Convert the 'statut' from the DB to uppercase to match the enum constant.
                    String statutFromDB = rs.getString("statut");
                    try {
                        c.setStatut(StatutCandidature.valueOf(statutFromDB.toUpperCase()));
                    } catch (IllegalArgumentException e) {
                        System.err.println("⚠️ Statut inconnu: " + statutFromDB);
                        c.setStatut(null);
                    }

                    c.setDateApplied(rs.getTimestamp("date_applied").toLocalDateTime());
                    c.setRating(rs.getInt("rating"));
                    list.add(c);
                }
            }
        } catch (SQLException e) {
            System.out.println("Erreur lors de la récupération des candidatures: " + e.getMessage());
        }

        return list;
    }
    public void ajouter(Candidature c, int offreId, int travailleurId) {
        String sql = "INSERT INTO candidature (id_travailleur_id, id_offre_id, statut, date_applied, rating) VALUES (?, ?, ?, ?, ?)";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, travailleurId);
            ps.setInt(2, offreId);
            ps.setString(3, c.getStatut().name());
            ps.setTimestamp(4, Timestamp.valueOf(c.getDateApplied()));
            if (c.getRating() != null) {
                ps.setInt(5, c.getRating());
            } else {
                ps.setNull(5, Types.INTEGER);
            }
            ps.executeUpdate();
            System.out.println("✅ Candidature ajoutée pour l'offre ID " + offreId + ", ouvrier ID " + travailleurId);
        } catch (SQLException e) {
            System.out.println("Erreur lors de l'ajout (offre+ouvrier): " + e.getMessage());
        }
    }

}
