package uno.anahata.ai.model.core;

import java.util.List;
import java.util.stream.Collectors;
import lombok.Getter;
import lombok.Setter;
import uno.anahata.ai.model.tool.AbstractToolCall;

/**
 * Represents a message originating from the AI model.
 *
 * @author anahata-gemini-pro-2.5
 */
@Getter
@Setter
public class ModelMessage extends AbstractMessage {
    
    /** The ID of the model that generated this message. */
    private String modelId;
    
    /** The number of tokens used by this message. */
    private int tokenCount;
    
    public ModelMessage() {
        //no arg constructor
    }
    
    @Override
    public Role getRole() {
        return Role.MODEL;
    }

    /**
     * Filters and returns only the tool call parts from this message.
     * @return A list of {@link AbstractToolCall} parts, or an empty list if none exist.
     */
    public List<AbstractToolCall> getToolCalls() {
        return getParts().stream()
                .filter(AbstractToolCall.class::isInstance)
                .map(AbstractToolCall.class::cast)
                .collect(Collectors.toList());
    }
}