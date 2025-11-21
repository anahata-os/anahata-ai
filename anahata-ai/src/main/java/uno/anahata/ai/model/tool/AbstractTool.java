/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Interface.java to edit this template
 */
package uno.anahata.ai.model.tool;

import java.util.List;
import java.util.Map;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

/**
 *
 * @author pablo
 */
@RequiredArgsConstructor
@Getter
public abstract class AbstractTool<T extends AbstractToolCall, U extends AbstractToolResponse> {
    /** The fully qualified name of the tool, e.g., "LocalFiles.readFile". */
    @NonNull
    private final String name;

    /** A detailed description of what the tool does. */
    @NonNull
    private final String description;

    /** A reference to the parent toolkit that owns this tool. Can be null for standalone tools. */
    private final Toolkit toolkit;

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
    private final List<ToolParameter> parameters;

    /**
     * Factory method to create a tool-specific call object from raw model data.
     * @param id The call ID.
     * @param args The raw arguments from the model.
     * @return A new tool call instance.
     */
    public abstract T createCall(String id, Map<String, Object> args);

}
