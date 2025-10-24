package utils;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class EnvLoader {

    public static void load() {
        try {
            Map<String, String> envVars = readEnvFile(".env");
            envVars.forEach((key, value) -> {
                System.setProperty(key, value);
            });
            System.out.println("✓ .env file loaded successfully");
        } catch (IOException e) {
            System.out.println("⚠ Warning: Could not load .env file: " + e.getMessage());
        }
    }

    private static Map<String, String> readEnvFile(String filePath) throws IOException {
        Map<String, String> envVars = new HashMap<>();

        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.trim().isEmpty() || line.trim().startsWith("#")) {
                    continue;
                }

                if (line.contains("=")) {
                    String[] parts = line.split("=", 2);
                    String key = parts[0].trim();
                    String value = parts[1].trim();

                    if (value.startsWith("\"") && value.endsWith("\"")) {
                        value = value.substring(1, value.length() - 1);
                    }

                    envVars.put(key, value);
                }
            }
        }

        return envVars;
    }
}