/* Licensed under the Anahata Software License (ASL) v 108. See the LICENSE file for details. Fora Bara! */
package uno.anahata.ai.tool;

import java.util.List;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import uno.anahata.ai.model.tool.AbstractToolResponse;
import uno.anahata.ai.tool.prompt.ToolCallOutcome;

/**
 * A container for the results of processing a model's response that contained
 * one or more tool calls.
 *
 * @author anahata-gemini-pro-2.5
 */
@Getter
@RequiredArgsConstructor
public class ToolProcessingResult {
    /** The list of tool responses generated from executed tool calls. */
    @NonNull
    private final List<AbstractToolResponse> toolResponses;

    /** The outcome (status and user comment) for every tool call proposed by the model. */
    @NonNull
    private final List<ToolCallOutcome> outcomes;

    /** A comment provided by the user during the tool confirmation prompt. */
    private final String userComment;
}
