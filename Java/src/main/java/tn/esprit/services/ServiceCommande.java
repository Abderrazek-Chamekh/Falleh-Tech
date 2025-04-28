package tn.esprit.services;

import tn.esprit.entities.Commande;
import tn.esprit.entities.Produit;
import tn.esprit.entities.User;
import tn.esprit.entities.CommandeProduit;
import tn.esprit.tools.Database;
import tn.esprit.tools.Database;

import java.math.BigDecimal;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ServiceCommande implements services<Commande> {

    private final Connection con;

    public ServiceCommande() {
        this.con = Database.getInstance().getConnection();
    }

    // Méthode de validation des données d'une Commande
    private boolean isValidCommande(Commande commande) {
        // Validation du montant total : doit être un nombre positif
        if (commande.getTotal() == null || commande.getTotal() <= 0) {
            System.out.println("Erreur: Le total doit être un nombre positif.");
            return false;
        }

        // Validation de la date de création : ne doit pas être nulle
        if (commande.getDateCreation() == null) {
            System.out.println("Erreur: La date de création ne peut pas être nulle.");
            return false;
        }

        // Validation du statut : doit être l'un des statuts autorisés
        if (commande.getStatus() != null && !commande.getStatus().matches("En Attente|Confirmée|Annulée|Remboursée")) {
            System.out.println("Erreur: Statut invalide.");
            return false;
        }

        // Validation de l'adresse de livraison : ne doit pas être vide
        if (commande.getAdresseLivraison() == null || commande.getAdresseLivraison().trim().isEmpty()) {
            System.out.println("Erreur: L'adresse de livraison ne peut pas être vide.");
            return false;
        }

        // Validation du mode de paiement : doit être l'un des modes autorisés
        if (commande.getModePaiement() == null || !commande.getModePaiement().matches("Espèces|Carte_Bancaire|e_DINAR")) {
            System.out.println("Erreur: Mode de paiement invalide.");
            return false;
        }

        // Validation du statut de paiement : doit être l'un des statuts autorisés
        if (commande.getStatusPaiement() == null || !commande.getStatusPaiement().matches("En Attente|Payé|Échoué|Remboursé")) {
            System.out.println("Erreur: Statut de paiement invalide.");
            return false;
        }

        return true;
    }

    @Override
    public void ajouter(Commande commande) {
        // Contrôle de saisie
        if (!isValidCommande(commande)) {
            System.out.println("Validation échouée, la commande n'a pas été ajoutée.");
            return;
        }

        String sql = "INSERT INTO commande (date_creation, total, status, adresse_livraison, mode_paiement, date_paiement, status_paiement, user_id) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setTimestamp(1, Timestamp.valueOf(commande.getDateCreation()));
            ps.setFloat(2, commande.getTotal());
            ps.setString(3, commande.getStatus());
            ps.setString(4, commande.getAdresseLivraison());
            ps.setString(5, commande.getModePaiement());
            ps.setTimestamp(6, commande.getDatePaiement() != null ? Timestamp.valueOf(commande.getDatePaiement()) : null);
            ps.setString(7, commande.getStatusPaiement());
            ps.setInt(8, commande.getUser().getId());

            int rows = ps.executeUpdate();
            if (rows > 0) {
                System.out.println("✅ Commande ajoutée avec succès !");
            } else {
                System.out.println("❌ Aucun ajout de commande.");
            }
        } catch (SQLException e) {
            System.out.println("Erreur lors de l'ajout de la commande: " + e.getMessage());
        }
    }

    @Override
    public void modifier(Commande commande) {
        // Contrôle de saisie
        if (!isValidCommande(commande)) {
            System.out.println("Validation échouée, la commande n'a pas été modifiée.");
            return;
        }

        String sql = "UPDATE commande SET date_creation = ?, total = ?, status = ?, adresse_livraison = ?, mode_paiement = ?, date_paiement = ?, status_paiement = ?, user_id = ? WHERE id = ?";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setTimestamp(1, Timestamp.valueOf(commande.getDateCreation()));
            ps.setFloat(2, commande.getTotal());
            ps.setString(3, commande.getStatus());
            ps.setString(4, commande.getAdresseLivraison());
            ps.setString(5, commande.getModePaiement());
            ps.setTimestamp(6, commande.getDatePaiement() != null ? Timestamp.valueOf(commande.getDatePaiement()) : null);
            ps.setString(7, commande.getStatusPaiement());
            ps.setInt(8, commande.getUser().getId());
            ps.setInt(9, commande.getId());

            int rows = ps.executeUpdate();
            System.out.println(rows > 0
                    ? "✅ Commande modifiée avec succès !"
                    : "❌ Aucun résultat trouvé pour l'ID: " + commande.getId());
        } catch (SQLException e) {
            System.out.println("Erreur lors de la modification de la commande: " + e.getMessage());
        }
    }

    @Override
    public void supprimer(Commande commande) {
        String sql = "DELETE FROM commande WHERE id = ?";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, commande.getId());
            int rows = ps.executeUpdate();
            System.out.println(rows > 0
                    ? "✅ Commande supprimée avec succès !"
                    : "❌ Aucune commande trouvée avec l'ID: " + commande.getId());
        } catch (SQLException e) {
            System.out.println("Erreur lors de la suppression de la commande: " + e.getMessage());
        }
    }

    @Override
    public List<Commande> getAll() {
        List<Commande> commandes = new ArrayList<>();
        String sql = "SELECT c.*, u.name as user_name FROM commande c " +
                "LEFT JOIN user u ON c.user_id = u.id";

        try (Statement st = con.createStatement();
             ResultSet rs = st.executeQuery(sql)) {

            while (rs.next()) {
                Commande c = new Commande();
                c.setId(rs.getInt("id"));
                c.setDateCreation(rs.getTimestamp("date_creation").toLocalDateTime());
                c.setTotal(rs.getFloat("total"));
                c.setStatus(rs.getString("status"));
                c.setAdresseLivraison(rs.getString("adresse_livraison"));
                c.setModePaiement(rs.getString("mode_paiement"));
                c.setDatePaiement(rs.getTimestamp("date_paiement") != null ?
                        rs.getTimestamp("date_paiement").toLocalDateTime() : null);
                c.setStatusPaiement(rs.getString("status_paiement"));

                User u = new User();
                u.setId(rs.getInt("user_id"));
                u.setName(rs.getString("user_name"));
                c.setUser(u);

                commandes.add(c);
            }
        } catch (SQLException e) {
            System.out.println("Erreur lors de la récupération des commandes: " + e.getMessage());
        }
        return commandes;
    }

    public Commande getCommandeById(int id) {
        Commande commande = null;
        String sql = "SELECT * FROM commande WHERE id = ?";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    commande = new Commande();
                    commande.setId(rs.getInt("id"));
                    commande.setDateCreation(rs.getTimestamp("date_creation").toLocalDateTime());
                    commande.setTotal(rs.getFloat("total"));
                    commande.setStatus(rs.getString("status"));
                    commande.setAdresseLivraison(rs.getString("adresse_livraison"));
                    commande.setModePaiement(rs.getString("mode_paiement"));
                    commande.setDatePaiement(rs.getTimestamp("date_paiement") != null ? rs.getTimestamp("date_paiement").toLocalDateTime() : null);
                    commande.setStatusPaiement(rs.getString("status_paiement"));

                    User u = new User();
                    u.setId(rs.getInt("user_id"));
                    commande.setUser(u);
                }
            }
        } catch (SQLException e) {
            System.out.println("Erreur lors de la récupération de la commande: " + e.getMessage());
        }
        return commande;
    }
    public List<Commande> getCommandesByUserId(int userId) {
        List<Commande> commandes = new ArrayList<>();
        String sql = "SELECT * FROM commande WHERE user_id = ?";

        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Commande commande = new Commande();
                    commande.setId(rs.getInt("id"));
                    commande.setDateCreation(rs.getTimestamp("date_creation").toLocalDateTime());
                    commande.setTotal(rs.getFloat("total"));
                    commande.setStatus(rs.getString("status"));
                    commande.setAdresseLivraison(rs.getString("adresse_livraison"));
                    commande.setModePaiement(rs.getString("mode_paiement"));
                    commande.setDatePaiement(rs.getTimestamp("date_paiement") != null ? rs.getTimestamp("date_paiement").toLocalDateTime() : null);
                    commande.setStatusPaiement(rs.getString("status_paiement"));

                    User u = new User();
                    u.setId(rs.getInt("user_id"));
                    commande.setUser(u);

                    commandes.add(commande);
                }
            }
        } catch (SQLException e) {
            System.out.println("Erreur lors de la récupération des commandes: " + e.getMessage());
        }

        return commandes;
    }

    public int createOrderFromCart(int userId, String adresseLivraison, String paymentMethod) throws SQLException {
        Connection conn = null;
        PreparedStatement psCommande = null;
        PreparedStatement psLigne = null;
        ResultSet rs = null;

        try {
            conn = Database.getInstance().getConnection();
            conn.setAutoCommit(false);

            // 1. Insert Commande
            String sqlCommande = "INSERT INTO commande (user_id, total, adresse_livraison, mode_paiement, "
                    + "status, status_paiement, date_creation, date_paiement) "
                    + "VALUES (?, ?, ?, ?, ?, ?, NOW(), ?)";

            psCommande = conn.prepareStatement(sqlCommande, Statement.RETURN_GENERATED_KEYS);

            // Set all parameters in correct order
            psCommande.setInt(1, userId);
            psCommande.setDouble(2, PanierService.getInstance().getTotal());
            psCommande.setString(3, adresseLivraison);
            psCommande.setString(4, paymentMethod);
            psCommande.setString(5, "En Attente"); // status
            psCommande.setString(6, "Espèces".equals(paymentMethod) ? "Payé" : "En Attente"); // status_paiement
            psCommande.setTimestamp(7, "Espèces".equals(paymentMethod) ? new Timestamp(System.currentTimeMillis()) : null); // date_paiement

            psCommande.executeUpdate();
            rs = psCommande.getGeneratedKeys();

            if (!rs.next()) {
                throw new SQLException("Failed to get generated commande ID");
            }
            int commandeId = rs.getInt(1);

            // 2. Insert Order Lines
            String sqlLigne = "INSERT INTO commande_produit (commande_id, produit_id, quantite, prix_unitaire, prix_total) "
                    + "VALUES (?, ?, ?, ?, ?)";

            psLigne = conn.prepareStatement(sqlLigne);
            for (Map.Entry<Produit, Integer> entry : PanierService.getInstance().getPanier().entrySet()) {
                Produit p = entry.getKey();
                int qty = entry.getValue();
                double prixTotal = p.getPrix().doubleValue() * qty;

                psLigne.setInt(1, commandeId);
                psLigne.setLong(2, p.getId());
                psLigne.setInt(3, qty);
                psLigne.setDouble(4, p.getPrix().doubleValue());
                psLigne.setDouble(5, prixTotal);
                psLigne.addBatch();
            }
            psLigne.executeBatch();

            conn.commit();
            return commandeId;

        } catch (SQLException e) {
            if (conn != null) conn.rollback();
            throw e;
        } finally {
            // Close resources in reverse order
            if (rs != null) rs.close();
            if (psLigne != null) psLigne.close();
            if (psCommande != null) psCommande.close();
            con.setAutoCommit(true);
        }
    }

    private int insertCommande(Connection conn, int userId, String adresseLivraison, String paymentMethod)
            throws SQLException {

        String sql = "INSERT INTO commande (user_id, total, adresse_livraison, mode_paiement, " +
                "status, status_paiement, date_creation, date_paiement) " +
                "VALUES (?, ?, ?, ?, 'En Attente', ?, NOW(), ?)";

        try (PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            // Set parameters
            ps.setInt(1, userId);
            ps.setDouble(2, PanierService.getInstance().getTotal()); // Using your existing getTotal()
            ps.setString(3, adresseLivraison);
            ps.setString(4, paymentMethod);
            ps.setString(5, "Espèces".equals(paymentMethod) ? "Payé" : "En Attente");
            ps.setTimestamp(6, "Espèces".equals(paymentMethod) ? Timestamp.valueOf(LocalDateTime.now()) : null);

            ps.executeUpdate();

            // Get generated ID
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        }
        throw new SQLException("Failed to get generated commande ID");
    }

    private void insertOrderLines(Connection conn, int commandeId) throws SQLException {
        String sqlLigne = "INSERT INTO commande_produit (commande_id, produit_id, quantite, prix_unitaire, prix_total) "
                + "VALUES (?, ?, ?, ?, ?)";

        try (PreparedStatement psLigne = conn.prepareStatement(sqlLigne)) {
            for (Map.Entry<Produit, Integer> entry : PanierService.getInstance().getPanier().entrySet()) {
                Produit p = entry.getKey();
                int qty = entry.getValue();
                double prixTotal = p.getPrix().multiply(BigDecimal.valueOf(qty)).doubleValue();

                psLigne.setInt(1, commandeId);
                psLigne.setLong(2, p.getId());
                psLigne.setInt(3, qty);
                psLigne.setDouble(4, p.getPrix().doubleValue());
                psLigne.setDouble(5, prixTotal);
                psLigne.addBatch();
            }
            psLigne.executeBatch();
        }
    }
    public Commande getCommandeWithProducts(int id) {
        Commande commande = null;
        String sql = "SELECT c.*, cp.id as cp_id, cp.produit_id, cp.quantite, cp.prix_unitaire, cp.prix_total, " +
                "p.nom as produit_nom, p.prix as produit_prix " +
                "FROM commande c " +
                "LEFT JOIN commande_produit cp ON c.id = cp.commande_id " +
                "LEFT JOIN produit p ON cp.produit_id = p.id " +
                "WHERE c.id = ?";

        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    if (commande == null) {
                        commande = new Commande();
                        commande.setId(rs.getInt("id"));
                        commande.setDateCreation(rs.getTimestamp("date_creation").toLocalDateTime());
                        commande.setTotal(rs.getFloat("total"));
                        commande.setStatus(rs.getString("status"));
                        commande.setAdresseLivraison(rs.getString("adresse_livraison"));
                        commande.setModePaiement(rs.getString("mode_paiement"));
                        commande.setDatePaiement(rs.getTimestamp("date_paiement") != null ?
                                rs.getTimestamp("date_paiement").toLocalDateTime() : null);
                        commande.setStatusPaiement(rs.getString("status_paiement"));

                        User u = new User();
                        u.setId(rs.getInt("user_id"));
                        commande.setUser(u);
                        commande.setCommandeProduits(new ArrayList<>());
                    }

                    // Add products if they exist
                    if (rs.getInt("cp_id") != 0) {
                        CommandeProduit cp = new CommandeProduit();
                        cp.setId(rs.getInt("cp_id"));
                        cp.setQuantite(rs.getInt("quantite"));
                        cp.setPrixUnitaire(rs.getFloat("prix_unitaire"));
                        cp.setPrixTotal(rs.getFloat("prix_total"));

                        Produit p = new Produit();
                        p.setId(rs.getLong("produit_id"));
                        p.setNom(rs.getString("produit_nom"));
                        p.setPrix(rs.getBigDecimal("produit_prix"));
                        cp.setProduit(p);

                        commande.getCommandeProduits().add(cp);
                    }
                }
            }
        } catch (SQLException e) {
            System.out.println("Erreur lors de la récupération de la commande: " + e.getMessage());
        }
        return commande;
    }

    public int createPendingOrder(int userId, String adresseLivraison) throws SQLException {
        Connection conn = null;
        PreparedStatement psCommande = null;
        PreparedStatement psLigne = null;
        ResultSet rs = null;

        try {
            conn = Database.getInstance().getConnection();
            conn.setAutoCommit(false);

            // Insert pending order
            String sqlCommande = "INSERT INTO commande (user_id, total, adresse_livraison, mode_paiement, "
                    + "status, status_paiement, date_creation) "
                    + "VALUES (?, ?, ?, 'Carte_Bancaire', 'En Attente', 'En Attente', NOW())";

            psCommande = conn.prepareStatement(sqlCommande, Statement.RETURN_GENERATED_KEYS);
            psCommande.setInt(1, userId);
            psCommande.setDouble(2, PanierService.getInstance().getTotal());
            psCommande.setString(3, adresseLivraison);

            psCommande.executeUpdate();
            rs = psCommande.getGeneratedKeys();

            if (!rs.next()) {
                throw new SQLException("Failed to get generated commande ID");
            }
            int commandeId = rs.getInt(1);

            // Insert order lines
            insertOrderLines(conn, commandeId);

            conn.commit();
            return commandeId;

        } catch (SQLException e) {
            if (conn != null) conn.rollback();
            throw e;
        } finally {
            closeResources(rs, psLigne, psCommande, conn);
        }
    }
    public void confirmOrderPayment(int commandeId) throws SQLException {
        String sql = "UPDATE commande SET status = 'Confirmée', "
                + "status_paiement = 'Payé', date_paiement = NOW() "
                + "WHERE id = ? AND status_paiement = 'En Attente'";

        try (Connection conn = Database.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, commandeId);
            ps.executeUpdate();
        }
    }
    private void closeResources(ResultSet rs, PreparedStatement psLigne,
                                PreparedStatement psCommande, Connection conn) {
        try {
            if (rs != null) rs.close();
            if (psLigne != null) psLigne.close();
            if (psCommande != null) psCommande.close();
            if (conn != null) {
                conn.setAutoCommit(true);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

}
