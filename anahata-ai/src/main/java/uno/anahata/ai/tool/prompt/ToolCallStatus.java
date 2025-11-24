/* Licensed under the Anahata Software License (ASL) v 108. See the LICENSE file for details. Fora Bara! */
package uno.anahata.ai.tool.prompt;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * Defines the final status of a tool call after user confirmation and execution.
 *
 * @author anahata-gemini-pro-2.5
 */
@Getter
@RequiredArgsConstructor
public enum ToolCallStatus {
    /** The user explicitly approved the tool call for this single turn. */
    YES("Approved (Single Turn)"),
    /** The user explicitly denied the tool call for this single turn. */
    NO("Denied (Single Turn)"),
    /** The tool call was automatically approved because the user set the permission to 'Always'. */
    ALWAYS("Approved (Always)"),
    /** The tool call was automatically denied because the user set the permission to 'Never'. */
    NEVER("Denied (Never)"),
    /** The user cancelled the entire batch of tool calls. */
    CANCELLED("Cancelled Batch"),
    /** The tool call failed during execution (e.g., threw an exception). */
    FAILED("Execution Failed");

    private final String displayName;
}
