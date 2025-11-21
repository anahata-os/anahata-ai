package uno.anahata.ai.tool;

/**
 * A custom exception that can be thrown by AI tools to provide a concise,
 * user-friendly error message to the model without including a stack trace.
 *
 * @author anahata
 */
public class AiToolException extends Exception {

    public AiToolException(String message) {
        super(message);
    }

    public AiToolException(String message, Throwable cause) {
        super(message, cause);
    }
}
