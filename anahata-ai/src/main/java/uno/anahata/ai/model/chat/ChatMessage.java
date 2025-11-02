package uno.anahata.ai.model.chat;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import lombok.Builder;
import lombok.Data;

/**
 * Represents a single, standardized message within a conversation as a class.
 * @author anahata
 */
@Data
@Builder
public class ChatMessage {
    
    @Builder.Default
    private String id = UUID.randomUUID().toString();
    
    private String role;
    
    private List<ChatMessagePart> parts;
    
    @Builder.Default
    private Instant createdOn = Instant.now();
    
    private Map<String, Object> metadata;
    
    /**
     * Creates a new chat message with default values for the ID, timestamp, and an empty metadata map.
     * @param role The role of the message author.
     * @param parts The content parts.
     * @return A new ChatMessage instance.
     */
    public static ChatMessage from(String role, List<ChatMessagePart> parts) {
        return ChatMessage.builder()
                .role(role)
                .parts(parts)
                .metadata(Map.of())
                .build();
    }
}
