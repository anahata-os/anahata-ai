package uno.anahata.ai.model.core;

import java.util.List;
import java.util.stream.Collectors;
import uno.anahata.ai.model.tool.AbstractToolCall;

/**
 * Represents a message originating from the AI model.
 *
 * @author Anahata
 */
public class ModelMessage extends Message {
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
