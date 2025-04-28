package tn.esprit.tools;

import jakarta.mail.*;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;
import java.util.Properties;

public class EmailService {
    private final String host;
    private final int port;
    private final String username;
    private final String password;
    private final String from;
    private final boolean auth;
    private final boolean starttls;

    public EmailService() {
        this.host = EmailConfig.getHost();
        this.port = EmailConfig.getPort();
        this.username = EmailConfig.getUsername();
        this.password = EmailConfig.getPassword();
        this.from = EmailConfig.getFrom();
        this.auth = EmailConfig.useAuth();
        this.starttls = EmailConfig.useStartTLS();
    }

    public void sendEmail(String to, String subject, String content) throws MessagingException {
        Properties props = new Properties();
        props.put("mail.smtp.auth", String.valueOf(auth));
        props.put("mail.smtp.starttls.enable", String.valueOf(starttls));
        props.put("mail.smtp.host", host);
        props.put("mail.smtp.port", port);
        props.put("mail.smtp.ssl.protocols", "TLSv1.2");
        props.put("mail.smtp.ssl.trust", "smtp.gmail.com");

        Session session = Session.getInstance(props,
                new Authenticator() {
                    protected PasswordAuthentication getPasswordAuthentication() {
                        return new PasswordAuthentication(username, password);
                    }
                });

        Message message = new MimeMessage(session);
        message.setFrom(new InternetAddress(username));
        message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(to));
        message.setSubject(subject);
        message.setText(content);

        Transport.send(message);
    }

    public void sendVerificationEmail(String to, String verificationCode) throws MessagingException {
        String subject = "Vérification de votre email Falleh Tech";
        String content = "Bonjour,\n\n"
                + "Merci de vous être inscrit sur Falleh Tech. Pour compléter votre inscription, "
                + "veuillez entrer le code de vérification suivant :\n\n"
                + "Code de vérification: " + verificationCode + "\n\n"
                + "Ce code expirera dans 15 minutes.\n\n"
                + "Si vous n'avez pas demandé cette inscription, veuillez ignorer cet email.\n\n"
                + "Cordialement,\nL'équipe Falleh Tech";

        sendEmail(to, subject, content);
    }

    public void sendPasswordResetEmail(String to, String newPassword) throws MessagingException {
        String subject = "Réinitialisation de votre mot de passe Falleh Tech";
        String content = "Bonjour,\n\n"
                + "Vous avez demandé une réinitialisation de votre mot de passe.\n\n"
                + "Votre nouveau mot de passe est: " + newPassword + "\n\n"
                + "Pour des raisons de sécurité, nous vous recommandons de changer ce mot de passe "
                + "après votre prochaine connexion dans les paramètres de votre compte.\n\n"
                + "Cordialement,\nL'équipe Falleh Tech";

        sendEmail(to, subject, content);
    }
}