package tn.esprit.tools;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class EnvLoader {
    private static Properties properties = new Properties();

    static {
        try (InputStream input = new FileInputStream(".env")) {
            properties.load(input);
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    public static String getEnv(String key) {
        return properties.getProperty(key);
    }
}
