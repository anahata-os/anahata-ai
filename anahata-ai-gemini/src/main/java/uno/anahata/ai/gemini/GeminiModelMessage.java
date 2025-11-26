/* Licensed under the Anahata Software License (ASL) v 108. See the LICENSE file for details. Fora Bara! */
package uno.anahata.ai.gemini;

import com.google.genai.types.Content;
import com.google.genai.types.FunctionCall;
import com.google.genai.types.Part;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import uno.anahata.ai.chat.Chat;
import uno.anahata.ai.model.core.AbstractModelMessage;
import uno.anahata.ai.model.core.AbstractPart;
import uno.anahata.ai.model.core.AbstractToolMessage;
import uno.anahata.ai.model.core.TextPart;
import uno.anahata.ai.model.tool.AbstractToolCall;

/**
 * An object-oriented representation of a ModelMessage derived from the Gemini provider.
 * This class encapsulates the logic for constructing a valid ModelMessage from a
 * Gemini Content object, ensuring the parent message is created before its parts.
 *
 * @author anahata-ai
 */
@Slf4j
@Getter
public class GeminiModelMessage extends AbstractModelMessage<GeminiToolMessage> {

    /** The original Gemini Content object from which this message was constructed. */
    private final transient Content geminiContent;

    /**
     * Constructs a GeminiModelMessage, encapsulating the conversion logic.
     *
     * @param chat          The parent chat session.
     * @param modelId       The ID of the model that generated the content.
     * @param geminiContent The source Gemini Content object.
     */
    public GeminiModelMessage(Chat chat, String modelId, Content geminiContent) {
        super(chat, modelId);
        this.geminiContent = geminiContent;
        
        // All construction logic is now encapsulated here. The parent (this) exists
        // before any child parts are created and added. The AbstractPart constructor
        // adds the part to the message, so we just need a terminal operation to
        // trigger the stream.
        geminiContent.parts().ifPresent(parts -> parts.stream()
            .map(this::toAnahataPart)
            .filter(Objects::nonNull)
            .collect(Collectors.toList())); // Use a terminal operation that doesn't re-add the parts.
    }

    /**
     * Converts a Google GenAI Part to an Anahata AbstractPart within the context of this message.
     * This method encapsulates the logic previously in PartAdapter and FunctionCallAdapter.
     *
     * @param googlePart The Google part to convert.
     * @return The corresponding Anahata AbstractPart, or null if unsupported.
     */
    private AbstractPart toAnahataPart(Part googlePart) {
        if (googlePart.text().isPresent()) {
            return new TextPart(this, googlePart.text().get());
        }
        if (googlePart.functionCall().isPresent()) {
            return toAnahataToolCall(googlePart.functionCall().get());
        }
        log.warn("Unsupported Gemini Part type for Anahata conversion, skipping: {}", googlePart);
        return null;
    }

    /**
     * Converts a Google GenAI FunctionCall to an Anahata AbstractToolCall.
     *
     * @param googleFc The FunctionCall received from the Google API.
     * @return A new AbstractToolCall.
     */
    private AbstractToolCall toAnahataToolCall(FunctionCall googleFc) {
        String name = googleFc.name().orElse("");
        Map<String, Object> args = googleFc.args().orElse(Map.of());
        String id = googleFc.id().orElse(null);

        // The ToolManager is accessible via the Chat reference in the message.
        return getChat().getToolManager().createToolCall(this, id, name, args);
    }

    @Override
    protected GeminiToolMessage createToolMessage() {
        return new GeminiToolMessage(this);
    }
    
    
}
