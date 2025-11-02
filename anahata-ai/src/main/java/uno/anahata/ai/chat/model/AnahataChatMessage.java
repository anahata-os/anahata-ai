package uno.anahata.ai.chat.model;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Represents a single, standardized message within a conversation as an immutable record.
 * This is a native Java 21 feature, providing conciseness and immutability without external dependencies.
 * @param id The unique identifier for the message.
 * @param role The role of the message author (e.g., "user", "model").
 * @param parts The content parts of the message.
 * @param createdOn The timestamp of creation.
 * @param metadata A map for storing provider-specific metadata (e.g., Gemini's 'thoughtSignatures').
 */
public record AnahataChatMessage(
    String id, 
    String role, 
    List<Object> parts, 
    Instant createdOn, 
    Map<String, Object> metadata
) {
    
    /**
     * Creates a new chat message with default values for the ID, timestamp, and an empty metadata map.
     * @param role The role of the message author.
     * @param parts The content parts.
     * @return A new AnahataChatMessage instance.
     */
    public static AnahataChatMessage from(String role, List<Object> parts) {
        return new AnahataChatMessage(UUID.randomUUID().toString(), role, parts, Instant.now(), Map.of());
    }
}
