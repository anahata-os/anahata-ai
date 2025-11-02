package uno.anahata.ai.tool.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Describes a parameter for a method annotated with {@link AiTool}.
 * <p>
 * This annotation provides a human-readable description of a tool's parameter,
 * which is used to generate the schema for the AI model.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.PARAMETER)
public @interface AiToolParam {
    /**
     * A clear and concise description of what this parameter represents and
     * what value is expected.
     * @return The parameter's description.
     */
    String value();
}
