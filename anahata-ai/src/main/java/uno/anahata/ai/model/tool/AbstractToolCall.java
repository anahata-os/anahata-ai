package uno.anahata.ai.model.tool;

import com.fasterxml.jackson.annotation.JsonIgnore;
import java.util.Map;
import lombok.Getter;
import lombok.NonNull;
import uno.anahata.ai.model.core.AbstractPart;

/**
 * Represents a request to execute a specific tool. It holds a direct reference
 * to the tool's definition and its corresponding response.
 * @author pablo
 * @param <T> The specific type of the Tool.
 * @param <R> The specific type of the Response.
 */
@Getter
public abstract class AbstractToolCall<T extends AbstractTool, R extends AbstractToolResponse> extends AbstractPart {

    /**
     * A unique, immutable identifier for this specific invocation request.
     */
    @NonNull
    private final String id;

    /**
     * The tool definition that this call corresponds to.
     */
    @NonNull
    private final T tool;

    /**
     * The arguments for the method, provided as a map of parameter names to
     * values.
     */
    @NonNull
    private final Map<String, Object> args;
    
    /**
     * The single, final response object associated with this call.
     * This is ignored during schema generation to prevent a circular reference.
     */
    @NonNull
    @JsonIgnore
    private final R response;

    public AbstractToolCall(@NonNull String id, @NonNull T tool, @NonNull Map<String, Object> args) {
        this.id = id;
        this.tool = tool;
        this.args = args;
        this.response = createResponse();
    }

    /**
     * Gets the name of the tool to be invoked.
     * @return The tool's name.
     */
    public String getName() {
        return tool.getName();
    }

    /**
     * Creates the corresponding response object for this tool call.
     * This acts as a factory method and is called once by the constructor.
     * @return A new, un-executed tool response.
     */
    protected abstract R createResponse();
    
    @Override
    public String asText() {
        return "[Tool Call: " + getName() + " with args: " + getArgs().toString() + "]";
    }
}