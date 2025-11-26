/* Licensed under the Anahata Software License (ASL) v 108. See the LICENSE file for details. Fora Bara! */
package uno.anahata.ai.gemini.adapter;

import com.google.genai.types.FunctionResponse;
import com.google.genai.types.Part;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import uno.anahata.ai.model.core.AbstractPart;
import uno.anahata.ai.model.core.TextPart;
import uno.anahata.ai.model.tool.AbstractToolResponse;

/**
 * An object-oriented adapter that converts a single Anahata AbstractPart into a
 * native Google GenAI Part.
 *
 * @author anahata-ai
 */
@Slf4j
@RequiredArgsConstructor
public class GeminiPartAdapter {
    private static final Gson GSON = new Gson();

    private final AbstractPart anahataPart;

    /**
     * Performs the conversion from the Anahata part to a Google GenAI Part.
     * @return The corresponding Google GenAI Part, or null if unsupported.
     */
    public Part toGoogle() {
        if (anahataPart instanceof TextPart) {
            return Part.fromText(((TextPart) anahataPart).getText());
        }
        if (anahataPart instanceof AbstractToolResponse) {
            return toGoogleFunctionResponsePart((AbstractToolResponse) anahataPart);
        }
        log.warn("Unsupported Anahata Part type for Google conversion, skipping: {}", anahataPart.getClass().getSimpleName());
        return null;
    }

    private Part toGoogleFunctionResponsePart(AbstractToolResponse anahataResponse) {
        // Serialize the result object into a generic Map for Google
        String json = GSON.toJson(anahataResponse.getResult());
        Map<String, Object> responseMap = GSON.fromJson(json, new TypeToken<Map<String, Object>>() {}.getType());

        FunctionResponse fr = FunctionResponse.builder()
            .name(anahataResponse.getCall().getName())
            .response(responseMap)
            .build();
        
        return Part.builder().functionResponse(fr).build();
    }
}
