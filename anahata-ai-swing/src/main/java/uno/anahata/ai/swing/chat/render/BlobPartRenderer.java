/*
 * Licensed under the Anahata Software License (ASL) v 108. See the LICENSE file for details. Força Barça!
 */
package uno.anahata.ai.swing.chat.render;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import javax.imageio.ImageIO;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import uno.anahata.ai.model.core.AbstractPart;
import uno.anahata.ai.model.core.BlobPart;
import uno.anahata.ai.internal.TextUtils;
import uno.anahata.ai.swing.chat.ChatPanel;
import uno.anahata.ai.swing.internal.SwingUtils;

/**
 * Renders a {@link uno.anahata.ai.model.core.BlobPart} into a JComponent,
 * handling images, audio, and file information.
 *
 * @author pablo
 */
public class BlobPartRenderer extends AbstractPartRenderer {

    public BlobPartRenderer(ChatPanel chatPanel, AbstractPart part) {
        super(chatPanel, part);
        if (!(part instanceof BlobPart)) {
            throw new IllegalArgumentException("BlobPartRenderer must be initialized with a BlobPart.");
        }
    }

    @Override
    protected void updateContent() {
        BlobPart blobPart = (BlobPart) part;
        String mimeType = blobPart.getMimeType();
        byte[] data = blobPart.getData();
        
        // The content panel will hold the image/label and the info panel
        JPanel content = new JPanel(new BorderLayout(10, 10));
        content.setOpaque(false);
        
        if (data == null || data.length == 0) {
            content.add(new JLabel("Error: Blob data is empty."), BorderLayout.CENTER);
            this.contentComponent = content;
            return;
        }

        // Handle Audio Blobs
        if (mimeType.startsWith("audio/")) {
            // TODO: Implement V2 AudioTool integration
            content.add(new JLabel("Audio Part: " + mimeType), BorderLayout.CENTER);
        } 
        // Handle Image Blobs
        else if (mimeType.startsWith("image/")) {
            try {
                BufferedImage originalImage = ImageIO.read(new ByteArrayInputStream(data));
                if (originalImage != null) {
                    Image thumbnail = SwingUtils.createThumbnail(originalImage);
                    JLabel imageLabel = new JLabel(new ImageIcon(thumbnail));
                    imageLabel.setBorder(BorderFactory.createLineBorder(Color.GRAY));
                    content.add(imageLabel, BorderLayout.CENTER);
                }
            } catch (IOException e) {
                content.add(new JLabel("Failed to render image: " + e.getMessage()), BorderLayout.CENTER);
            }
        } else {
            // Default for other file types
            String fileName = blobPart.getSourcePath() != null ? blobPart.getSourcePath().getFileName().toString() : "Unknown File";
            content.add(new JLabel("File: " + fileName), BorderLayout.CENTER);
        }

        // Info Panel for mimeType and size (for all blob types)
        JPanel infoPanel = new JPanel();
        infoPanel.setLayout(new BoxLayout(infoPanel, BoxLayout.Y_AXIS));
        infoPanel.setOpaque(false);

        JLabel mimeTypeLabel = new JLabel("MIME Type: " + mimeType);
        mimeTypeLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel sizeLabel = new JLabel("Size: " + TextUtils.formatSize(data.length));
        sizeLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        infoPanel.add(mimeTypeLabel);
        infoPanel.add(Box.createRigidArea(new Dimension(0, 5)));
        infoPanel.add(sizeLabel);

        content.add(infoPanel, BorderLayout.SOUTH);
        
        // Set the content component for the base class
        this.contentComponent = content;
    }
}