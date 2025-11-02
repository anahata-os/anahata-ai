package uno.anahata.ai.tool.conversion;

import java.util.Collections;
import java.util.Map;

/**
 * Handles the conversion of Java objects returned by tool methods into a
 * standardized, serializable format (a Map) for the AnahataToolResponse.
 * This class will form the bridge between arbitrary Java return types and
 * the structured data required by the AI model.
 */
public class TypeConverter {

    /**
     * Converts a given object into a Map representation.
     * <p>
     * This method will be expanded to use reflection and JSON serialization
     * (e.g., with Gson or Jackson) to handle complex POJOs, respecting
     * annotations like @Schema to guide the serialization process.
     *
     * @param result The object returned from a tool method.
     * @return A Map representing the object, suitable for a FunctionResponse.
     */
    @SuppressWarnings("unchecked")
    public Map<String, Object> convert(Object result) {
        if (result == null) {
            // Return a map with a defined "output" key for null results.
            return Collections.singletonMap("output", "");
        }
        
        // If the result is already a map, assume it's in the correct format.
        if (result instanceof Map) {
            // This is an unsafe cast, but it's a common pattern in this context.
            // A more robust implementation would validate the map's key/value types.
            return (Map<String, Object>) result;
        }
        
        // For any other object type (primitives, strings, POJOs), wrap it
        // in a standard map structure. The actual serialization of POJOs
        // will be implemented here later.
        return Collections.singletonMap("output", result);
    }
}
