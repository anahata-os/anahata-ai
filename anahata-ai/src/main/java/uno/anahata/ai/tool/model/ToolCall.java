package uno.anahata.ai.tool.model;

import java.util.Map;

/**
 * Represents a standardized, model-agnostic function call.
 * This record serves as the internal representation of a tool invocation request from the AI model.
 *
 * @param name The name of the function to be called.
 * @param args A map of argument names to their corresponding values.
 */
public record ToolCall(String name, Map<String, Object> args) {
}
