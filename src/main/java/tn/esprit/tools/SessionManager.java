package tn.esprit.tools;

import tn.esprit.entities.User;
import java.security.SecureRandom;
import java.math.BigInteger;
import java.util.HashMap;
import java.util.Map;

public class SessionManager {
    private static SessionManager instance;
    private static Map<String, User> activeSessions = new HashMap<>();
    private String currentSessionId;

    private SessionManager() {}

    public static synchronized SessionManager getInstance() {
        if (instance == null) {
            instance = new SessionManager();
        }
        return instance;
    }

    public void createSession(User user) {
        String sessionId = generateSessionId();
        activeSessions.put(sessionId, user);
        this.currentSessionId = sessionId;
    }

    public User getCurrentUser() {
        return activeSessions.get(currentSessionId);
    }

    public void invalidateCurrentSession() {
        if (currentSessionId != null) {
            activeSessions.remove(currentSessionId);
            currentSessionId = null;
        }
    }

    public static String generateSessionId() {
        SecureRandom secureRandom = new SecureRandom();
        byte[] token = new byte[32];
        secureRandom.nextBytes(token);
        return new BigInteger(1, token).toString(16);
    }

    // Add this method to update user info in session
    public void updateCurrentUser(User updatedUser) {
        if (currentSessionId != null) {
            activeSessions.put(currentSessionId, updatedUser);
        }
    }
}