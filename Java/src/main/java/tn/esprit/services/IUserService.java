package tn.esprit.services;

import tn.esprit.entities.User;
import java.sql.SQLException;
import java.util.List;

public interface IUserService {
    void add(User user) throws SQLException;
    void update(User user) throws SQLException;
    void delete(int id) throws SQLException;
    User getById(int id) throws SQLException;
    List<User> getAll() throws SQLException;
    User authenticate(String email, String password) throws SQLException;
    boolean emailExists(String email) throws SQLException;
    boolean carteIdentiteExists(String carteIdentite) throws SQLException;
    List<User> getByRole(String role) throws SQLException;
    void activateUser(int id) throws SQLException;
    void deactivateUser(int id) throws SQLException;
}