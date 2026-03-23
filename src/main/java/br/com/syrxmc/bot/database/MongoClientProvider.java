package br.com.syrxmc.bot.database;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoDatabase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MongoClientProvider {

    private static final Logger logger = LoggerFactory.getLogger(MongoClientProvider.class);

    private static MongoClient mongoClient;
    private static String databaseName;

    private MongoClientProvider() {}

    public static void init(String mongoUri) {
        if (mongoClient != null) {
            logger.warn("MongoClientProvider already initialized.");
            return;
        }
        databaseName = extractDbName(mongoUri);
        mongoClient = MongoClients.create(mongoUri);
        logger.info("MongoDB connected. Database: {}", databaseName);
    }

    public static MongoDatabase getDatabase() {
        ensureInitialized();
        return mongoClient.getDatabase(databaseName);
    }

    public static MongoDatabase getDatabase(String dbName) {
        ensureInitialized();
        return mongoClient.getDatabase(dbName);
    }

    public static void close() {
        if (mongoClient != null) {
            mongoClient.close();
            mongoClient = null;
            logger.info("MongoDB connection closed.");
        }
    }

    private static void ensureInitialized() {
        if (mongoClient == null) {
            throw new IllegalStateException("MongoClientProvider is not initialized. Call MongoClientProvider.init(uri) first.");
        }
    }

    private static String extractDbName(String uri) {
        try {
            // mongodb://host:port/dbname or mongodb+srv://user:pass@host/dbname
            String withoutQuery = uri.contains("?") ? uri.substring(0, uri.indexOf('?')) : uri;
            int lastSlash = withoutQuery.lastIndexOf('/');
            if (lastSlash >= 0) {
                // Check there are at least two slashes (so it's not just the scheme)
                String afterScheme = withoutQuery.substring(withoutQuery.indexOf("://") + 3);
                int slashInAfterScheme = afterScheme.indexOf('/');
                if (slashInAfterScheme >= 0) {
                    String candidate = afterScheme.substring(slashInAfterScheme + 1);
                    if (!candidate.isBlank()) {
                        return candidate;
                    }
                }
            }
        } catch (Exception e) {
            logger.warn("Could not extract database name from URI, defaulting to 'syrx'.");
        }
        return "syrx";
    }
}
