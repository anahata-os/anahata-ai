/*
 * Licensed under the Anahata Software License (ASL) v 108. See the LICENSE file for details. Força Barça!
 */
package uno.anahata.ai.swing.chat.render;

import com.vladsch.flexmark.ext.tables.TablesExtension;
import com.vladsch.flexmark.html.HtmlRenderer;
import com.vladsch.flexmark.parser.Parser;
import com.vladsch.flexmark.util.ast.Node;
import com.vladsch.flexmark.util.data.MutableDataSet;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.util.Arrays;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.swing.BoxLayout;
import javax.swing.JComponent;
import javax.swing.JEditorPane;
import javax.swing.JPanel;
import javax.swing.text.html.HTMLEditorKit;
import javax.swing.text.html.StyleSheet;
import lombok.Getter;
import uno.anahata.ai.model.core.AbstractPart;
import uno.anahata.ai.model.core.ModelTextPart;
import uno.anahata.ai.model.core.TextPart;
import uno.anahata.ai.swing.chat.ChatPanel;
import uno.anahata.ai.swing.chat.SwingChatConfig.UITheme;
import uno.anahata.ai.swing.components.WrappingEditorPane;

/**
 * Renders a {@link uno.anahata.ai.model.core.TextPart} into a JComponent,
 * handling markdown and code block rendering.
 *
 * @author pablo
 */
@Getter
public class TextPartRenderer extends AbstractPartRenderer {

    private static final Pattern CODE_BLOCK_PATTERN = Pattern.compile("```(\\w*)\\r?\\n([\\s\\S]*?)\\r?\\n```");
    private final Parser markdownParser;
    private final HtmlRenderer htmlRenderer;
    
    private String lastRenderedText = ""; // State tracking for diffing

    public TextPartRenderer(ChatPanel chatPanel, AbstractPart part) {
        super(chatPanel, part); 
        if (!(part instanceof TextPart) && part != null) {
            throw new IllegalArgumentException("TextPartRenderer must be initialized with a TextPart or null.");
        }
        
        MutableDataSet options = new MutableDataSet();
        options.set(Parser.EXTENSIONS, Arrays.asList(TablesExtension.create()));
        options.set(HtmlRenderer.SOFT_BREAK, "<br />");
        this.markdownParser = Parser.builder(options).build();
        this.htmlRenderer = HtmlRenderer.builder(options).build();
    }
    
    @Override
    protected void updateContent() {
        String markdownText = ((TextPart) part).getText();
        
        // Only re-render the expensive markdown/HTML if the text has actually changed
        if (markdownText.equals(lastRenderedText)) {
            return;
        }
        
        lastRenderedText = markdownText;
        
        // The content panel will hold the rendered components (HTML/Code Blocks)
        JPanel contentPanel = new JPanel();
        contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));
        contentPanel.setOpaque(false);
        
        if (part == null) {
            this.contentComponent = contentPanel;
            return;
        }
        
        boolean isThought = part instanceof ModelTextPart && ((ModelTextPart) part).isThought();
        
        // If no code blocks are found, render the entire text as a single HTML pane
        if (!CODE_BLOCK_PATTERN.matcher(markdownText).find()) {
            contentPanel.add(createHtmlPane(markdownText, isThought));
            this.contentComponent = contentPanel;
            return;
        }

        // If code blocks are present, split the text and render segments individually
        Matcher matcher = CODE_BLOCK_PATTERN.matcher(markdownText);
        int lastEnd = 0;
        while (matcher.find()) {
            // Render preceding text as HTML
            if (matcher.start() > lastEnd) {
                String textSegment = markdownText.substring(lastEnd, matcher.start());
                contentPanel.add(createHtmlPane(textSegment, isThought));
            }
            
            // Render code block
            String language = matcher.group(1);
            String code = matcher.group(2);
            JComponent codeBlock = CodeBlockRenderer.render(language, code, chatPanel);
            codeBlock.setAlignmentX(Component.LEFT_ALIGNMENT);
            contentPanel.add(codeBlock);
            
            lastEnd = matcher.end();
        }

        // Render remaining text as HTML
        if (lastEnd < markdownText.length()) {
            String textSegment = markdownText.substring(lastEnd);
            contentPanel.add(createHtmlPane(textSegment, isThought));
        }
        
        this.contentComponent = contentPanel;
    }

    private JComponent createHtmlPane(String markdown, boolean isThought) {
        UITheme theme = chatPanel.getChatConfig().getTheme();
        
        // Parse markdown to HTML
        Node document = markdownParser.parse(markdown);
        String html = htmlRenderer.render(document);
        
        // Use the V2 WrappingEditorPane for correct line wrapping
        JEditorPane editorPane = new WrappingEditorPane(); 
        editorPane.setEditable(false);
        editorPane.setContentType("text/html");
        editorPane.setOpaque(false); 
        
        HTMLEditorKit kit = new HTMLEditorKit();
        editorPane.setEditorKit(kit);
        
        // Apply custom CSS for styling and word wrapping
        StyleSheet sheet = kit.getStyleSheet();
        
        String fontStyle = isThought ? "font-style: italic; color: #888888;" : "color: " + toHtmlColor(theme.getFontColor()) + ";";
        String fontWeight = isThought ? "font-weight: normal;" : "font-weight: normal;";
        
        sheet.addRule("body { word-wrap: break-word; font-family: sans-serif; font-size: 14px; background-color: transparent; " + fontStyle + fontWeight + "}");
        sheet.addRule("table { border-collapse: collapse; width: 100%; }");
        sheet.addRule("th, td { border: 1px solid #dddddd; text-align: left; padding: 8px; }");
        sheet.addRule("th { background-color: #f2f2f2; }");
        
        editorPane.setText("<html><body>" + html + "</body></html>");
        editorPane.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        // Wrap the editor pane in a JPanel with BorderLayout to force width constraint
        JPanel wrapper = new JPanel(new BorderLayout());
        wrapper.setOpaque(false);
        wrapper.add(editorPane, BorderLayout.CENTER);
        return wrapper;
    }
    
    private String toHtmlColor(Color color) {
        return String.format("#%02x%02x%02x", color.getRed(), color.getGreen(), color.getBlue());
    }
}