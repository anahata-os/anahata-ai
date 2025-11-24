/* Licensed under the Anahata Software License (ASL) v 108. See the LICENSE file for details. Fora Bara! */
package uno.anahata.ai.tool.prompt;

import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import uno.anahata.ai.model.tool.AbstractToolCall;

/**
 * Represents the final status of a single tool call proposed by the model,
 * including the user's decision (if any).
 *
 * @author anahata-gemini-pro-2.5
 */
@Getter
@RequiredArgsConstructor
public class ToolCallOutcome {
    @NonNull
    private final AbstractToolCall toolCall;
    
    @NonNull
    private final ToolCallStatus status;
}
