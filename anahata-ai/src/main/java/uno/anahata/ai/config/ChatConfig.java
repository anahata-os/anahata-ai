package uno.anahata.ai.config;

import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import uno.anahata.ai.AiConfig;

/**
 * A model-agnostic, intelligent configuration object for a single chat session.
 * It is not a simple DTO but contains logic for session-specific settings.
 */
@Getter
@RequiredArgsConstructor
public class ChatConfig {

    /** A reference to the global, application-wide configuration. */
    @NonNull
    private final AiConfig aiConfig;

    /** The unique identifier for this specific chat session. */
    @NonNull
    private final String sessionId;

    /** The identifier for the AI model to be used in this session (e.g., "gemini-pro-latest"). */
    @NonNull
    private final String modelId;

    /**
     * Gets the list of tool classes to be used in this chat session.
     * This can be overridden by subclasses to provide a custom set of tools.
     * @return A list of classes annotated with @AITool.
     */
    public List<Class<?>> getToolClasses() {
        // Default implementation can be provided here or left abstract
        return new ArrayList<>();
    }

    /**
     * Gets the list of context providers to be used in this chat session.
     * @return A list of ContextProvider instances.
     */
    /*
    public List<ContextProvider> getContextProviders() {
        // Default implementation can be provided here or left abstract
        return new ArrayList<>();
    }*/
    
    /**
     * Convenience method to get the host application ID from the parent AiConfig.
     * @return The host application ID.
     */
    public String getHostApplicationId() {
        return aiConfig.getHostApplicationId();
    }
}