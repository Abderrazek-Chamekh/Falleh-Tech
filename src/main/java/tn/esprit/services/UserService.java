package tn.esprit.services;

import tn.esprit.entities.User;
import tn.esprit.tools.Database;
import tn.esprit.tools.PasswordHasher;

import java.sql.*;
import java.time.Month;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class UserService implements IUserService {

    private final Connection connection;

    public UserService() {
        connection = Database.getInstance().getConnection();
    }

    @Override
    public void add(User user) throws SQLException {
        String query = "INSERT INTO user (name, last_name, email, password, phone_number, role, " +
                "carte_identite, disponibility, location, experience, active) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (PreparedStatement statement = connection.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {
            statement.setString(1, user.getName());
            statement.setString(2, user.getLastName());
            statement.setString(3, user.getEmail());
            statement.setString(4, user.getPassword());
            statement.setString(5, user.getPhoneNumber());
            statement.setString(6, user.getRole());
            statement.setString(7, user.getCarteIdentite());
            statement.setObject(8, user.getDisponibility());
            statement.setString(9, user.getLocation());
            statement.setString(10, user.getExperience());
            statement.setBoolean(11, user.isActive());

            statement.executeUpdate();

            try (ResultSet generatedKeys = statement.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    user.setId(generatedKeys.getInt(1));
                }
            }
        }
    }

    @Override
    public void update(User user) throws SQLException {
        String query = "UPDATE user SET name = ?, last_name = ?, email = ?, password = ?, " +
                "phone_number = ?, role = ?, carte_identite = ?, disponibility = ?, " +
                "location = ?, experience = ?, active = ? WHERE id = ?";

        try (PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setString(1, user.getName());
            statement.setString(2, user.getLastName());
            statement.setString(3, user.getEmail());
            statement.setString(4, user.getPassword());
            statement.setString(5, user.getPhoneNumber());
            statement.setString(6, user.getRole());
            statement.setString(7, user.getCarteIdentite());
            statement.setObject(8, user.getDisponibility());
            statement.setString(9, user.getLocation());
            statement.setString(10, user.getExperience());
            statement.setBoolean(11, user.isActive());
            statement.setInt(12, user.getId());

            statement.executeUpdate();
        }
    }

    @Override
    public void delete(int id) throws SQLException {
        String query = "DELETE FROM user WHERE id = ?";

        try (PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setInt(1, id);
            statement.executeUpdate();
        }
    }

    @Override
    public User getById(int id) throws SQLException {
        String query = "SELECT * FROM user WHERE id = ?";

        try (PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setInt(1, id);

            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return createUserFromResultSet(resultSet);
                }
            }
        }
        return null;
    }

    public User getByEmail(String email) throws SQLException {
        String query = "SELECT * FROM user WHERE email = ?";

        try (PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setString(1, email);

            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return createUserFromResultSet(resultSet);
                }
            }
        }
        return null;
    }

    @Override
    public List<User> getAll() throws SQLException {
        List<User> users = new ArrayList<>();
        String query = "SELECT * FROM user";

        try (Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery(query)) {

            while (resultSet.next()) {
                users.add(createUserFromResultSet(resultSet));
            }
        }
        return users;
    }

    @Override
    public boolean emailExists(String email) throws SQLException {
        String query = "SELECT COUNT(*) FROM user WHERE email = ?";

        try (PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setString(1, email);

            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return resultSet.getInt(1) > 0;
                }
            }
        }
        return false;
    }

    @Override
    public boolean carteIdentiteExists(String carteIdentite) throws SQLException {
        String query = "SELECT COUNT(*) FROM user WHERE carte_identite = ?";

        try (PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setString(1, carteIdentite);

            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return resultSet.getInt(1) > 0;
                }
            }
        }
        return false;
    }

    @Override
    public List<User> getByRole(String role) throws SQLException {
        List<User> users = new ArrayList<>();
        String query = "SELECT * FROM user WHERE role = ?";

        try (PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setString(1, role);

            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    users.add(createUserFromResultSet(resultSet));
                }
            }
        }
        return users;
    }

    @Override
    public void activateUser(int id) throws SQLException {
        String query = "UPDATE user SET active = true WHERE id = ?";

        try (PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setInt(1, id);
            statement.executeUpdate();
        }
    }

    @Override
    public void deactivateUser(int id) throws SQLException {
        String query = "UPDATE user SET active = false WHERE id = ?";

        try (PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setInt(1, id);
            statement.executeUpdate();
        }
    }
    public void updateUserStatus(int userId, boolean active) throws SQLException {
        String query = "UPDATE user SET active = ? WHERE id = ?";
        try (PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setBoolean(1, active);
            statement.setInt(2, userId);
            statement.executeUpdate();
        }
    }

    private User createUserFromResultSet(ResultSet resultSet) throws SQLException {
        User user = new User();
        user.setId(resultSet.getInt("id"));
        user.setName(resultSet.getString("name"));
        user.setLastName(resultSet.getString("last_name"));
        user.setEmail(resultSet.getString("email"));
        user.setPassword(resultSet.getString("password"));
        user.setPhoneNumber(resultSet.getString("phone_number"));
        user.setRole(resultSet.getString("role"));
        user.setCarteIdentite(resultSet.getString("carte_identite"));

        Timestamp disponibility = resultSet.getTimestamp("disponibility");
        user.setDisponibility(disponibility != null ? disponibility.toLocalDateTime() : null);

        user.setLocation(resultSet.getString("location"));
        user.setExperience(resultSet.getString("experience"));
        user.setActive(resultSet.getBoolean("active"));

        return user;
    }

    @Override
    public User authenticate(String email, String password) throws SQLException {

        // Check if the user exists in the database based on the provided email
        User user = getByEmail(email);

        if (user == null) {
            return null ;
        }
        PasswordHasher ps= new PasswordHasher();
        // Verify the password
        String hashedPasswordFromDatabase = user.getPassword();
        System.out.println("eeeeee"+hashedPasswordFromDatabase);// Retrieve hashed password from the database

        hashedPasswordFromDatabase = hashedPasswordFromDatabase.substring(0, 2) + 'y' + hashedPasswordFromDatabase.substring(3);
        System.out.println("ffffff"+hashedPasswordFromDatabase);
        // Hash the provided password using the same algorithm and parameters used to hash the password in the database
        // Compare the hashed passwords

        if (ps.verifyPassword(password,hashedPasswordFromDatabase)) {
            return user;
        } else {
            return null;
        }
    }

    // Add these to your UserService class

    public int getTotalUserCount() throws SQLException {
        String query = "SELECT COUNT(*) as total FROM user";
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {
            return rs.next() ? rs.getInt("total") : 0;
        }
    }

    public int getActiveUserCount() throws SQLException {
        String query = "SELECT COUNT(*) as active FROM user WHERE active = true";
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {
            return rs.next() ? rs.getInt("active") : 0;
        }
    }

    public int getRoleCount() throws SQLException {
        String query = "SELECT COUNT(DISTINCT role) as roles FROM user";
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {
            return rs.next() ? rs.getInt("roles") : 0;
        }
    }

    public Map<String, Integer> getUserCountByRole() throws SQLException {
        Map<String, Integer> roleCounts = new HashMap<>();
        String query = "SELECT role, COUNT(*) as count FROM user GROUP BY role";

        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {

            while (rs.next()) {
                roleCounts.put(rs.getString("role"), rs.getInt("count"));
            }
        }

        return roleCounts;
    }

    public Map<Boolean, Integer> getUserCountByStatus() throws SQLException {
        Map<Boolean, Integer> statusCounts = new HashMap<>();
        String query = "SELECT active, COUNT(*) as count FROM user GROUP BY active";

        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {

            while (rs.next()) {
                statusCounts.put(rs.getBoolean("active"), rs.getInt("count"));
            }
        }

        return statusCounts;
    }
    public Map<String, Double> getActivityByRole() throws SQLException {
        Map<String, Double> roleActivity = new HashMap<>();
        String query = """
        SELECT role, 
               COUNT(*) as total_users,
               SUM(CASE WHEN last_login >= DATE_SUB(NOW(), INTERVAL 30 DAY) THEN 1 ELSE 0 END) as active_users
        FROM user
        GROUP BY role
        """;

        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {

            while (rs.next()) {
                double activityLevel = rs.getInt("active_users") * 100.0 / rs.getInt("total_users");
                roleActivity.put(rs.getString("role"), activityLevel);
            }
        }

        return roleActivity;
    }
    public List<User> getAllOuvriers() {
        List<User> list = new ArrayList<>();
        String sql = "SELECT * FROM user WHERE role = 'ouvrier'";

        try (Statement st = connection.createStatement(); ResultSet rs = st.executeQuery(sql)) {
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
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
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
}