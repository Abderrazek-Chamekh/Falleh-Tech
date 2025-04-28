package tn.esprit.tools;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class EmailConfig {
    /*private static final Properties properties = new Properties();

    static {
        try (InputStream input = EmailConfig.class.getClassLoader().getResourceAsStream("email.properties")) {
            if (input == null) {
                throw new RuntimeException("Unable to find email.properties");
            }
            properties.load(input);
        } catch (IOException e) {
            throw new RuntimeException("Failed to load email configuration", e);
        }
    }*/

    public static String getHost() {
        //return properties.getProperty("mail.smtp.host");
        return "smtp.gmail.com";
    }

    public static int getPort() {
        //return Integer.parseInt(properties.getProperty("mail.smtp.port"));
        return 587;
    }

    public static String getUsername() {
        //return properties.getProperty("mail.username");
        return "shaima.ajailia02@gmail.com";
    }

    public static String getPassword() {
        //String envPass = System.getenv("SMTP_PASSWORD");
        //return envPass != null ? envPass : properties.getProperty("mail.password");
        return "queoyntbkztlbjws";
    }

    public static String getFrom() {
        //return properties.getProperty("mail.from");
        return "shaima.ajailia02@gmail.com";
    }

    public static boolean useAuth() {
        //return Boolean.parseBoolean(properties.getProperty("mail.auth"));
        return true;
    }

    public static boolean useStartTLS() {
        //return Boolean.parseBoolean(properties.getProperty("mail.starttls.enable"));
        return true;
    }
}