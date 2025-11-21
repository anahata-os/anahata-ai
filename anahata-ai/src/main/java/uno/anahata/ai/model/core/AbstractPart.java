package uno.anahata.ai.model.core;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Getter;
import lombok.Setter;

/**
 * The abstract base class for all components of a {@link AbstractMessage}.
 * <p>
 * This rich, hierarchical model allows for type-safe handling of different
 * content types (text, tool calls, etc.) and provides common functionality
 * like a back-reference to the parent message and a pruning flag.
 */
@Getter
@Setter
public abstract class AbstractPart {
    /**
     * A backward reference to the Message that contains this part.
     * This is for runtime convenience and is ignored during schema generation
     * to keep the public contract clean.
     */
    @JsonIgnore
    private AbstractMessage message;

    /**
     * A flag indicating whether this part has been pruned from the context.
     */
    private boolean pruned = false;
}
