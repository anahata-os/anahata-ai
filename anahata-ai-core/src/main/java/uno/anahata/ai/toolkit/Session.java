/*
 * Licensed under the Anahata Software License (ASL) v 108. See the LICENSE file for details. Força Barça!
 */
package uno.anahata.ai.toolkit;

import java.util.Collections;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import uno.anahata.ai.context.ContextManager;
import uno.anahata.ai.context.ContextProvider;
import uno.anahata.ai.model.core.RagMessage;
import uno.anahata.ai.tool.AiTool;
import uno.anahata.ai.tool.AiToolParam;
import uno.anahata.ai.tool.AiToolkit;
import uno.anahata.ai.tool.AnahataToolkit;

/**
 * A toolkit for managing the current chat session's metadata.
 * 
 * @author anahata-gemini-pro-2.5
 */
@Slf4j
@AiToolkit("Tools for managing the current chat session's metadata. " +
           "STRICT USAGE RULE: These tools MUST ONLY be called if there are other task-related tool calls in the same turn. " +
           "NEVER call these tools as the sole action in a turn.")
public class Session extends AnahataToolkit {

    /**
     * Updates the current chat session's summary.
     * <p>
     * <b>STRICT USAGE RULE:</b> This tool MUST ONLY be called if there are other 
     * "task-related" tool calls (e.g., file manipulation, shell commands, pruning) 
     * being made in the same turn. It should NEVER be called as the sole tool 
     * in a turn, as its purpose is background maintenance and it should not 
     * trigger an extra conversation turn on its own.
     * 
     * @param summary A concise summary of the conversation's current state or topic.
     * @return A confirmation message.
     */
    @AiTool(value = "Updates the current chat session's summary. " +
            "STRICT USAGE RULE: Only call this if other task-related tools are being called in the same turn.",
            requiresApproval = false)
    public String updateSessionSummary(@AiToolParam("A concise summary of the conversation's current state.") String summary) {
        uno.anahata.ai.chat.Chat domainChat = getChat();
        if (summary != null && !summary.isBlank()) {
            domainChat.setSummary(summary);
        }
        log.info("Session summary updated: summary={}", summary);
        return "Session summary updated successfully.";
    }
    
    @AiTool(value = "Enables / disables context providers", retention = 0)
    public void updateContextProviders(
            @AiToolParam("Whether to enable or disable the providers.") boolean enabled, 
            @AiToolParam("The IDs of the context providers to update.") List<String> providerIds) {
        ContextManager cm = getChat().getContextManager();
        for (ContextProvider cp : cm.getProviders()) {
            if (providerIds.contains(cp.getId())) {
                cp.setEnabled(enabled);
                log((enabled ? "Enabled" : "Disabled") + " provider: " + cp.getName());
            }
        }
    }

    @Override
    public void populateMessage(RagMessage ragMessage) throws Exception {
        uno.anahata.ai.chat.Chat domainChat = ragMessage.getChat();
        StringBuilder sb = new StringBuilder();
        sb.append("## Current Session Metadata\n");
        sb.append("- **Session ID**: ").append(domainChat.getConfig().getSessionId()).append("\n");
        sb.append("- **Nickname**: ").append(domainChat.getNickname()).append("\n");
        sb.append("- **Summary**: ").append(domainChat.getSummary() != null ? domainChat.getSummary() : "N/A").append("\n");
        sb.append("- **Total Messages**: ").append(domainChat.getContextManager().getHistory().size()).append("\n");
        sb.append("- **Context Usage**: ").append(String.format("%.1f%%", domainChat.getContextWindowUsage() * 100))
          .append(" (").append(domainChat.getLastTotalTokenCount()).append(" / ").append(domainChat.getConfig().getTokenThreshold()).append(" tokens)\n");
        ragMessage.addPart(sb.toString());
    }

    @Override
    public List<String> getSystemInstructionParts(uno.anahata.ai.chat.Chat chat) throws Exception {
        return Collections.emptyList();
    }
}
