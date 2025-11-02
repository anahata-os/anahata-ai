package uno.anahata.ai;

import java.nio.file.Path;
import java.util.Optional;

/**
 * Central configuration for the Anahata AI Core framework.
 * This class holds global settings, such as the working directory and
 * configuration for the active workspace.
 */
public interface AnahataAIConfig {

    /**
     * Gets the absolute path to the working directory where the AI assistant
     * can store temporary files, notes, and session history.
     *
     * @return The absolute path to the working directory.
     */
    Path getWorkDirectory();

    /**
     * Gets the maximum number of tokens the user is willing to work with.
     * This is used by the context management system to determine when to prune.
     *
     * @return The token threshold.
     */
    int getTokenThreshold();

    /**
     * Gets the API key for the primary AI model provider.
     *
     * @return An Optional containing the API key, or empty if not set.
     */
    Optional<String> getApiKey();
}
