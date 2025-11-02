package uno.anahata.ai.gemini.model.provider.model;

import com.google.genai.types.Model;
import java.util.Collections;
import java.util.List;
import lombok.RequiredArgsConstructor;
import uno.anahata.ai.gemini.util.GeminiGsonUtils;
import uno.anahata.ai.model.provider.AbstractAiProvider;
import uno.anahata.ai.model.provider.model.AbstractAiModel;

/**
 * Gemini-specific implementation of the AiModel.
 * It wraps the native Google GenAI Model object and implements the abstract
 * methods from the superclass by delegating to the wrapped object.
 *
 * @author anahata
 */
@RequiredArgsConstructor
public class GeminiAiModel extends AbstractAiModel {

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
        
        // Use HTML for a nicely formatted tooltip
        return "<html>"
             + "<body>"
             + "<h3>Raw GenAI Model Details</h3>"
             + "<b>JSON:</b>"
             + "<pre>" + escapeHtml(json) + "</pre>"
             + "<hr>"
             + "<b>toString():</b>"
             + "<pre>" + escapeHtml(toString) + "</pre>"
             + "</body>"
             + "</html>";
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
        // The Gemini API uses "tool" for function calling
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
