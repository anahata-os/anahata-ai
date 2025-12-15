/*
 * Licensed under the Anahata Software License (ASL) v 108. See the LICENSE file for details. Força Barça!
 */
package uno.anahata.ai.context.system;

import java.util.Collections;
import java.util.List;
import uno.anahata.ai.chat.Chat;
import uno.anahata.ai.status.ChatStatus;

/**
 * A context provider that injects the current status of the chat into the prompt.
 * This is a direct port of the proven V1 provider.
 * 
 * @author pablo
 */
public class ChatStatusProvider extends AbstractSystemInstructionsProvider {

    public ChatStatusProvider(Chat chat) {
        super(chat, "core-chat-status", "Chat Status", "Provides the current status of the chat session.");
    }

    @Override
    public List<String> getSystemInstructions() throws Exception {
        ChatStatus status = chat.getStatusManager().getCurrentStatus();
        String statusString = String.format("Current Chat Status: %s (%s)", status.getDisplayName(), status.getDescription());
        return Collections.singletonList(statusString);
    }
}
