package uno.anahata.ai.swing.provider;

import java.awt.BorderLayout;
import java.awt.Dimension;
import javax.swing.JTextArea;
import javax.swing.JToolTip;

/**
 * A custom JToolTip that uses a JTextArea internally to ensure that long,
 * unbroken lines of text (like a raw object's toString() output) are correctly
 * wrapped within a constrained width.
 *
 * @author anahata-gemini-pro-2.5
 */
public class WrappingToolTip extends JToolTip {

    private final JTextArea textArea = new JTextArea();

    public WrappingToolTip() {
        textArea.setEditable(false);
        textArea.setOpaque(false);
        textArea.setLineWrap(true);
        textArea.setWrapStyleWord(true);
        textArea.setFont(getFont()); // Inherit font from the tooltip
        
        setLayout(new BorderLayout());
        add(textArea, BorderLayout.CENTER);
    }

    @Override
    public void setTipText(String text) {
        textArea.setText(text);
        // Set a fixed number of columns to constrain the width
        textArea.setColumns(80); 
        
        // Force the JTextArea to calculate its preferred size based on the new columns
        // and then set the tooltip's size to match.
        Dimension preferredSize = textArea.getPreferredSize();
        preferredSize.width = Math.min(preferredSize.width, 800); // Cap max width
        setPreferredSize(preferredSize);
        
        // Call super to ensure the tooltip manager handles the text change
        super.setTipText(text);
    }
    
    @Override
    public Dimension getPreferredSize() {
        // Delegate preferred size calculation to the JTextArea
        return textArea.getPreferredSize();
    }
}
