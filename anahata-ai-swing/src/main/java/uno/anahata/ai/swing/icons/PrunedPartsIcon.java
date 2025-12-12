package uno.anahata.ai.swing.icons;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.Rectangle2D;
import javax.swing.Icon;

/**
 * A simple, programmatically drawn Icon representing a "pruned" or "hidden" part.
 * It is drawn as a dashed-line box to indicate content that is still present
 * but not currently visible.
 */
public class PrunedPartsIcon implements Icon {

    private final int size;
    private static final float[] DASH_PATTERN = {2.0f, 2.0f};

    public PrunedPartsIcon(int size) {
        this.size = size;
    }

    @Override
    public void paintIcon(Component c, Graphics g, int x, int y) {
        Graphics2D g2d = (Graphics2D) g.create();
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setColor(c.isEnabled() ? Color.GRAY : Color.LIGHT_GRAY);

        // Calculate bounds for the box
        int padding = size / 8;
        int boxSize = size - (2 * padding);
        int boxX = x + padding;
        int boxY = y + padding;

        // Set dashed stroke
        g2d.setStroke(new BasicStroke(1.5f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 10.0f, DASH_PATTERN, 0.0f));

        // Draw the dashed box
        g2d.draw(new Rectangle2D.Double(boxX, boxY, boxSize, boxSize));

        // Draw a small 'X' or line to emphasize the hidden state
        g2d.setStroke(new BasicStroke(1.5f));
        g2d.drawLine(boxX + boxSize / 4, boxY + boxSize / 4, boxX + 3 * boxSize / 4, boxY + 3 * boxSize / 4);
        g2d.drawLine(boxX + 3 * boxSize / 4, boxY + boxSize / 4, boxX + boxSize / 4, boxY + 3 * boxSize / 4);

        g2d.dispose();
    }

    @Override
    public int getIconWidth() {
        return size;
    }

    @Override
    public int getIconHeight() {
        return size;
    }
}