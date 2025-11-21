package uno.anahata.ai.model.core;

import java.util.List;
import java.util.UUID;
import lombok.Getter;
import lombok.Setter;

/**
 * The abstract base class for all messages in a conversation, providing common
 * metadata and functionality.
 * <p>
 * This rich, hierarchical model supports type-safe roles through its subclasses
 * (e.g., {@code UserMessage}, {@code ModelMessage}) and ensures each message
 * has a unique identity and timestamp.
 */
@Getter
@Setter
public abstract class AbstractMessage {
    /**
     * A unique, immutable identifier for this message.
     */
    private final String id = UUID.randomUUID().toString();

    /**
     * The timestamp when this message was created, in milliseconds since the epoch.
     */
    private final long timestamp = System.currentTimeMillis();
    
    /**
     * A monotonically increasing number assigned to the message when it is added to a chat,
     * representing its order in the conversation.
     */
    private long sequenceNumber;
    
    /**
     * The number of tokens in this specific message, as reported by the provider.
     * This is crucial for granular cost analysis and context management.
     */
    private int tokenCount;

    /**
     * The list of parts that make up the message content.
     */
    private List<AbstractPart> parts;

    /**
     * A flag indicating whether this message has been pruned from the context.
     */
    private boolean pruned = false;

    /**
     * Gets the role of the entity that created this message.
     * This is implemented by subclasses to provide compile-time type safety.
     *
     * @return The role of the message creator.
     */
    public abstract Role getRole();
}
