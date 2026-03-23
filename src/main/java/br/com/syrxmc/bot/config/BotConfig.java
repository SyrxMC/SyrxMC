package br.com.syrxmc.bot.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class BotConfig {

    private static final Logger logger = LoggerFactory.getLogger(BotConfig.class);

    private static String botToken;
    private static String mongoUri;

    private BotConfig() {}

    public static void load() {
        Map<String, String> dotenv = loadDotEnvFile();

        botToken = resolveVar("BOT_TOKEN", dotenv);
        mongoUri = resolveVar("MONGO_URI", dotenv);

        if (botToken == null || botToken.isBlank()) {
            throw new IllegalStateException("BOT_TOKEN is required but not set. Provide it via environment variable or .env file.");
        }
        if (mongoUri == null || mongoUri.isBlank()) {
            throw new IllegalStateException("MONGO_URI is required but not set. Provide it via environment variable or .env file.");
        }

        logger.info("BotConfig loaded successfully.");
    }

    public static String getBotToken() {
        ensureLoaded();
        return botToken;
    }

    public static String getMongoUri() {
        ensureLoaded();
        return mongoUri;
    }

    private static void ensureLoaded() {
        if (botToken == null) {
            throw new IllegalStateException("BotConfig has not been loaded. Call BotConfig.load() first.");
        }
    }

    private static String resolveVar(String key, Map<String, String> dotenv) {
        String value = System.getenv(key);
        if (value != null && !value.isBlank()) {
            return value;
        }
        return dotenv.get(key);
    }

    private static Map<String, String> loadDotEnvFile() {
        Map<String, String> env = new HashMap<>();
        File file = new File(".env");
        if (!file.exists()) {
            logger.debug(".env file not found, relying on system environment variables.");
            return env;
        }
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty() || line.startsWith("#")) {
                    continue;
                }
                int idx = line.indexOf('=');
                if (idx > 0) {
                    String key = line.substring(0, idx).trim();
                    String value = line.substring(idx + 1).trim();
                    env.put(key, value);
                }
            }
        } catch (IOException e) {
            logger.warn("Failed to read .env file: {}", e.getMessage());
        }
        return env;
    }
}
