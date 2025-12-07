package utils;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/** 
 * Asked AI best way to load a .env file so we can use the environment variables in the code
 */
public class DotEnvLoader {
    private static Map<String, String> envVars = new HashMap<>();

    static {
        loadEnvFile();
    }

    private static void loadEnvFile() {
        try (BufferedReader reader = new BufferedReader(new FileReader(".env"))) {
            String line;
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty() || line.startsWith("#")) {
                    continue;
                }

                int equalsIndex = line.indexOf('=');
                if (equalsIndex > 0) {
                    String key = line.substring(0, equalsIndex).trim();
                    String value = line.substring(equalsIndex + 1).trim();
                    envVars.put(key, value);
                    // Also set as system property for easy access
                    System.setProperty(key, value);
                }
            }
        } catch (IOException e) {
            // .env file not found or readable - this is OK for production
            System.out.println(".env file not found. Using system environment variables.");
        }
    }

    public static String get(String key) {
        // First check system properties (set from .env), then system environment
        String value = System.getProperty(key);
        if (value == null) {
            value = System.getenv(key);
        }
        return value;
    }

    public static String get(String key, String defaultValue) {
        String value = get(key);
        return value != null ? value : defaultValue;
    }
}
