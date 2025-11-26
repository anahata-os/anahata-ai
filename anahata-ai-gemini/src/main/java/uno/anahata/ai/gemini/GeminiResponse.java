/*
 * Licensed under the Anahata Software License (ASL) v 108. See the LICENSE file for details. Força Barça!
 */
package uno.anahata.ai.gemini;

import com.google.genai.types.Candidate;
import com.google.genai.types.GenerateContentResponse;
import com.google.genai.types.GenerateContentResponseUsageMetadata;
import java.util.List;
import java.util.stream.Collectors;
import lombok.Getter;
import uno.anahata.ai.chat.Chat;
import uno.anahata.ai.model.core.Response;

/**
 * A specialized, object-oriented Response class for the Gemini provider.
 * It encapsulates all the logic for converting a native Google GenerateContentResponse
 * into the Anahata domain model, making it a self-contained and reusable component.
 *
 * @author pablo
 */
@Getter
public class GeminiResponse extends Response<GeminiModelMessage> {

    /** The original, native response object from the Google GenAI API. */
    private final transient GenerateContentResponse genaiResponse;

    // --- Final fields to hold the converted data ---
    private final List<GeminiModelMessage> candidates;
    private final String finishReason;
    private final int promptTokenCount;
    private final int totalTokenCount;

    /**
     * Constructs a GeminiResponse, performing the full conversion from the native
     * Google GenAI response to the Anahata domain model.
     *
     * @param chat          The parent chat session, required for constructing model messages.
     * @param modelId       The ID of the model that generated this response.
     * @param genaiResponse The native response object from the API.
     */
    public GeminiResponse(Chat chat, String modelId, GenerateContentResponse genaiResponse) {
        super(); // Clean super() call to the abstract class constructor.
        this.genaiResponse = genaiResponse;

        // --- Conversion Logic ---
        this.candidates = genaiResponse.candidates().get().stream()
            .map(candidate -> {
                GeminiModelMessage message = new GeminiModelMessage(chat, modelId, candidate.content().get());
                message.setTokenCount(candidate.tokenCount().orElse(0));
                return message;
            })
            .collect(Collectors.toList());

        this.promptTokenCount = genaiResponse.usageMetadata().flatMap(GenerateContentResponseUsageMetadata::promptTokenCount).orElse(0);
        this.totalTokenCount = genaiResponse.usageMetadata().flatMap(GenerateContentResponseUsageMetadata::totalTokenCount).orElse(0);

        this.finishReason = genaiResponse.candidates().get().stream().findFirst()
            .flatMap(Candidate::finishReason)
            .map(Object::toString)
            .orElse("UNKNOWN");
    }

    // --- Implementation of Abstract Methods ---

    @Override
    public List<GeminiModelMessage> getCandidates() {
        return candidates;
    }

    @Override
    public String getFinishReason() {
        return finishReason;
    }

    @Override
    public int getPromptTokenCount() {
        return promptTokenCount;
    }

    @Override
    public int getTotalTokenCount() {
        return totalTokenCount;
    }
}
