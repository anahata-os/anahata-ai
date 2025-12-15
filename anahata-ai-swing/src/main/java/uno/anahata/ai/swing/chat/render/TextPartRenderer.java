/*
 * Licensed under the Anahata Software License (ASL) v 108. See the LICENSE file for details. Força Barça!
 */
package uno.anahata.ai.swing.chat.render;

import java.awt.Component;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.swing.JComponent;
import lombok.Getter;
import uno.anahata.ai.model.core.ModelTextPart;
import uno.anahata.ai.model.core.TextPart;
import uno.anahata.ai.swing.chat.ChatPanel;

/**
 * Renders a {@link uno.anahata.ai.model.core.TextPart} into a list of JComponents,
 * handling markdown and code block rendering.
 *
 * @author anahata
 */
@Getter
public class TextPartRenderer extends AbstractPartRenderer<TextPart> {

    private static final Pattern CODE_BLOCK_PATTERN = Pattern.compile("```(\\w*)\\r?\\n([\\s\\S]*?)\\r?\\n```");

    private final List<AbstractTextSegmentRenderer> cachedSegmentRenderers = new ArrayList<>();

    /**
     * Constructs a new TextPartRenderer.
     *
     * @param chatPanel The chat panel instance.
     * @param part The TextPart to be rendered.
     */
    public TextPartRenderer(ChatPanel chatPanel, TextPart part) {
        super(chatPanel, part);
    }

    /**
     * Renders the content of the TextPart into a list of JComponents.
     * This method now performs a diffing operation to reuse and update existing components.
     *
     * @return A list of JComponents representing the content of the text part.
     */
    @Override
    protected List<JComponent> renderContentComponents() {
        String markdownText = part.getText();
        if (markdownText == null || markdownText.trim().isEmpty()) {
            cachedSegmentRenderers.clear(); // Clear all cached components if no text
            return new ArrayList<>();
        }

        boolean isThought = part instanceof ModelTextPart && ((ModelTextPart) part).isThought();
        List<TextSegmentDescriptor> newSegmentDescriptors = new ArrayList<>();

        Matcher matcher = CODE_BLOCK_PATTERN.matcher(markdownText);
        int lastEnd = 0;

        while (matcher.find()) {
            // Preceding text segment
            if (matcher.start() > lastEnd) {
                String textSegmentContent = markdownText.substring(lastEnd, matcher.start());
                newSegmentDescriptors.add(new TextSegmentDescriptor(TextSegmentType.TEXT, textSegmentContent, null));
            }

            // Code block segment
            String language = matcher.group(1);
            String code = matcher.group(2);
            newSegmentDescriptors.add(new TextSegmentDescriptor(TextSegmentType.CODE, code, language));

            lastEnd = matcher.end();
        }

        // Remaining text segment
        if (lastEnd < markdownText.length()) {
            String textSegmentContent = markdownText.substring(lastEnd);
            newSegmentDescriptors.add(new TextSegmentDescriptor(TextSegmentType.TEXT, textSegmentContent, null));
        }

        // Perform diffing and update cachedSegmentRenderers
        updateCachedSegmentRenderers(newSegmentDescriptors, isThought);

        // Return the components from the updated cachedSegmentRenderers
        List<JComponent> componentsToReturn = new ArrayList<>();
        for (AbstractTextSegmentRenderer segmentRenderer : cachedSegmentRenderers) {
            JComponent component = segmentRenderer.render();
            if (component != null) {
                component.setAlignmentX(Component.LEFT_ALIGNMENT);
                componentsToReturn.add(component);
            }
        }
        return componentsToReturn;
    }

    /**
     * Performs a diffing operation to update the {@code cachedSegmentRenderers} list.
     * It reuses existing renderers, creates new ones, and removes old ones as needed.
     *
     * @param newSegmentDescriptors The list of segment descriptors parsed from the current markdown text.
     * @param isThought True if the text represents a model thought, false otherwise.
     */
    private void updateCachedSegmentRenderers(List<TextSegmentDescriptor> newSegmentDescriptors, boolean isThought) {
        boolean needsFullRebuild = false;

        if (newSegmentDescriptors.size() != cachedSegmentRenderers.size()) {
            needsFullRebuild = true;
        } else {
            for (int i = 0; i < newSegmentDescriptors.size(); i++) {
                TextSegmentDescriptor newDescriptor = newSegmentDescriptors.get(i);
                AbstractTextSegmentRenderer cachedRenderer = cachedSegmentRenderers.get(i);

                // Check if the cached renderer matches the new descriptor
                if (!cachedRenderer.matches(newDescriptor)) {
                    needsFullRebuild = true;
                    break;
                }
            }
        }

        if (needsFullRebuild) {
            cachedSegmentRenderers.clear();
            for (TextSegmentDescriptor descriptor : newSegmentDescriptors) {
                cachedSegmentRenderers.add(descriptor.createRenderer(chatPanel, isThought));
            }
        } else {
            // Sizes and types/languages match, update content of existing renderers
            for (int i = 0; i < newSegmentDescriptors.size(); i++) {
                TextSegmentDescriptor newDescriptor = newSegmentDescriptors.get(i);
                AbstractTextSegmentRenderer cachedRenderer = cachedSegmentRenderers.get(i);
                cachedRenderer.updateContent(newDescriptor.content());
            }
        }
    }
}
