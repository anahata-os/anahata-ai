package uno.anahata.ai.model.config;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import lombok.extern.slf4j.Slf4j;

/**
 * Central configuration for the Anahata AI Core framework.
 * This class holds global settings, such as the working directory and
 * default API behavior. It is designed to be extended by provider-specific
 * configurations.
 *
 * @author anahata
 */
@Slf4j
public class AiConfig {

    /**
     * Gets the absolute path to the primary working directory where the AI assistant
     * stores its files (~/.anahata/ai).
     * The directory is created if it doesn't exist.
     *
     * @return The absolute path to the working directory.
     */
    public Path getWorkDirectory() {
        Path path = Path.of(System.getProperty("user.home"), ".anahata", "ai");
        if (!Files.exists(path)) {
            log.info("Anahata AI work directory not found, creating it at: {}", path);
            try {
                Files.createDirectories(path);
            } catch (IOException e) {
                throw new RuntimeException("Failed to create Anahata AI work directory at: " + path, e);
            }
        }
        return path;
    }

    /**
     * Gets the maximum number of tokens the user is willing to work with.
     * This is used by the context management system to determine when to prune.
     *
     * @return The token threshold.
     */
    public int getTokenThreshold() {
        return 100_000; // A sensible default
    }

    // --- Default API Behavior ---
    
    public int getApiMaxRetries() {
        return 5;
    }

    public long getApiInitialDelayMillis() {
        return 1000;
    }

    public long getApiMaxDelayMillis() {
        return 30000;
    }
}
