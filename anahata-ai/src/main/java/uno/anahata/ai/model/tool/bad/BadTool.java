package uno.anahata.ai.model.tool.bad;

import java.lang.reflect.Type;
import java.util.Collections;
import java.util.Map;
import uno.anahata.ai.model.tool.AbstractTool;
import uno.anahata.ai.model.tool.ToolParameter;
import uno.anahata.ai.model.tool.ToolPermission;

/**
 * A special tool implementation representing a tool that was requested by the
 * model but was not found in the registered toolkits.
 *
 * @author pablo
 */
public class BadTool extends AbstractTool<ToolParameter, BadToolCall> {

    public BadTool(String name) {
        super(
            name,
            "Tool not found: " + name,
            null, // No parent toolkit
            ToolPermission.DENY_NEVER,
            Collections.emptyList(),
            null // No return type schema for a bad tool
        );
    }

    @Override
    public BadToolCall createCall(String id, Map<String, Object> args) {
        return new BadToolCall(id, this, args);
    }

    @Override
    public Type getResponseType() {
        return BadToolResponse.class;
    }
}
