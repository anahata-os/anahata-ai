/* Licensed under the Anahata Software License (ASL) v 108. See the LICENSE file for details. Fora Bara! */
package uno.anahata.ai.tool.prompt;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * Represents the user's choice for a tool call during the confirmation prompt.
 *
 * @author anahata-gemini-pro-2.5
 */
@Getter
@RequiredArgsConstructor
public enum ToolConfirmation {
    /** Approve the tool call for this single turn. */
    YES("Approve"),
    /** Deny the tool call for this single turn. */
    NO("Deny"),
    /** Approve the tool call and set the preference to 'Always Approve'. */
    ALWAYS("Always Approve"),
    /** Deny the tool call and set the preference to 'Never Approve'. */
    NEVER("Never Approve");

    private final String displayName;
}
