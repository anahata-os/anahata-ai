/* Licensed under the Anahata Software License (ASL) v 108. See the LICENSE file for details. Fora Bara! */
package uno.anahata.ai.tool.prompt;

import java.util.Map;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import uno.anahata.ai.model.tool.AbstractToolCall;

/**
 * The result returned by the {@link ToolPrompter} after user interaction,
 * containing the user's decisions for a batch of tool calls.
 *
 * @author anahata-gemini-pro-2.5
 */
@Getter
@RequiredArgsConstructor
public class PromptResult {
    /** The user's decision for each tool call in the batch. */
    @NonNull
    private final Map<AbstractToolCall, ToolConfirmation> toolConfirmations;
    
    /** A comment provided by the user during the prompt. */
    private final String userComment;
    
    /** True if the user cancelled the entire batch operation. */
    private final boolean cancelled;
}
