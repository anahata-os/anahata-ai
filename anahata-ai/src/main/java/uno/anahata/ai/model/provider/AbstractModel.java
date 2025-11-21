package uno.anahata.ai.model.provider;

import java.util.List;
import uno.anahata.ai.model.provider.AbstractAiProvider;

/**
 * Base class for all AI models, defining a common set of capabilities.
 * Provider-specific implementations will extend this class.
 *
 * @author anahata
 */
public abstract class AbstractModel {

    public abstract AbstractAiProvider getProvider();
    
    public abstract String getModelId();

    public abstract String getDisplayName();
    
    public abstract String getDescription();

    public abstract String getVersion();

    public abstract int getMaxInputTokens();

    public abstract int getMaxOutputTokens();

    public abstract List<String> getSupportedActions();

    /**
     * Gets a detailed, provider-specific raw description of the model,
     * typically for UI tooltips or debugging.
     * @return A formatted string (e.g., HTML) with the model's raw details.
     */
    public abstract String getRawDescription();
    
    public String getProviderId() {
        return getProvider().getProviderId();
    }
    
    // --- Abstract Capability Methods ---
    public abstract boolean isSupportsFunctionCalling();
    public abstract boolean isSupportsContentGeneration();
    public abstract boolean isSupportsBatchEmbeddings();
    public abstract boolean isSupportsEmbeddings();
    public abstract boolean isSupportsCachedContent();
}
