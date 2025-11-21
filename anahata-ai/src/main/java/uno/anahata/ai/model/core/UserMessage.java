package uno.anahata.ai.model.core;

/**
 * Represents a message originating from the user.
 *
 * @author anahata-gemini-pro-2.5
 */
public class UserMessage extends AbstractMessage {
    @Override
    public Role getRole() {
        return Role.USER;
    }
}
