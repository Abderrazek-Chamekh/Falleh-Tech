package tn.esprit.services;


import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp;
import com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.client.util.store.FileDataStoreFactory;

import java.io.FileReader;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.Collections;
import java.util.List;

public class GoogleAuthService {
    private static final String APPLICATION_NAME = "Falleh Tech";
    private static final JsonFactory JSON_FACTORY = GsonFactory.getDefaultInstance();
    private static final String TOKENS_DIRECTORY_PATH = "tokens";

    // Use your Symfony credentials
    private static final String CLIENT_ID = "274064654007-qbruksn43ihhv8d46n8746fqlposoi43.apps.googleusercontent.com";
    private static final String CLIENT_SECRET = "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";
    private static final String REDIRECT_URI = "http://localhost:8000/login/google/callback";

    private static final List<String> SCOPES = Collections.singletonList("profile email");

    public Credential authorize() throws IOException, GeneralSecurityException {
        final NetHttpTransport HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport();

        GoogleClientSecrets clientSecrets = new GoogleClientSecrets()
                .setWeb(new GoogleClientSecrets.Details()
                        .setClientId(CLIENT_ID)
                        .setClientSecret(CLIENT_SECRET)
                        .setRedirectUris(Collections.singletonList(REDIRECT_URI)));

        GoogleAuthorizationCodeFlow flow = new GoogleAuthorizationCodeFlow.Builder(
                HTTP_TRANSPORT, JSON_FACTORY, clientSecrets, SCOPES)
                .setDataStoreFactory(new FileDataStoreFactory(new java.io.File(TOKENS_DIRECTORY_PATH)))
                .setAccessType("offline")
                .build();

        return new AuthorizationCodeInstalledApp(flow, new LocalServerReceiver()).authorize("user");
    }
}