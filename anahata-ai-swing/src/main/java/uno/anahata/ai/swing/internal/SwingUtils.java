/*
 * Licensed under the Anahata Software License (ASL) v 108. See the LICENSE file for details. Força Barça!
 */
package uno.anahata.ai.swing.internal;

import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import lombok.experimental.UtilityClass;

/**
 * A collection of general-purpose Swing utility methods, primarily for image
 * manipulation and UI component creation.
 *
 * @author pablo
 */
@UtilityClass
public class SwingUtils {

    private static final int THUMBNAIL_MAX_SIZE = 250;

    /**
     * Creates a scaled thumbnail image from an original BufferedImage, maintaining
     * the aspect ratio and ensuring the largest dimension does not exceed
     * {@code THUMBNAIL_MAX_SIZE}.
     *
     * @param original The original image.
     * @return The scaled thumbnail image.
     */
    public static Image createThumbnail(BufferedImage original) {
        int width = original.getWidth();
        int height = original.getHeight();

        if (width <= THUMBNAIL_MAX_SIZE && height <= THUMBNAIL_MAX_SIZE) {
            return original;
        }

        double thumbRatio = (double) THUMBNAIL_MAX_SIZE / (double) Math.max(width, height);
        int newWidth = (int) (width * thumbRatio);
        int newHeight = (int) (height * thumbRatio);

        BufferedImage resized = new BufferedImage(newWidth, newHeight, original.getType() == 0 ? BufferedImage.TYPE_INT_ARGB : original.getType());
        Graphics2D g = resized.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g.drawImage(original, 0, 0, newWidth, newHeight, null);
        g.dispose();

        return resized;
    }
}