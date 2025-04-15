package tn.esprit.services;

import tn.esprit.entities.User;
import tn.esprit.tools.my_db;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ServiceUser implements services<User> {

    private final Connection con;
    public ServiceUser() {
        this.con = my_db.getInstance().getConnection();
    }

    @Override
    public List<User> getAll() {
        List<User> users = new ArrayList<>();
        String query = "SELECT * FROM user";

        try (PreparedStatement stmt = con.prepareStatement(query);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                User user = new User();
                user.setId(rs.getInt("id"));
                user.setName(rs.getString("name"));
                user.setLastName(rs.getString("last_name"));
                user.setEmail(rs.getString("email"));
                user.setPassword(rs.getString("password"));
                user.setRole(rs.getString("role"));
                user.setCarteIdentite(rs.getString("carte_identite"));
                user.setPhoneNumber(rs.getString("phone_number"));
                user.setDisponibility(rs.getTimestamp("disponibility") != null
                        ? rs.getTimestamp("disponibility").toLocalDateTime() : null);
                user.setLocation(rs.getString("location"));
                user.setExperience(rs.getString("experience"));
                user.setActive(rs.getBoolean("active"));

                users.add(user);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return users;
    }
    public List<User> getAllOuvriers() {
        List<User> list = new ArrayList<>();
        String sql = "SELECT * FROM user WHERE role = 'ouvrier'";

        try (Statement st = con.createStatement(); ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                User u = new User();
                u.setId(rs.getInt("id"));
                u.setName(rs.getString("name"));
                u.setLastName(rs.getString("last_name"));
                u.setEmail(rs.getString("email"));
                list.add(u);
            }
        } catch (SQLException e) {
            System.err.println("❌ Erreur getAllOuvriers(): " + e.getMessage());
        }

        return list;
    }
    public User findById(int id) {
        String sql = "SELECT * FROM user WHERE id = ?";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                User u = new User();
                u.setId(rs.getInt("id"));
                u.setName(rs.getString("name"));
                u.setLastName(rs.getString("last_name"));
                u.setRole(rs.getString("role"));
                u.setEmail(rs.getString("email"));
                return u;
            }
        } catch (SQLException e) {
            System.out.println("❌ Erreur lors de la récupération de l'utilisateur: " + e.getMessage());
        }
        return null;
    }

    // Other methods from the interface not implemented for now
    @Override public void ajouter(User user) {}
    @Override public void modifier(User user) {}
    @Override public void supprimer(User user) {}
}
