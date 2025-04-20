package tn.esprit.utils;

public class SessionUtilisateur {
    private static String roleActuel;

    public static String getRole() {
        return roleActuel;
    }

    public static void setRole(String role) {
        roleActuel = role;
    }
}
