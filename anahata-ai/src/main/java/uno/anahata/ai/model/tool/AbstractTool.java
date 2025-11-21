package uno.anahata.ai.model.tool;

import java.util.List;
import java.util.Map;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

/**
 * The abstract base class for a tool, now generic on its Parameter and Call types.
 * @author pablo
 * @param <P> The specific subclass of ToolParameter this tool uses.
 * @param <C> The specific subclass of AbstractToolCall this tool creates.
 */
@RequiredArgsConstructor
@Getter
public abstract class AbstractTool<P extends ToolParameter, C extends AbstractToolCall> {
    /** The fully qualified name of the tool, e.g., "LocalFiles.readFile". */
    @NonNull
    private final String name;

    /** A detailed description of what the tool does. */
    @NonNull
    private final String description;

    /** A reference to the parent toolkit that owns this tool. Can be null for standalone tools. */
    private final AbstractToolkit toolkit;

    /** The user's configured preference for this tool, determining its execution behavior. */
    @Setter
    @Getter
    @NonNull
    private ToolPermission permission;

    /** The number of turns this tool call should be retained in the context. */
    @Setter
    private int retentionTurns;

    /** A rich, ordered list of the tool's parameters. */
    @NonNull
    private final List<P> parameters;
    
    /** A pre-generated, language-agnostic JSON schema for the tool's return type. Can be null for void methods. */
    private final String returnTypeSchema;

    /**
     * Factory method to create a tool-specific call object from raw model data.
     * @param id The call ID.
     * @param args The raw arguments from the model.
     * @return A new tool call instance.
     */
    public abstract C createCall(String id, Map<String, Object> args);

}
