package tn.esprit.entities;

public enum StatutCandidature {
    EN_ATTENTE,
    ACCEPTEE,
    REFUSEE,
    TERMINEE,
    CONFIRMEE;

    @Override
    public String toString() {
        return switch (this) {
            case EN_ATTENTE -> "en_attente";
            case ACCEPTEE   -> "acceptee";
            case REFUSEE    -> "refusee";
            case TERMINEE   -> "terminee";
            case CONFIRMEE  -> "confirmee";
        };
    }


    public static StatutCandidature fromString(String dbValue) {
        return switch (dbValue.trim().toLowerCase()) {
            case "en_attente", "en attente"        -> EN_ATTENTE;
            case "acceptee", "accepte"             -> ACCEPTEE;
            case "refusee", "rejete", "rejetee"    -> REFUSEE;
            case "terminee"                        -> TERMINEE;
            case "confirmee"                       -> CONFIRMEE;
            default -> {
                System.err.println("⚠️ Statut inconnu: " + dbValue);
                yield EN_ATTENTE;
            }
        };
    }
}
