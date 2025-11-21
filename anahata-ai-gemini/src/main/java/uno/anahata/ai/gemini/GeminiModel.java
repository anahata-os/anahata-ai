package uno.anahata.ai.gemini;

import com.google.genai.types.Model;
import java.util.Collections;
import java.util.List;
import lombok.RequiredArgsConstructor;
import uno.anahata.ai.gemini.util.GeminiGsonUtils;
import uno.anahata.ai.model.provider.AbstractAiProvider;
import uno.anahata.ai.model.provider.AbstractModel;

/**
 * Gemini-specific implementation of the AiModel.
 * It wraps the native Google GenAI Model object and implements the abstract
 * methods from the superclass by delegating to the wrapped object.
 *
 * @author anahata
 */
@RequiredArgsConstructor
public class GeminiModel extends AbstractModel {

    private final AbstractAiProvider provider;
    private final Model genaiModel;

    @Override
    public AbstractAiProvider getProvider() {
        return provider;
    }

    @Override
    public String getModelId() {
        return genaiModel.name().orElse("");
    }

    @Override
    public String getDisplayName() {
        return genaiModel.displayName().orElse("");
    }
    
    @Override
    public String getDescription() {
        return genaiModel.description().orElse("No description available.");
    }

    @Override
    public String getVersion() {
        return genaiModel.version().orElse("");
    }

    @Override
    public int getMaxInputTokens() {
        return genaiModel.inputTokenLimit().orElse(0);
    }

    @Override
    public int getMaxOutputTokens() {
        return genaiModel.outputTokenLimit().orElse(0);
    }

    @Override
    public List<String> getSupportedActions() {
        return genaiModel.supportedActions().orElse(Collections.emptyList());
    }
    
    @Override
    public String getRawDescription() {
        String json = GeminiGsonUtils.getPrettyPrintGson().toJson(genaiModel);
        String toString = genaiModel.toString();
        
        // Return only the inner content. WrappingHtmlPane will add the <html><body> tags.
        return "<b>ID: </b>" + escapeHtml(getModelId()) + "<br>"
             + "<b>Display Name: </b>" + escapeHtml(getDisplayName()) + "<br>"
             + "<b>Version: </b>" + escapeHtml(getVersion()) + "<br>"
             + "<b>Description: </b>" + escapeHtml(getDescription())
             + "<hr>"
             + "<b>JSON:</b>"
             + "<pre style='white-space: pre-wrap; word-wrap: break-word;'>" 
             + escapeHtml(json) 
             + "</pre>"
             + "<hr>"
             + "<b>toString():</b>"
             + "<pre style='white-space: pre-wrap; word-wrap: break-word;'>" 
             + escapeHtml(toString) 
             + "</pre>";
    }
    
    private String escapeHtml(String text) {
        return text.replace("&", "&amp;")
                   .replace("<", "&lt;")
                   .replace(">", "&gt;")
                   .replace("\"", "&quot;")
                   .replace("'", "&#x27;")
                   .replace("/", "&#x2F;");
    }
    
    @Override
    public boolean isSupportsFunctionCalling() {
        return getSupportedActions().contains("tool");
    }

    @Override
    public boolean isSupportsContentGeneration() {
        return getSupportedActions().contains("generateContent");
    }

    @Override
    public boolean isSupportsBatchEmbeddings() {
        return getSupportedActions().contains("batchEmbedContents");
    }

    @Override
    public boolean isSupportsEmbeddings() {
        return getSupportedActions().contains("embedContent");
    }

    @Override
    public boolean isSupportsCachedContent() {
        return getSupportedActions().contains("createCachedContent");
    }
}
