/*
 * Licensed under the Anahata Software License (ASL) v 108. See the LICENSE file for details. Força Barça!
 */
package uno.anahata.ai.swing.chat.render;

import com.vladsch.flexmark.ext.tables.TablesExtension;
import com.vladsch.flexmark.html.HtmlRenderer;
import com.vladsch.flexmark.parser.Parser;
import com.vladsch.flexmark.util.ast.Node;
import com.vladsch.flexmark.util.data.MutableDataSet;
import java.awt.Component;
import java.util.Arrays;
import javax.swing.JComponent;
import javax.swing.text.html.HTMLEditorKit;
import javax.swing.text.html.StyleSheet;
import uno.anahata.ai.model.core.ModelTextPart;
import uno.anahata.ai.swing.chat.ChatPanel;
import uno.anahata.ai.swing.chat.SwingChatConfig.UITheme;
import uno.anahata.ai.swing.components.WrappingEditorPane;
import uno.anahata.ai.swing.internal.SwingUtils;

/**
 * Renders a markdown text segment into a {@link WrappingEditorPane}.
 * This class extends {@link AbstractTextSegmentRenderer} and handles the
 * conversion of markdown to HTML and applying appropriate styling.
 *
 * @author anahata
 */
public class MarkupTextSegmentRenderer extends AbstractTextSegmentRenderer {

    private final Parser markdownParser;
    private final HtmlRenderer htmlRenderer;
    private final boolean isThought;

    /**
     * Constructs a new MarkupTextSegmentRenderer.
     *
     * @param chatPanel The chat panel instance.
     * @param initialContent The initial markdown content for this segment.
     * @param isThought True if the text represents a model thought, false otherwise.
     */
    public MarkupTextSegmentRenderer(ChatPanel chatPanel, String initialContent, boolean isThought) {
        super(chatPanel, initialContent);
        MutableDataSet options = new MutableDataSet();
        options.set(Parser.EXTENSIONS, Arrays.asList(TablesExtension.create()));
        options.set(HtmlRenderer.SOFT_BREAK, "<br />");
        this.markdownParser = Parser.builder(options).build();
        this.htmlRenderer = HtmlRenderer.builder(options).build();
        this.isThought = isThought;
    }

    /**
     * Renders or updates the markdown text segment.
     * It reuses the existing {@link WrappingEditorPane} if available and updates its content
     * only if the markdown text has changed.
     *
     * @return The {@link WrappingEditorPane} representing the rendered markdown.
     */
    @Override
    public JComponent render() {
        if (component == null) {
            // Initial render: create the WrappingEditorPane
            WrappingEditorPane editorPane = new WrappingEditorPane();
            editorPane.setEditable(false);
            editorPane.setContentType("text/html");
            editorPane.setOpaque(false);

            HTMLEditorKit kit = new HTMLEditorKit();
            editorPane.setEditorKit(kit);

            // Apply custom CSS for styling and word wrapping
            StyleSheet sheet = kit.getStyleSheet();
            sheet.addRule("body { word-wrap: break-word; font-family: sans-serif; font-size: 14px; background-color: transparent;}");
            sheet.addRule("table { border-collapse: collapse; width: 100%; }");
            sheet.addRule("th, td { border: 1px solid #dddddd; text-align: left; padding: 8px; }");
            sheet.addRule("th { background-color: #f2f2f2; }");

            this.component = editorPane;
        }

        // Update content only if it has changed
        if (hasContentChanged()) {
            WrappingEditorPane editorPane = (WrappingEditorPane) this.component;
            UITheme theme = chatConfig.getTheme();
            Node document = markdownParser.parse(currentContent);
            String html = htmlRenderer.render(document);

            String fontStyle = isThought ? "font-style: italic; color: #888888;" : "color: " + SwingUtils.toHtmlColor(theme.getFontColor()) + ";";
            String fontWeight = isThought ? "font-weight: normal;" : "font-weight: normal;";

            StyleSheet sheet = ((HTMLEditorKit) editorPane.getEditorKit()).getStyleSheet();
            // This is still a hack. A proper solution would involve modifying the existing rule.
            // For now, adding a new rule will effectively override the previous one if properties are the same.
            sheet.addRule("body { " + fontStyle + fontWeight + "}");

            editorPane.setText("<html><body>" + html + "</body></html>");
            contentRendered(); // Mark content as rendered
        }

        return component;
    }

    /**
     * Determines if this renderer can handle the given segment descriptor.
     * A {@code MarkupTextSegmentRenderer} handles {@link TextSegmentType#TEXT} descriptors.
     *
     * @param descriptor The {@link TextSegmentDescriptor} to check.
     * @return True if the descriptor's type is {@link TextSegmentType#TEXT}, false otherwise.
     */
    @Override
    public boolean matches(TextSegmentDescriptor descriptor) {
        return descriptor.type() == TextSegmentType.TEXT;
    }
}
