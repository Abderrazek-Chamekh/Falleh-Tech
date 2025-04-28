package tn.esprit.Controllers.User.Authentication;

import com.google.api.client.auth.oauth2.AuthorizationCodeRequestUrl;
import com.google.api.client.auth.oauth2.TokenResponse;
import com.google.api.client.googleapis.auth.oauth2.*;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import tn.esprit.services.UserService;
import tn.esprit.tools.EmailService;
import java.util.Collections;
public class GoogleLoginController {

    @FXML private WebView webView;

    private static final String CLIENT_ID = "274064654007-qbruksn43ihhv8d46n8746fqlposoi43.apps.googleusercontent.com";
    private static final String CLIENT_SECRET = "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";
    private static final String REDIRECT_URI = "http://localhost:8000/login/google/callback";

    private static final GsonFactory JSON_FACTORY = GsonFactory.getDefaultInstance();
    private static final NetHttpTransport HTTP_TRANSPORT = new NetHttpTransport();

    private static final String SCOPE = "profile email openid";
    private UserService userService = new UserService();

    @FXML
    public void initialize() {
        try {
            GoogleAuthorizationCodeFlow flow = new GoogleAuthorizationCodeFlow.Builder(
                    HTTP_TRANSPORT, JSON_FACTORY,
                    CLIENT_ID, CLIENT_SECRET,
                    Collections.singletonList(SCOPE))
                    .setAccessType("offline")
                    .build();

            AuthorizationCodeRequestUrl authorizationUrl = flow.newAuthorizationUrl().setRedirectUri(REDIRECT_URI);

            WebEngine engine = webView.getEngine();
            engine.load(authorizationUrl.build());

            engine.locationProperty().addListener((obs, oldLoc, newLoc) -> {
                if (newLoc != null && newLoc.startsWith(REDIRECT_URI)) {
                    String code = extractCodeFromUrl(newLoc);
                    if (code != null) {
                        new Thread(() -> {
                            try {
                                TokenResponse response = flow.newTokenRequest(code)
                                        .setRedirectUri(REDIRECT_URI)
                                        .execute();

                                GoogleCredential credential = new GoogleCredential.Builder()
                                        .setTransport(HTTP_TRANSPORT)
                                        .setJsonFactory(JSON_FACTORY)
                                        .setClientSecrets(CLIENT_ID, CLIENT_SECRET)
                                        .build()
                                        .setFromTokenResponse(response);

                                GoogleTokenResponse tokenResponse = flow.newTokenRequest(code)
                                        .setRedirectUri(REDIRECT_URI)
                                        .execute();

                                GoogleIdToken idToken = tokenResponse.parseIdToken();
                                GoogleIdToken.Payload payload = idToken.getPayload();

                                String email = payload.getEmail();
                                String name = (String) payload.get("name"); // optional



                                /*/ Fetch user info using the token
                                GoogleIdToken idToken = GoogleIdToken.parse(JSON_FACTORY, credential.getAccessToken());
                                GoogleIdToken.Payload payload = idToken.getPayload();

                                String email = (String) payload.getEmail();
                                String name = (String) payload.get("name");*/

                                // Send welcome email
                                EmailService emailService = new EmailService();
                                String subject = "Bienvenue chez Falleh Tech !";
                                String content = "Bonjour " + name + ",\n\n"
                                        + "Vous vous êtes connecté avec succès via Google.\n"
                                        + "Email : " + email + "\n\n"
                                        + "Bienvenue dans la communauté Falleh Tech !\n\n"
                                        + "Cordialement,\nL'équipe Falleh Tech";

                                emailService.sendEmail(email, subject, content);

                                Platform.runLater(() -> {
                                    System.out.println("Google login successful for: " + email);
                                    showSuccessScreen();
                                });

                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }).start();
                    }
                }
            });

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private String extractCodeFromUrl(String url) {
        if (url.contains("code=")) {
            String codePart = url.substring(url.indexOf("code=") + 5);
            int ampIndex = codePart.indexOf("&");
            return ampIndex != -1 ? codePart.substring(0, ampIndex) : codePart;
        }
        return null;
    }

    private void showSuccessScreen() {
        System.out.println("Navigation vers le dashboard...");
        // Add your scene switch logic here
    }
}
