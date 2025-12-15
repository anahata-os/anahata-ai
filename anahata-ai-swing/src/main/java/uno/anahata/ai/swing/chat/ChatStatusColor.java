/*
 * Licensed under the Anahata Software License (ASL) v 108. See the LICENSE file for details. Fora Bara!
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
    IDLE(ChatStatus.IDLE, new Color(40, 167, 69), "idle_waiting_for_user.wav"),
    API_CALL_IN_PROGRESS(ChatStatus.API_CALL_IN_PROGRESS, new Color(0, 123, 255), "api_call_in_progress.wav"),
    TOOL_PROMPT(ChatStatus.TOOL_PROMPT, new Color(255, 193, 7), null),
    CANDIDATE_CHOICE_PROMPT(ChatStatus.CANDIDATE_CHOICE_PROMPT, new Color(23, 162, 184), null),
    TOOL_EXECUTION_IN_PROGRESS(ChatStatus.TOOL_EXECUTION_IN_PROGRESS, new Color(255, 193, 7), "tool_execution_in_progress.wav"),
    WAITING_WITH_BACKOFF(ChatStatus.WAITING_WITH_BACKOFF, new Color(255, 193, 7), "waiting_with_backoff.wav"),
    MAX_RETRIES_REACHED(ChatStatus.MAX_RETRIES_REACHED, new Color(220, 53, 69), "max_retries_reached.wav"),
    SHUTDOWN(ChatStatus.SHUTDOWN, Color.GRAY, null);

    private final ChatStatus status;
    private final Color defaultColor;
    private final String soundFileName;

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
