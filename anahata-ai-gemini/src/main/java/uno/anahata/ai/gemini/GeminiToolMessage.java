/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package uno.anahata.ai.gemini;

import com.google.genai.types.Content;
import com.google.genai.types.Part;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import uno.anahata.ai.gemini.adapter.GeminiPartAdapter;
import uno.anahata.ai.model.core.AbstractModelMessage;
import uno.anahata.ai.model.core.AbstractToolMessage;

/**
 *
 * @author pablo
 */
public class GeminiToolMessage extends AbstractToolMessage {

    public GeminiToolMessage(AbstractModelMessage modelMessage) {
        super(modelMessage);
    }

    /**
     * Converts this message into a native Google GenAI Content object.
     * This method encapsulates the conversion logic, making the class self-contained.
     *
     * @param includePruned Whether to include parts that are effectively pruned.
     * @return The corresponding Content object, or null if the message has no visible parts.
     */
    public Content toGoogleContent(boolean includePruned) {
        Content.Builder builder = Content.builder()
            .role(getRole().name().toLowerCase());

        List<Part> googleParts = getParts(includePruned).stream()
            .map(part -> new GeminiPartAdapter(part).toGoogle())
            .filter(Objects::nonNull)
            .collect(Collectors.toList());

        if (googleParts.isEmpty()) {
            return null; // Don't create a Content object if there are no visible parts
        }

        builder.parts(googleParts);
        return builder.build();
    }
}
