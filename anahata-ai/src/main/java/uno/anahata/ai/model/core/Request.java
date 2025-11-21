package uno.anahata.ai.model.core;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NonNull;
import uno.anahata.ai.model.provider.AbstractModel;

/**
 * A standardized, model-agnostic request to be sent to an AI model provider.
 * @author pablo
 */
@Getter
@AllArgsConstructor
public class Request {
    /** The model to use for the request. */
    @NonNull
    private final AbstractModel model;
    
    /** The complete conversation history to be sent to the model. */
    @NonNull
    private final List<Message> history;

    /** The configuration for this specific request. */
    private final RequestConfig config;
}