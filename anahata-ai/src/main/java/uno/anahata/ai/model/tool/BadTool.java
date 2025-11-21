package uno.anahata.ai.model.tool;

import java.util.Collections;
import java.util.Map;

/**
 * A special tool implementation representing a tool that was requested by the
 * model but was not found in the registered toolkits.
 *
 * @author pablo
 */
public class BadTool extends AbstractTool<BadToolCall, BadToolResponse> {

    public BadTool(String name) {
        super(
            name,
            "Tool not found: " + name,
            null, // No parent toolkit
            ToolPermission.DENY_NEVER,
            Collections.emptyList()
        );
    }

    @Override
    public BadToolCall createCall(String id, Map<String, Object> args) {
        return new BadToolCall(id, this, args);
    }
}
