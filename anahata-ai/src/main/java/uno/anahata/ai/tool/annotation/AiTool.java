package uno.anahata.ai.tool.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marks a method as an AI-callable tool.
 * <p>
 * The {@link uno.anahata.ai.tool.ToolManager} will scan for this annotation to discover
 * and register methods that the AI model can execute.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface AiTool {
    /**
     * A clear and concise description of what the tool does. This description
     * is provided to the AI model to help it decide when to use the tool.
     * @return The tool's description.
     */
    String value();
}
