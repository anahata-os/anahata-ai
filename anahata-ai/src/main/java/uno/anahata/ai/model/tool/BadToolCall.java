package uno.anahata.ai.model.tool;

import java.util.Map;
import lombok.NonNull;

/**
 * Represents a call to a {@link BadTool}, capturing an invalid tool request
 * from the model.
 *
 * @author pablo
 */
public class BadToolCall extends AbstractToolCall<BadTool, BadToolResponse> {

    public BadToolCall(@NonNull String id, @NonNull BadTool tool, @NonNull Map<String, Object> args) {
        super(id, tool, args);
    }

    @Override
    protected BadToolResponse createResponse() {
        return new BadToolResponse(this);
    }
}
