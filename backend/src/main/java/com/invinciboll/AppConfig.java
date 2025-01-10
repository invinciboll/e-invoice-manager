package com.invinciboll;

import java.io.InputStream;
import java.util.Properties;

public class AppConfig {
    private static AppConfig instance; // Singleton instance
    private Properties properties;

    private AppConfig(String fileName) throws Exception {
        properties = new Properties();
        try (InputStream input = getClass().getClassLoader().getResourceAsStream(fileName)) {
            if (input == null) {
                throw new Exception("Unable to find " + fileName + " in resources.");
            }
            properties.load(input);
        }
    }

    public static AppConfig getInstance(String fileName) throws Exception {
        if (instance == null) {
            synchronized (AppConfig.class) { // Ensure thread safety
                if (instance == null) {
                    instance = new AppConfig(fileName);
                }
            }
        }
        return instance;
    }

    public static AppConfig getInstance() throws Exception {
        if (instance == null) {
            throw new IllegalStateException("AppConfig is not initialized. Call getInstance(String fileName) first.");
        }
        return instance;
    }

    public String getProperty(String key) {
        return properties.getProperty(key);
    }
}
