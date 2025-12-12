/*
 * Licensed under the Anahata Software License (ASL) v 108. See the LICENSE file for details. Força Barça!
 */
package uno.anahata.ai.swing.chat;

import java.awt.BorderLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.SwingConstants;
import lombok.Getter;
import uno.anahata.ai.model.core.BlobPart;
import uno.anahata.ai.model.core.InputUserMessage;
import uno.anahata.ai.swing.chat.render.BlobPartRenderer;
import uno.anahata.ai.swing.chat.render.TextPartRenderer;

/**
 * A panel that provides a live, read-only preview of the user's input message,
 * including markdown rendering of the text part and a display of any attached
 * blob parts.
 * <p>
 * This component is a standard JPanel that is updated by the parent InputPanel.
 *
 * @author pablo
 */
@Getter
public class InputMessageRenderer extends JPanel {

    private final ChatPanel chatPanel;
    private final InputUserMessage message; // The mutable message instance
    private final JPanel contentPanel; // The panel that holds the rendered parts
    
    // Cached renderers for diff-based updates
    private final TextPartRenderer textRenderer;
    private final Map<BlobPart, BlobPartRenderer> blobRenderers;
    
    // State tracking for diffing (now only for blobs, text is self-managed)

    public InputMessageRenderer(ChatPanel chatPanel, InputUserMessage message) {
        super(new BorderLayout());
        this.chatPanel = chatPanel;
        this.message = message;
        
        // Initialize cached renderers
        this.textRenderer = new TextPartRenderer(chatPanel, message.getEditableTextPart());
        this.blobRenderers = new HashMap<>();
        
        // The content panel uses GridBagLayout to stack the rendered parts vertically
        // It is a regular JPanel, as the parent InputPanel wraps this entire component in a JScrollPane.
        contentPanel = new JPanel(new GridBagLayout());
        contentPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        // Add the content panel directly to this renderer
        add(contentPanel, BorderLayout.CENTER);
        
        // Initial render
        render();
    }
    
    /**
     * Updates the preview panel with the content of the live message.
     */
    public void render() {
        // 1. Update Text Part (self-managed diff)
        textRenderer.update();
        
        // 2. Diff and Update Blob Parts
        List<BlobPart> currentBlobs = message.getAttachments();
        
        // Identify removed blobs
        List<BlobPart> removedBlobs = blobRenderers.keySet().stream()
            .filter(blob -> !currentBlobs.contains(blob))
            .collect(Collectors.toList());
        
        // Remove renderers for deleted blobs
        removedBlobs.forEach(blobRenderers::remove);
        
        // Identify new blobs and create renderers
        currentBlobs.stream()
            .filter(blob -> !blobRenderers.containsKey(blob))
            .forEach(blob -> blobRenderers.put(blob, new BlobPartRenderer(chatPanel, blob)));
        
        // 3. Rebuild Content Panel (Order matters)
        contentPanel.removeAll();
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridwidth = GridBagConstraints.REMAINDER;
        gbc.weightx = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.NORTHWEST;
        
        boolean firstPart = true;
        String currentText = message.getText();

        // Add Text Part (always first)
        if (currentText != null && !currentText.trim().isEmpty()) {
            gbc.insets = new Insets(0, 0, 0, 0);
            contentPanel.add(textRenderer, gbc); // TextRenderer is now a ScrollablePanel
            firstPart = false;
        }
        
        // Add Blob Parts in the order they appear in the message
        for (BlobPart blobPart : currentBlobs) {
            BlobPartRenderer renderer = blobRenderers.get(blobPart);
            if (renderer != null) {
                // Update the renderer's content and controls
                renderer.update(); 
                
                if (!firstPart) {
                    // Add a separator before the blob part if it's not the first component
                    gbc.insets = new Insets(10, 0, 10, 0);
                    JSeparator separator = new JSeparator(SwingConstants.HORIZONTAL);
                    contentPanel.add(separator, gbc);
                }
                
                gbc.insets = new Insets(0, 0, 0, 0);
                contentPanel.add(renderer, gbc); // BlobRenderer is now a ScrollablePanel
                firstPart = false;
            }
        }
        
        // 4. Add vertical glue to push content to the top
        gbc.weighty = 1;
        contentPanel.add(Box.createVerticalGlue(), gbc);
        
        contentPanel.revalidate();
        contentPanel.repaint();
    }
}