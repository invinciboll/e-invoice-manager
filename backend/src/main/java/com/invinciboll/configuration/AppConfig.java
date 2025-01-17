package com.invinciboll.configuration;

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

    // Initialize AppConfig with a specific file name
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

    // Retrieve AppConfig instance when already initialized
    public static AppConfig getInstance() {
        if (instance == null) {
            throw new IllegalStateException("AppConfig is not initialized. Call getInstance(String fileName) first.");
        }
        return instance;
    }

    public String getProperty(String key) {
        return properties.getProperty(key);
    }

    // Retrieve a property by key with a default value
    public String getProperty(String key, String defaultValue) {
        try {
            return properties.getProperty(key, defaultValue); // Uses the Properties API default
        } catch (Exception e) {
            return defaultValue; // Fallback to the provided default value
        }
    }
}
