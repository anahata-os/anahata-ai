package uno.anahata.ai.model.tool;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NonNull;

/**
 * A rich, self-documenting representation of a single parameter for a tool method.
 * This replaces the primitive Map-based approach with a robust, object-oriented model.
 *
 * @author pablo
 */
@Getter
@Builder
@AllArgsConstructor
public class ToolParameter {
    /** The name of the parameter. */
    @NonNull
    private final String name;

    /** A detailed description of the parameter's purpose and expected format. */
    @NonNull
    private final String description;

    /** A pre-generated, language-agnostic JSON schema for this parameter. */
    @NonNull
    private final String jsonSchema;

    /** Whether this parameter is required for the tool call. */
    private final boolean required;

    /** An optional identifier for a custom UI renderer for this parameter. */
    private final String rendererId;
}