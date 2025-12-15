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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects; // Added import
import javax.imageio.ImageIO;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import uno.anahata.ai.model.core.BlobPart;
import uno.anahata.ai.internal.TextUtils;
import uno.anahata.ai.swing.chat.ChatPanel;
import uno.anahata.ai.swing.internal.SwingUtils;

/**
 * Renders a {@link uno.anahata.ai.model.core.BlobPart} into a JComponent,
 * handling images, audio, and file information.
 *
 * @author anahata
 */
public class BlobPartRenderer extends AbstractPartRenderer<BlobPart> {

    private JPanel contentPanel; // Main panel for blob content
    private JLabel mainContentLabel; // Label for image or file name
    private JPanel infoPanel; // Panel for mimeType and size
    private JLabel mimeTypeLabel; // Label for MIME type
    private JLabel sizeLabel; // Label for size

    private byte[] lastRenderedData; // To track changes in blob data
    private String lastRenderedMimeType; // To track changes in mime type

    /**
     * Constructs a new BlobPartRenderer.
     *
     * @param chatPanel The chat panel instance.
     * @param part The BlobPart to be rendered.
     */
    public BlobPartRenderer(ChatPanel chatPanel, BlobPart part) {
        super(chatPanel, part);
    }

    /**
     * Renders the content of the BlobPart into a list of JComponents.
     * This method handles different MIME types (image, audio, other files).
     * It reuses existing components and updates their content only if the blob data or mime type has changed.
     *
     * @return A list of JComponents representing the content of the blob part.
     */
    @Override
    protected List<JComponent> renderContentComponents() {
        BlobPart blobPart = (BlobPart) part;
        String currentMimeType = blobPart.getMimeType();
        byte[] currentData = blobPart.getData();

        boolean contentChanged = !Arrays.equals(currentData, lastRenderedData) || !Objects.equals(currentMimeType, lastRenderedMimeType);

        if (contentPanel == null) {
            // Initial render: create all components
            contentPanel = new JPanel(new BorderLayout(10, 10));
            contentPanel.setOpaque(false);

            mainContentLabel = new JLabel();
            contentPanel.add(mainContentLabel, BorderLayout.CENTER);

            infoPanel = new JPanel();
            infoPanel.setLayout(new BoxLayout(infoPanel, BoxLayout.Y_AXIS));
            infoPanel.setOpaque(false);

            mimeTypeLabel = new JLabel();
            mimeTypeLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

            sizeLabel = new JLabel();
            sizeLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

            infoPanel.add(mimeTypeLabel);
            infoPanel.add(Box.createRigidArea(new Dimension(0, 5)));
            infoPanel.add(sizeLabel);

            contentPanel.add(infoPanel, BorderLayout.SOUTH);
        }

        if (contentChanged) {
            // Update content of existing components
            if (currentData == null || currentData.length == 0) {
                mainContentLabel.setText("Error: Blob data is empty.");
                mainContentLabel.setIcon(null);
            } else if (currentMimeType.startsWith("audio/")) {
                // TODO: Implement V2 AudioTool integration
                mainContentLabel.setText("Audio Part: " + currentMimeType);
                mainContentLabel.setIcon(null);
            } else if (currentMimeType.startsWith("image/")) {
                try {
                    BufferedImage originalImage = ImageIO.read(new ByteArrayInputStream(currentData));
                    if (originalImage != null) {
                        Image thumbnail = SwingUtils.createThumbnail(originalImage);
                        mainContentLabel.setIcon(new ImageIcon(thumbnail));
                        mainContentLabel.setText(null);
                        mainContentLabel.setBorder(BorderFactory.createLineBorder(Color.GRAY));
                    } else {
                        mainContentLabel.setText("Failed to load image.");
                        mainContentLabel.setIcon(null);
                    }
                } catch (IOException e) {
                    mainContentLabel.setText("Failed to render image: " + e.getMessage());
                    mainContentLabel.setIcon(null);
                }
            } else {
                // Default for other file types
                String fileName = blobPart.getSourcePath() != null ? blobPart.getSourcePath().getFileName().toString() : "Unknown File";
                mainContentLabel.setText("File: " + fileName);
                mainContentLabel.setIcon(null);
            }

            mimeTypeLabel.setText("MIME Type: " + currentMimeType);
            sizeLabel.setText("Size: " + TextUtils.formatSize(currentData != null ? currentData.length : 0));

            lastRenderedData = currentData;
            lastRenderedMimeType = currentMimeType;
        }

        List<JComponent> components = new ArrayList<>();
        components.add(contentPanel);
        return components;
    }
}
