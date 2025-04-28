package tn.esprit.utils;

public class SessionUtilisateur {
    private static String roleActuel;
    private static int userId;
    private static String username;

    public static String getRole() {
        return roleActuel;
    }

    public static void setRole(String role) {
        roleActuel = role;
    }
    public static int getUserId() {
        return userId;
    }

    public static void setUserId(int id) {
        userId = id;
    }

    public static String getUsername() {
        return username;
    }

    public static void setUsername(String username) {
        SessionUtilisateur.username = username;
    }
}
