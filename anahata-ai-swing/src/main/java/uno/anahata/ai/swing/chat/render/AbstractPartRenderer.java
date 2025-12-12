/*
 * Licensed under the Anahata Software License (ASL) v 108. See the LICENSE file for details. Força Barça!
 */
package uno.anahata.ai.swing.chat.render;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import lombok.Getter;
import net.miginfocom.swing.MigLayout;
import uno.anahata.ai.internal.TextUtils;
import uno.anahata.ai.model.core.AbstractPart;
import uno.anahata.ai.model.core.BlobPart;
import uno.anahata.ai.swing.chat.ChatPanel;
import uno.anahata.ai.swing.chat.SwingChatConfig;
import uno.anahata.ai.swing.components.ScrollablePanel;
import uno.anahata.ai.swing.icons.IconUtils;

/**
 * The abstract base class for all V2 Part Renderers.
 * <p>
 * This class is now a composite {@link ScrollablePanel} that manages the layout
 * of the content component (provided by subclasses) and the control panel
 * (implemented here). It also handles the conditional display of content based
 * on the part's pruned state.
 *
 * @author pablo
 */
@Getter
public abstract class AbstractPartRenderer extends ScrollablePanel {

    protected final ChatPanel chatPanel;
    protected final AbstractPart part; // The part this renderer instance is tied to
    protected final SwingChatConfig chatConfig;
    
    // The component provided by the subclass (e.g., JEditorPane, JLabel)
    protected JComponent contentComponent; 
    
    // Control Panel Components
    private final JPanel controlPanel;
    private final JLabel infoLabel;
    private final JButton pruneButton;
    private final JButton removeButton;

    public AbstractPartRenderer(ChatPanel chatPanel, AbstractPart part) {
        super(SwingConstants.VERTICAL); // Vertical scrolling by default
        setLayout(new BorderLayout());
        this.chatPanel = chatPanel;
        this.part = part;
        this.chatConfig = chatPanel.getChatConfig();
        
        // Initialize control panel components
        this.infoLabel = new JLabel();
        this.pruneButton = new JButton();
        this.removeButton = new JButton();
        
        // Initialize the control panel
        this.controlPanel = createControlPanel();
        
        // Add the control panel to the top
        add(controlPanel, BorderLayout.NORTH);
        
        // Initial render
        update();
    }

    /**
     * Renders or updates the internal content component based on the current state of
     * the {@link #part} field. This method is called by the constructor for
     * initial rendering and by the public {@code update()} method for subsequent
     * changes.
     * <p>
     * Subclasses must set the {@code contentComponent} field in this method.
     */
    protected abstract void updateContent();

    /**
     * Triggers a re-render of the component based on the current state of the
     * {@link #part} field. This is the method the parent {@code MessageRenderer}
     * will call when the part's content has changed.
     */
    public final void update() {
        // 1. Update the content component (implemented by subclass)
        updateContent();
        
        // 2. Update the control panel text and buttons
        updateControlPanel();
        
        // 3. Handle conditional visibility based on pruned state and config
        boolean isEffectivelyPruned = part.isEffectivelyPruned();
        boolean shouldShowContent = !isEffectivelyPruned || chatConfig.isShowPrunedParts();
        
        Component currentCenterComponent = ((BorderLayout) getLayout()).getLayoutComponent(BorderLayout.CENTER);
        
        // Only remove the center component if the visibility state is changing
        // or if the content component itself is changing.
        if (currentCenterComponent != null && (!shouldShowContent || currentCenterComponent != contentComponent)) {
            remove(currentCenterComponent);
            currentCenterComponent = null;
        }
        
        if (shouldShowContent && contentComponent != null && currentCenterComponent == null) {
            // Add the content component to the center if it should be shown and is not already there
            add(contentComponent, BorderLayout.CENTER);
        }
        
        // Revalidate and repaint the whole renderer
        revalidate();
        repaint();
    }
    
    private JPanel createControlPanel() {
        // Layout: [Part Type/Summary] [PUSH] [Prune Button] [Remove Button]
        JPanel panel = new JPanel(new MigLayout("insets 0, fillx, gap 5", "[grow]push[][]", "[]"));
        panel.setOpaque(false);
        panel.setBorder(BorderFactory.createEmptyBorder(2, 0, 2, 0));
        
        // 1. Part Info Label (Left-aligned)
        infoLabel.setForeground(Color.GRAY);
        panel.add(infoLabel, "growx");
        
        // 2. Prune Button (3-state)
        pruneButton.setAction(new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // Logic: null -> true (prune) -> false (pin) -> null (auto)
                Boolean currentPruned = part.getPruned();
                if (currentPruned == null) {
                    part.setPruned(Boolean.TRUE); // Prune
                } else if (currentPruned == Boolean.TRUE) {
                    part.setPruned(Boolean.FALSE); // Pin
                } else {
                    part.setPruned(null); // Auto
                }
                // Trigger a full UI update (will be handled by the parent MessageRenderer later)
                update(); 
            }
        });
        pruneButton.setToolTipText("Toggle Pruning State (Auto/Pruned/Pinned)");
        panel.add(pruneButton, "w 24!, h 24!");
        
        // 3. Remove Button (Conditional)
        if (part instanceof BlobPart) {
            removeButton.setAction(new AbstractAction(null, IconUtils.getIcon("delete.png")) {
                @Override
                public void actionPerformed(ActionEvent e) {
                    // This is the public method we made in AbstractPart.java
                    part.remove(); 
                    // The parent InputMessageRenderer will detect the removal and re-render
                }
            });
            removeButton.setToolTipText("Remove Attachment");
            panel.add(removeButton, "w 24!, h 24!");
        }
        
        return panel;
    }
    
    private void updateControlPanel() {
        // Update Info Label
        String partType = part.getClass().getSimpleName();
        String rawText = part.asText();
        String summary = TextUtils.formatValue(rawText);
        
        infoLabel.setText("<html><b>" + partType + "</b> <span style='color: #AAAAAA;'>" + summary + "</span></html>");
        
        // Update Prune Button Icon and Tooltip
        Boolean prunedState = part.getPruned();
        if (Boolean.TRUE.equals(prunedState)) {
            // Explicitly Pruned
            pruneButton.setIcon(IconUtils.getIcon("pruned.png")); // Assuming a 'pruned.png' icon exists
            pruneButton.setToolTipText("Status: Explicitly Pruned (Click to Pin)");
        } else if (Boolean.FALSE.equals(prunedState)) {
            // Explicitly Pinned
            pruneButton.setIcon(IconUtils.getIcon("pinned.png")); // Assuming a 'pinned.png' icon exists
            pruneButton.setToolTipText("Status: Explicitly Pinned (Click for Auto-Prune)");
        } else {
            // Auto-Prune (Default)
            pruneButton.setIcon(IconUtils.getIcon("auto_prune.png")); // Assuming an 'auto_prune.png' icon exists
            pruneButton.setToolTipText("Status: Auto-Prune (Click to Prune)");
        }
    }
}