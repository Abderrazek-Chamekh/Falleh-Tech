package tn.esprit.services;

import jakarta.mail.*;
import jakarta.mail.internet.*;
import jakarta.activation.DataHandler;
import jakarta.activation.DataSource;
import jakarta.mail.util.ByteArrayDataSource;

import javax.net.ssl.*;
import java.security.SecureRandom;
import java.security.cert.X509Certificate;
import java.io.*;
import java.util.Arrays;
import java.util.Properties;

public class EmailService {
    private final String smtpHost = "smtp.gmail.com";
    private final int smtpPort = 587;
    private final String username = "shaima.ajailia02@gmail.com";
    private final String password = "queoyntbkztlbjws";

    public void sendFactureEmail(String recipientEmail, String subject, String body,
                                 byte[] pdfAttachment, String attachmentName) throws MessagingException {
        // Validate inputs
        if (recipientEmail == null || recipientEmail.trim().isEmpty()) {
            throw new IllegalArgumentException("Recipient email is required");
        }

        Properties props = new Properties();
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.host", smtpHost);
        props.put("mail.smtp.port", smtpPort);
        props.put("mail.smtp.ssl.trust", "*");

        Session session = Session.getInstance(props, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(username, password);
            }
        });

        try {
            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress(username));

            // Fix 1: Explicitly set recipient instead of using parse()
            message.addRecipient(Message.RecipientType.TO, new InternetAddress(recipientEmail));

            message.setSubject(subject);

            // Create multipart message
            MimeBodyPart textPart = new MimeBodyPart();
            textPart.setText(body, "UTF-8", "plain");

            MimeBodyPart pdfPart = new MimeBodyPart();
            ByteArrayDataSource source = new ByteArrayDataSource(pdfAttachment, "application/pdf");
            pdfPart.setDataHandler(new DataHandler(source));
            pdfPart.setFileName(MimeUtility.encodeText(attachmentName));

            Multipart multipart = new MimeMultipart();
            multipart.addBodyPart(textPart);
            multipart.addBodyPart(pdfPart);

            message.setContent(multipart);

            // Fix 2: Add debug output
            System.out.println("Final message recipients: " + Arrays.toString(message.getAllRecipients()));

            Transport.send(message);
        } catch (Exception e) {
            System.err.println("Failed to send email to " + recipientEmail);
            throw new MessagingException("Failed to send email", e);
        }
    }

    private SSLSocketFactory getDummySSLSocketFactory() {
        try {
            SSLContext sslContext = SSLContext.getInstance("TLS");
            sslContext.init(null, new TrustManager[]{
                    new X509TrustManager() {
                        public void checkClientTrusted(X509Certificate[] certs, String authType) {}
                        public void checkServerTrusted(X509Certificate[] certs, String authType) {}
                        public X509Certificate[] getAcceptedIssuers() { return null; }
                    }
            }, new SecureRandom());
            return sslContext.getSocketFactory();
        } catch (Exception e) {
            throw new RuntimeException("Failed to create dummy SSL factory", e);
        }
    }
    public void sendTestEmail(String recipientEmail) throws MessagingException {
        Properties props = new Properties();
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.host", smtpHost);
        props.put("mail.smtp.port", smtpPort);

        Session session = Session.getInstance(props, new Authenticator() {
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(username, password);
            }
        });
        session.setDebug(true);  // Enable SMTP debugging

        try {
            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress(username));
            message.addRecipient(Message.RecipientType.TO, new InternetAddress(recipientEmail));
            message.setSubject("Test Email");
            message.setText("This is a test email");

            Transport.send(message);
            System.out.println("Test email sent successfully");
        } catch (Exception e) {
            System.err.println("Failed to send test email");
            throw e;
        }
    }
}