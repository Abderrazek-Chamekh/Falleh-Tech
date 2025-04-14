package tn.esprit.services;

import tn.esprit.entities.OffreEmploi;
import tn.esprit.entities.User;
import tn.esprit.tools.my_db;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ServiceOffreEmploi implements services<OffreEmploi> {

    private final Connection con;

    public ServiceOffreEmploi() {
        this.con = my_db.getInstance().getConnection();
    }

    @Override
    public void ajouter(OffreEmploi offre) {
        String sql = "INSERT INTO offre_emploi (titre, description, salaire, lieu, start_date, date_expiration, id_employeur_id) VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, offre.getTitre());
            ps.setString(2, offre.getDescription());
            ps.setFloat(3, offre.getSalaire());
            ps.setString(4, offre.getLieu());
            ps.setDate(5, Date.valueOf(offre.getStartDate()));
            ps.setDate(6, Date.valueOf(offre.getDateExpiration()));
            ps.setInt(7, 30); // 🔒 Static employer ID
            ps.executeUpdate();
            System.out.println("OffreEmploi ajoutée avec succès !");
        } catch (SQLException e) {
            System.out.println("Erreur lors de l'ajout de l'offre: " + e.getMessage());
        }
    }

    @Override
    public void modifier(OffreEmploi offre) {
        String sql = "UPDATE offre_emploi SET titre = ?, description = ?, salaire = ?, lieu = ?, start_date = ?, date_expiration = ?, id_employeur_id = ? WHERE id = ?";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, offre.getTitre());
            ps.setString(2, offre.getDescription());
            ps.setFloat(3, offre.getSalaire());
            ps.setString(4, offre.getLieu());
            ps.setDate(5, Date.valueOf(offre.getStartDate()));
            ps.setDate(6, Date.valueOf(offre.getDateExpiration()));
            ps.setInt(7, 30); // 🔒 Always assign to user ID 5
            ps.setInt(8, offre.getId());
            int rows = ps.executeUpdate();
            System.out.println(rows > 0 ? "Offre modifiée avec succès !" : "Aucune offre trouvée avec ID: " + offre.getId());
        } catch (SQLException e) {
            System.out.println("Erreur lors de la modification: " + e.getMessage());
        }
    }

    @Override
    public void supprimer(OffreEmploi offre) {
        try {
            // Étape 1: Supprimer les candidatures associées
            String deleteCandidaturesSQL = "DELETE FROM candidature WHERE id_offre_id = ?";
            try (PreparedStatement psCand = con.prepareStatement(deleteCandidaturesSQL)) {
                psCand.setInt(1, offre.getId());
                int candDeleted = psCand.executeUpdate();
                System.out.println("🗑️ Candidatures supprimées: " + candDeleted);
            }

            // Étape 2: Supprimer l'offre
            String deleteOffreSQL = "DELETE FROM offre_emploi WHERE id = ?";
            try (PreparedStatement psOffre = con.prepareStatement(deleteOffreSQL)) {
                psOffre.setInt(1, offre.getId());
                int rows = psOffre.executeUpdate();
                System.out.println(rows > 0 ? "✅ Offre supprimée avec succès !" : "⚠️ Aucune offre trouvée avec ID: " + offre.getId());
            }

        } catch (SQLException e) {
            System.out.println("❌ Erreur lors de la suppression de l'offre: " + e.getMessage());
        }
    }

    public List<OffreEmploi> getAll() {
        List<OffreEmploi> offres = new ArrayList<>();
        String sql = "SELECT * FROM offre_emploi";
        try (Statement st = con.createStatement();
             ResultSet rs = st.executeQuery(sql)) {

            while (rs.next()) {
                OffreEmploi o = new OffreEmploi();
                o.setId(rs.getInt("id"));
                o.setTitre(rs.getString("titre"));
                o.setDescription(rs.getString("description"));
                o.setSalaire(rs.getFloat("salaire"));
                o.setLieu(rs.getString("lieu"));
                o.setStartDate(rs.getDate("start_date").toLocalDate());
                o.setDateExpiration(rs.getDate("date_expiration").toLocalDate());

                // 🧍 Static user with ID = 55
                User staticUser = new User();
                staticUser.setId(55);
                o.setIdEmployeur(staticUser);

                offres.add(o);
            }

        } catch (SQLException e) {
            System.out.println("Erreur lors de la récupération des offres: " + e.getMessage());
        }
        return offres;
    }
}
