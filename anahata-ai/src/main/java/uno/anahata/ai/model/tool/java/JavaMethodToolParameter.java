package uno.anahata.ai.model.tool.java;

import java.lang.reflect.Type;
import lombok.Getter;
import lombok.NonNull;
import uno.anahata.ai.model.tool.ToolParameter;

/**
 * A subclass of ToolParameter that holds Java-specific reflection information,
 * namely the full generic Type of the parameter.
 * @author anahata-gemini-pro-2.5
 */
@Getter
public class JavaMethodToolParameter extends ToolParameter {
    /** The full Java reflection Type, preserving generics. */
    @NonNull
    private final Type javaType;

    public JavaMethodToolParameter(
            @NonNull String name,
            @NonNull String description,
            @NonNull String jsonSchema,
            boolean required,
            String rendererId,
            @NonNull Type javaType) {
        super(name, description, jsonSchema, required, rendererId);
        this.javaType = javaType;
    }
}