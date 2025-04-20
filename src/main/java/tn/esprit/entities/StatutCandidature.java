package tn.esprit.entities;
public enum StatutCandidature {
    EN_ATTENTE,
    ACCEPTE,
    REJETE,
    TERMINEE; // ✅ NEW

    public static StatutCandidature fromString(String dbValue) {
        return switch (dbValue.trim().toLowerCase()) {
            case "accepte", "acceptee" -> ACCEPTE;
            case "rejete", "rejetee"   -> REJETE;
            case "en_attente"          -> EN_ATTENTE;
            case "terminee"            -> TERMINEE; // ✅ Handle it
            default -> throw new IllegalArgumentException("Unknown statut: " + dbValue);
        };
    }
}
