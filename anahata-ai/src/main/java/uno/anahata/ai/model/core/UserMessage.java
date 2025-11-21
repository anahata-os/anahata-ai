package uno.anahata.ai.model.core;

/**
 * Represents a message originating from the user.
 *
 * @author Anahata
 */
public class UserMessage extends Message {
    @Override
    public Role getRole() {
        return Role.USER;
    }
}
