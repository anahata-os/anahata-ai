package uno.anahata.ai.context;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import uno.anahata.ai.chat.model.AnahataChatMessage;

/**
 * Manages the state of the conversation context.
 * <p>
 * This class is the central authority for the conversation history. It holds the
 * list of all messages and, critically, maintains the index of dependencies
 * between message parts (e.g., linking a FunctionCall to its FunctionResponse).
 * This logic was previously part of the ChatMessage class itself but has been
 * moved here to create a cleaner separation of concerns.
 */
public class ContextManager {

    private final List<AnahataChatMessage> context = new ArrayList<>();

    /**
     * This map will store the relationships between message parts.
     * For example, it could map the ID of a FunctionResponse part to the ID
     * of its corresponding FunctionCall part. This index is essential for
     * the "paired pruning" logic.
     * The exact structure (e.g., Map<String, String>) will be refined during
     * implementation.
     */
    private final Map<Object, Object> partDependencies = new HashMap<>();

    /**
     * Adds a message to the context and updates the dependency index.
     *
     * @param message The message to add.
     */
    public void addMessage(AnahataChatMessage message) {
        context.add(message);
        // Future implementation:
        // 1. Inspect the parts of the new message.
        // 2. If a part is a FunctionResponse, find its originating FunctionCall in a previous message.
        // 3. Record this relationship in the partDependencies map.
    }

    /**
     * Prunes parts from the context, using the dependency index to ensure integrity.
     *
     * @param messageId The ID of the message containing the part to prune.
     * @param partIndex The index of the part to prune.
     */
    public void prunePart(String messageId, int partIndex) {
        // Future implementation:
        // 1. Find the message and the part.
        // 2. Look up the part in the partDependencies map.
        // 3. If a dependency is found, locate and remove the dependent part as well.
        // 4. Remove the original part.
    }
    
    public List<AnahataChatMessage> getContext() {
        return new ArrayList<>(context); // Return a copy for immutability
    }
}
