package uno.anahata.ai.tool.model;

import java.util.Map;

/**
 * Represents a standardized, model-agnostic response from an executed tool.
 * This record serves as the internal representation of a tool's output,
 * which will be sent back to the AI model.
 *
 * @param name The name of the function that was called.
 * @param content A map representing the structured output of the tool.
 */
public record ToolResponse(String name, Map<String, Object> content) {
}
