package tn.esprit.services;

import tn.esprit.entities.Livraison;
import tn.esprit.entities.Commande;
import tn.esprit.tools.Database;
import tn.esprit.tools.Database;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ServiceLivraison implements services<Livraison> {

    private final Connection con;

    public ServiceLivraison() {
        this.con = Database.getInstance().getConnection();
    }

    // Méthode de validation des données d'une Livraison
    private boolean isValidLivraison(Livraison livraison) {
        // Validation du statut
        if (livraison.getStatut() == null || livraison.getStatut().trim().isEmpty()) {
            System.out.println("Erreur: Le statut de la livraison ne doit pas être vide.");
            return false;
        }
        if (!livraison.getStatut().matches("En Cours|Livrée|Annulée")) {
            System.out.println("Erreur: Statut de livraison invalide.");
            return false;
        }

        // Validation du transporteur
        if (livraison.getTransporteur() == null || livraison.getTransporteur().trim().isEmpty()) {
            System.out.println("Erreur: Le transporteur ne doit pas être vide.");
            return false;
        }
        if (livraison.getTransporteur().length() < 3 || livraison.getTransporteur().length() > 100) {
            System.out.println("Erreur: Le nom du transporteur doit contenir entre 3 et 100 caractères.");
            return false;
        }

        // Validation du numéro de téléphone du transporteur
        if (livraison.getNumTelTransporteur() == null || livraison.getNumTelTransporteur().trim().isEmpty()) {
            System.out.println("Erreur: Le numéro de téléphone du transporteur ne doit pas être vide.");
            return false;
        }
        if (!livraison.getNumTelTransporteur().matches("^\\+?[0-9\\s\\-]+$")) {
            System.out.println("Erreur: Le numéro de téléphone du transporteur est invalide.");
            return false;
        }

        // Validation de la commande associée
        if (livraison.getCommande() == null || livraison.getCommande().getId() == null) {
            System.out.println("Erreur: La commande associée est obligatoire.");
            return false;
        }

        return true;
    }

    @Override
    public void ajouter(Livraison livraison) {
        // Contrôle de saisie
        if (!isValidLivraison(livraison)) {
            System.out.println("Validation échouée, la livraison n'a pas été ajoutée.");
            return;
        }
        String sql = "INSERT INTO livraison (commande_id, statut, transporteur, num_tel_transporteur, date_livraison) VALUES (?, ?, ?, ?, ?)";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, livraison.getCommande().getId());
            ps.setString(2, livraison.getStatut());
            ps.setString(3, livraison.getTransporteur());
            ps.setString(4, livraison.getNumTelTransporteur());
            ps.setTimestamp(5, Timestamp.valueOf(livraison.getDateLivraison()));

            int rows = ps.executeUpdate();
            if (rows > 0) {
                System.out.println("✅ Livraison ajoutée avec succès !");
            } else {
                System.out.println("❌ Aucun ajout de livraison.");
            }
        } catch (SQLException e) {
            System.out.println("Erreur lors de l'ajout de la livraison: " + e.getMessage());
        }
    }

    @Override
    public void modifier(Livraison livraison) {
        // Contrôle de saisie
        if (!isValidLivraison(livraison)) {
            System.out.println("Validation échouée, la livraison n'a pas été modifiée.");
            return;
        }
        String sql = "UPDATE livraison SET statut = ?, transporteur = ?, num_tel_transporteur = ?, date_livraison = ? WHERE commande_id = ?";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, livraison.getStatut());
            ps.setString(2, livraison.getTransporteur());
            ps.setString(3, livraison.getNumTelTransporteur());
            ps.setTimestamp(4, Timestamp.valueOf(livraison.getDateLivraison()));
            ps.setInt(5, livraison.getCommande().getId());

            int rows = ps.executeUpdate();
            System.out.println(rows > 0
                    ? "✅ Livraison modifiée avec succès !"
                    : "❌ Aucune livraison trouvée pour la commande ID: " + livraison.getCommande().getId());
        } catch (SQLException e) {
            System.out.println("Erreur lors de la modification de la livraison: " + e.getMessage());
        }
    }

    @Override
    public void supprimer(Livraison livraison) {
        String sql = "DELETE FROM livraison WHERE commande_id = ?";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, livraison.getCommande().getId());
            int rows = ps.executeUpdate();
            System.out.println(rows > 0
                    ? "✅ Livraison supprimée avec succès !"
                    : "❌ Aucune livraison trouvée pour la commande ID: " + livraison.getCommande().getId());
        } catch (SQLException e) {
            System.out.println("Erreur lors de la suppression de la livraison: " + e.getMessage());
        }
    }

    @Override
    public List<Livraison> getAll() {
        List<Livraison> livraisons = new ArrayList<>();
        String sql = "SELECT * FROM livraison";
        try (Statement st = con.createStatement();
             ResultSet rs = st.executeQuery(sql)) {

            while (rs.next()) {
                Livraison livraison = new Livraison();
                livraison.setId(rs.getInt("id"));
                Commande commande = new Commande();
                commande.setId(rs.getInt("commande_id"));
                livraison.setCommande(commande);
                livraison.setStatut(rs.getString("statut"));
                livraison.setTransporteur(rs.getString("transporteur"));
                livraison.setNumTelTransporteur(rs.getString("num_tel_transporteur"));
                livraison.setDateLivraison(rs.getTimestamp("date_livraison").toLocalDateTime());

                livraisons.add(livraison);
            }
        } catch (SQLException e) {
            System.out.println("Erreur lors de la récupération des livraisons: " + e.getMessage());
        }
        return livraisons;
    }
}
