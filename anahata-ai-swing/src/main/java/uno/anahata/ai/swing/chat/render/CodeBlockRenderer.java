/*
 * Licensed under the Anahata Software License (ASL) v 108. See the LICENSE file for details. Força Barça!
 */
package uno.anahata.ai.swing.chat.render;

import java.awt.Font;
import java.util.logging.Level;
import javax.swing.JComponent;
import javax.swing.JEditorPane;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.text.EditorKit;
import lombok.extern.slf4j.Slf4j;
import uno.anahata.ai.swing.chat.ChatPanel;
import uno.anahata.ai.swing.chat.render.editorkit.EditorKitProvider;

/**
 * A utility class responsible for creating syntax-highlighted code block
 * components. This is the V2 implementation, which is a static utility class
 * that takes the ChatPanel to access the EditorKitProvider.
 *
 * @author pablo
 */
@Slf4j
public final class CodeBlockRenderer {

    private CodeBlockRenderer() {
    }

    /**
     * Creates a syntax-highlighted JEditorPane for the given code, wraps it in
     * a JScrollPane, and returns the component.
     *
     * @param language The programming language for syntax highlighting.
     * @param code The source code to render.
     * @param chatPanel The ChatPanel to get the EditorKitProvider from.
     * @return A JComponent (a JScrollPane containing the JEditorPane) ready to be displayed.
     */
    public static JComponent render(String language, String code, ChatPanel chatPanel) {
        EditorKitProvider editorKitProvider = chatPanel.getChatConfig().getEditorKitProvider();
        
        if (editorKitProvider == null) {
            log.warn("EditorKitProvider is null. Cannot render code block.");
            return createFallbackPane(code);
        }

        JEditorPane codeEditor = new JEditorPane();
        codeEditor.setEditable(false);

        try {
            EditorKit kit = editorKitProvider.getEditorKitForLanguage(language);
            if (kit == null) {
                log.warn("No EditorKit found for language '" + language + "'. Falling back to plain text for code: {}.", code);
                return createFallbackPane(code);
            }
            codeEditor.setEditorKit(kit);
            
            // FIX: Manually set the mimeType property on the document. This is the key
            // that the NetBeans editor infrastructure uses to find the correct
            // lexer and trigger the "second pass" of advanced syntax highlighting.
            codeEditor.getDocument().putProperty("mimeType", kit.getContentType());
            log.info("codeEditor.getDocument().putProperty(mimeType, {}); for {}'" + language + "'.", kit.getContentType());
            
            codeEditor.setText(code);
            
        } catch (Exception e) {
            log.warn("Failed to render code block for language '" + language + "'.", e);
            return createFallbackPane(code);
        }
        
        // Wrap the editor pane in a JScrollPane
        JScrollPane scrollPane = new JScrollPane(codeEditor);
        scrollPane.setPreferredSize(new java.awt.Dimension(0, 200)); // Default height
        scrollPane.setBorder(javax.swing.BorderFactory.createEmptyBorder());
        
        return scrollPane;
    }
    
    private static JComponent createFallbackPane(String code) {
        JTextArea fallbackEditor = new JTextArea(code, 10, 80);
        fallbackEditor.setEditable(false);
        fallbackEditor.setLineWrap(true);
        fallbackEditor.setWrapStyleWord(true);
        fallbackEditor.setBackground(new java.awt.Color(240, 240, 240));
        fallbackEditor.setFont(new java.awt.Font(Font.MONOSPACED, java.awt.Font.PLAIN, 12));        
        
        JScrollPane scrollPane = new JScrollPane(fallbackEditor);
        scrollPane.setPreferredSize(new java.awt.Dimension(0, 200)); // Default height
        scrollPane.setBorder(javax.swing.BorderFactory.createEmptyBorder());
        
        return scrollPane;
    }
}