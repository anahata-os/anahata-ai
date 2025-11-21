package uno.anahata.ai.model.core;

import java.util.List;
import lombok.Builder;
import lombok.Getter;
import uno.anahata.ai.model.tool.AbstractTool;

/**
 * A comprehensive, model-agnostic configuration for a single generateContent request.
 * @author pablo
 */
@Getter
@Builder
public class RequestConfig {
    /**
     * A list of tools that the model may call.
     */
    private final List<AbstractTool> tools;

    /**
     * A list of TextParts to be used as system instructions.
     */
    private final List<TextPart> systemInstructions;

    /**
     * Controls the randomness of the output. Must be a value between 0.0 and 1.0.
     */
    private final Float temperature;

    /**
     * The maximum number of tokens to generate in the response.
     */
    private final Integer maxOutputTokens;
    
    /**
     * The maximum number of tokens to generate in the response.
     * The model considers only the **K** most likely next tokens.
     */
    private final Integer topK;
    
    /**
     * The maximum number of tokens to generate in the response.
     * The model considers the smallest set of tokens whose cumulative probability exceeds the threshold **P**.
     */
    private final Float topP;
    
    /**
     * A flag to indicate whether the model should include its internal "thoughts" or reasoning process in the output.
     * Note: This is a conceptual parameter; actual support depends on the provider and model.
     */
    private final boolean includeThoughts;
}
