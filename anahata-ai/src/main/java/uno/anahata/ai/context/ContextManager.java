package uno.anahata.ai.context;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import lombok.extern.slf4j.Slf4j;
import uno.anahata.ai.model.chat.ChatMessage;

/**
 * Manages the state of the conversation context.
 * <p>
 * This class is the central authority for the conversation history. It holds the
 * list of all messages, manages dependencies between message parts (for pruning),
 * and tracks UI-agnostic presentation state, such as which messages have been read.
 *
 * @author anahata
 */
@Slf4j
public class ContextManager {

    private final List<ChatMessage> context = new ArrayList<>();
    private final Map<Object, Object> partDependencies = new HashMap<>();
    private final Set<String> readMessageIds = new HashSet<>();

    /**
     * Adds a message to the context and updates the dependency index.
     *
     * @param message The message to add.
     */
    public void addMessage(ChatMessage message) {
        context.add(message);
        log.debug("Added message {} to context.", message.getId());
        // Future implementation:
        // 1. Inspect the parts of the new message.
        // 2. If a part is a FunctionResponse, find its originating FunctionCall.
        // 3. Record this relationship in the partDependencies map.
    }

    /**
     * Prunes parts from the context, using the dependency index to ensure integrity.
     *
     * @param messageId The ID of the message containing the part to prune.
     * @param partIndex The index of the part to prune.
     */
    public void prunePart(String messageId, int partIndex) {
        log.debug("Pruning part {} from message {}.", partIndex, messageId);
        // Future implementation:
        // 1. Find the message and the part.
        // 2. Look up the part in the partDependencies map.
        // 3. If a dependency is found, locate and remove the dependent part as well.
        // 4. Remove the original part.
    }
    
    /**
     * Marks a message as read.
     * @param messageId The ID of the message to mark as read.
     */
    public void markAsRead(String messageId) {
        readMessageIds.add(messageId);
    }

    /**
     * Checks if a message has been marked as read.
     * @param messageId The ID of the message to check.
     * @return {@code true} if the message has been read, {@code false} otherwise.
     */
    public boolean isRead(String messageId) {
        return readMessageIds.contains(messageId);
    }
    
    /**
     * Gets the current conversation context.
     * @return An unmodifiable list of the messages in the context.
     */
    public List<ChatMessage> getContext() {
        return Collections.unmodifiableList(context);
    }
}
