package uno.anahata.ai.swing.components;

import java.awt.Dimension;
import java.awt.Rectangle;
import javax.swing.JEditorPane;
import javax.swing.Scrollable;

/**
 * A custom JEditorPane designed to correctly handle content wrapping,
 * particularly for HTML content, by ensuring it tracks the viewport width.
 * This is essential for preventing horizontal overflow of long, unbroken strings.
 *
 * @author anahata-gemini-pro-2.5
 */
public class WrappingEditorPane extends JEditorPane implements Scrollable {

    // No explicit constructor, relies on default JEditorPane constructor
    // Configuration (EditorKit, content type, etc.) is handled by the container (WrappingHtmlPane)

    @Override
    public boolean getScrollableTracksViewportWidth() {
        return true;
    }
    @Override
    public Dimension getPreferredScrollableViewportSize() {
        return getPreferredSize();
    }
    @Override
    public int getScrollableUnitIncrement(Rectangle visibleRect, int orientation, int direction) {
        return 16;
    }
    @Override
    public int getScrollableBlockIncrement(Rectangle visibleRect, int orientation, int direction) {
        return visibleRect.height;
    }
    @Override
    public boolean getScrollableTracksViewportHeight() {
        return false;
    }
}
