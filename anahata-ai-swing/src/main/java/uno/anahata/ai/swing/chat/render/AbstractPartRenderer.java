/*
 * Licensed under the Anahata Software License (ASL) v 108. See the LICENSE file for details. Força Barça!
 */
package uno.anahata.ai.swing.chat.render;

import java.awt.Color;
import java.awt.Container;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.List;
import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import lombok.Getter;
import net.miginfocom.swing.MigLayout;
import uno.anahata.ai.internal.TextUtils;
import uno.anahata.ai.model.core.AbstractPart;
import uno.anahata.ai.swing.chat.ChatPanel;
import uno.anahata.ai.swing.chat.SwingChatConfig;
import uno.anahata.ai.swing.icons.IconUtils;

/**
 * The abstract base class for all V2 Part Renderers.
 * <p>
 * This class is now a factory that produces a list of JComponents for a given
 * {@link AbstractPart}. It no longer extends {@link JPanel} or {@link ScrollablePanel}.
 * The control panel (header) is created here, and the content component is
 * provided by subclasses.
 *
 * @author anahata
 */
@Getter
public abstract class AbstractPartRenderer<T extends AbstractPart> {

    /**
     * The chat panel instance.
     */
    protected final ChatPanel chatPanel;
    /**
     * The part this renderer instance is tied to.
     */
    protected final T part;
    /**
     * The chat configuration.
     */
    protected final SwingChatConfig chatConfig;

    private final JPanel controlPanel; // Created once
    private final JLabel infoLabel; // Part of controlPanel, created once
    private final JButton pruneButton; // Part of controlPanel, created once
    private final JButton removeButton; // Part of controlPanel, created once

    private final List<JComponent> renderedContentComponents = new ArrayList<>(); // Stores content components for visibility management

    /**
     * Constructs a new AbstractPartRenderer.
     *
     * @param chatPanel The chat panel instance.
     * @param part The part to be rendered.
     */
    public AbstractPartRenderer(ChatPanel chatPanel, T part) {
        this.chatPanel = chatPanel;
        this.part = part;
        this.chatConfig = chatPanel.getChatConfig();

        // Initialize control panel and its components once
        this.controlPanel = new JPanel(new MigLayout("insets 0, fillx, gap 5", "[grow]push[][]", "[]"));
        this.controlPanel.setOpaque(false);
        this.controlPanel.setBorder(BorderFactory.createEmptyBorder(2, 0, 2, 0));

        this.infoLabel = new JLabel();
        this.infoLabel.setForeground(Color.GRAY);
        this.controlPanel.add(this.infoLabel, "growx");

        this.pruneButton = new JButton();
        this.pruneButton.setAction(new AbstractAction() {
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
                // Update the control panel and content components visibility
                updateControlPanel();
                updateContentComponentsVisibility();
                controlPanel.revalidate();
                controlPanel.repaint();
            }
        });
        this.pruneButton.setToolTipText("Toggle Pruning State (Auto/Pruned/Pinned)");
        this.controlPanel.add(this.pruneButton, "w 24!, h 24!");

        this.removeButton = new JButton();
        this.removeButton.setAction(new AbstractAction(null, IconUtils.getIcon("delete.png")) {
            @Override
            public void actionPerformed(ActionEvent e) {
                part.remove();
                // Remove this renderer's components from their parent
                removeFromParent();
            }
        });
        this.removeButton.setToolTipText("Remove Part");
        this.controlPanel.add(this.removeButton, "w 24!, h 24!");
    }

    /**
     * Renders the {@link AbstractPart} into a list of JComponents, including
     * the control panel (header) and the content component(s) provided by
     * subclasses.
     *
     * @return A list of JComponents representing the rendered part.
     */
    public final List<JComponent> render() {
        List<JComponent> components = new ArrayList<>();

        // 1. Update the state of the existing control panel components
        updateControlPanel();
        components.add(this.controlPanel);

        // 2. Create the content component(s) (implemented by subclass)
        // Clear previous content components and add new ones
        renderedContentComponents.clear();
        renderedContentComponents.addAll(renderContentComponents());

        if (!renderedContentComponents.isEmpty()) {
            // 3. Handle conditional visibility based on pruned state and config
            updateContentComponentsVisibility();
            components.addAll(renderedContentComponents);
        }

        return components;
    }

    /**
     * Subclasses must implement this method to create the specific content
     * component(s) for their part type.
     *
     * @return A list of JComponents representing the content of the part, or an empty list if no content.
     */
    protected abstract List<JComponent> renderContentComponents();

    /**
     * Updates the state of the control panel components (info label, prune button).
     * This method is called on every render to reflect the current state of the part.
     */
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

    /**
     * Updates the visibility of the content components based on the part's pruned state
     * and the chat configuration.
     */
    private void updateContentComponentsVisibility() {
        boolean isEffectivelyPruned = part.isEffectivelyPruned();
        boolean shouldShowContent = !isEffectivelyPruned || chatConfig.isShowPrunedParts();

        for (JComponent component : renderedContentComponents) {
            component.setVisible(shouldShowContent);
        }
    }

    /**
     * Removes all components produced by this renderer (the control panel and content components)
     * from their current parent container.
     */
    public void removeFromParent() {
        // The control panel is always the first component and should have a parent if rendered.
        if (controlPanel.getParent() != null) {
            Container parent = controlPanel.getParent();
            parent.remove(controlPanel);
            for (JComponent contentComponent : renderedContentComponents) {
                parent.remove(contentComponent);
            }
            parent.revalidate();
            parent.repaint();
        }
    }
}
