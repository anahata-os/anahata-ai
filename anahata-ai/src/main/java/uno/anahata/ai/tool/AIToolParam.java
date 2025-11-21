package uno.anahata.ai.tool;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Provides a description for a parameter of a method marked with {@link AIToolMethod}.
 * This is essential for the model to understand how to use the tool correctly.
 *
 * @author anahata-gemini-pro-2.5
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.PARAMETER)
public @interface AIToolParam {
    /**
     * A clear and concise description of the parameter's purpose.
     */
    String value();
    
    boolean required() default true;
}
