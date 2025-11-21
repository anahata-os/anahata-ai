package uno.anahata.ai.tool;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marks a method as an AI-callable tool and provides essential metadata.
 * This is the cornerstone of the V2 tool framework.
 *
 * @author anahata
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface AiToolkit {

    /**
     * A detailed description of what the tools on this toolkit do, including its purpose,
     * usage notes, etc.
     */
    String value();

    /**
     * The default retention policy for ALL of this toolkit's tools in number of user turns. 
     * This serves as a default for any tools in this toolkit that do 
     * not specify a retention policy.
     */
    int retention() default 5; // System default
}
