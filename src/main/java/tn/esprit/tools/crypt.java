package tn.esprit.tools;

import org.mindrot.jbcrypt.BCrypt;

public class crypt {
    public static void main(String[] args) {
        String motDePasseClair = "ADMIN1234"; // 🧠 Ton vrai mot de passe ici
        String hash = BCrypt.hashpw(motDePasseClair, BCrypt.gensalt());
        System.out.println("Mot de passe hashé : " + hash);
    }
}

