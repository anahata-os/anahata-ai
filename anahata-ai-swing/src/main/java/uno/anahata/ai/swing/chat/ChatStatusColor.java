/*
 * Licensed under the Anahata Software License (ASL) v 108. See the LICENSE file for details. Força Barça!
 */
package uno.anahata.ai.swing.chat;

import java.awt.Color;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import uno.anahata.ai.status.ChatStatus;

/**
 * Maps a core {@link ChatStatus} to its Swing-specific UI properties (color and sound file).
 *
 * @author anahata
 */
@RequiredArgsConstructor
@Getter
public enum ChatStatusColor {
    IDLE(ChatStatus.IDLE, new Color(40, 167, 69)),
    API_CALL_IN_PROGRESS(ChatStatus.API_CALL_IN_PROGRESS, new Color(0, 123, 255)),
    TOOL_PROMPT(ChatStatus.TOOL_PROMPT, new Color(255, 193, 7)),
    CANDIDATE_CHOICE_PROMPT(ChatStatus.CANDIDATE_CHOICE_PROMPT, new Color(23, 162, 184)),
    TOOL_EXECUTION_IN_PROGRESS(ChatStatus.TOOL_EXECUTION_IN_PROGRESS, new Color(255, 193, 7)),
    WAITING_WITH_BACKOFF(ChatStatus.WAITING_WITH_BACKOFF, new Color(255, 193, 7)),
    MAX_RETRIES_REACHED(ChatStatus.MAX_RETRIES_REACHED, new Color(220, 53, 69)),
    SHUTDOWN(ChatStatus.SHUTDOWN, Color.GRAY);

    private final ChatStatus status;
    private final Color defaultColor;

    /**
     * Finds the {@code ChatStatusColor} enum constant corresponding to the given core {@code ChatStatus}.
     *
     * @param status The core status.
     * @return The corresponding {@code ChatStatusColor}.
     */
    public static ChatStatusColor fromStatus(ChatStatus status) {
        for (ChatStatusColor color : values()) {
            if (color.status == status) {
                return color;
            }
        }
        // Should not happen if all core statuses are mapped
        return IDLE;
    }
}