/*
 * Licensed under the Anahata Software License (ASL) v 108. See the LICENSE file for details. Força Barça!
 */
package uno.anahata.ai.swing.chat.render;

import java.awt.BorderLayout;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import lombok.NonNull;
import uno.anahata.ai.internal.TextUtils;
import uno.anahata.ai.model.tool.ToolResponseAttachment;
import uno.anahata.ai.swing.chat.ChatPanel;
import uno.anahata.ai.swing.media.util.AudioPlaybackPanel;

/**
 * A panel for rendering a list of {@link ToolResponseAttachment}s.
 * It uses a diff-based approach to avoid unnecessary re-renders.
 * 
 * @author anahata-ai
 */
public class ToolResponseAttachmentsPanel extends JPanel {

    private final ChatPanel chatPanel;
    private final Map<ToolResponseAttachment, JPanel> cachedPanels = new HashMap<>();
    /** Map to track playback stoppers for audio attachments. */
    private final Map<ToolResponseAttachment, Runnable> playbackStoppers = new HashMap<>();

    public ToolResponseAttachmentsPanel(@NonNull ChatPanel chatPanel) {
        this.chatPanel = chatPanel;
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        setOpaque(false);
    }

    /**
     * Updates the panel with the given list of attachments.
     * 
     * @param attachments The list of attachments to render.
     */
    public void render(List<ToolResponseAttachment> attachments) {
        // 1. Remove panels for attachments no longer present
        cachedPanels.keySet().removeIf(attachment -> {
            if (!attachments.contains(attachment)) {
                JPanel panel = cachedPanels.get(attachment);
                if (panel != null) {
                    remove(panel);
                }
                Runnable stopper = playbackStoppers.remove(attachment);
                if (stopper != null) stopper.run();
                return true;
            }
            return false;
        });

        // 2. Add or update panels for current attachments
        for (int i = 0; i < attachments.size(); i++) {
            ToolResponseAttachment attachment = attachments.get(i);
            JPanel panel = cachedPanels.get(attachment);
            if (panel == null) {
                panel = createAttachmentPanel(attachment);
                cachedPanels.put(attachment, panel);
            }
            
            if (i >= getComponentCount() || getComponent(i) != panel) {
                add(panel, i);
            }
        }

        // 3. Clean up trailing components
        while (getComponentCount() > attachments.size()) {
            remove(getComponentCount() - 1);
        }

        revalidate();
        repaint();
    }

    private JPanel createAttachmentPanel(ToolResponseAttachment attachment) {
        JPanel itemPanel = new JPanel(new BorderLayout(5, 5));
        itemPanel.setOpaque(false);
        itemPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

        String mimeType = attachment.getMimeType();
        byte[] data = attachment.getData();

        if (mimeType.startsWith("image/")) {
            itemPanel.add(MediaRenderer.createImageComponent(data, this), BorderLayout.CENTER);
        } else if (mimeType.startsWith("audio/")) {
            AudioPlaybackPanel audioPanel = chatPanel.getStatusPanel().getAudioPlaybackPanel();
            itemPanel.add(MediaRenderer.createAudioComponent(data, audioPanel, stopper -> {
                if (stopper == null) {
                    Runnable oldStopper = playbackStoppers.remove(attachment);
                    if (oldStopper != null) oldStopper.run();
                } else {
                    playbackStoppers.put(attachment, stopper);
                }
            }), BorderLayout.CENTER);
        } else {
            itemPanel.add(new JLabel("Attachment (" + mimeType + "): " + TextUtils.formatSize(data.length)), BorderLayout.CENTER);
        }
        return itemPanel;
    }

    @Override
    public void removeNotify() {
        playbackStoppers.values().forEach(Runnable::run);
        playbackStoppers.clear();
        super.removeNotify();
    }
}
