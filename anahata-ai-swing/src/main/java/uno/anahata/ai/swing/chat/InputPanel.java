/*
 * Licensed under the Anahata Software License (ASL) v 108. See the LICENSE file for details. Força Barça!
 */
package uno.anahata.ai.swing.chat;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.io.File;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import javax.swing.*;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.jdesktop.swingx.JXTextArea;
import org.jdesktop.swingx.JXTitledPanel;
import uno.anahata.ai.chat.Chat;
import uno.anahata.ai.model.core.InputUserMessage;
import uno.anahata.ai.swing.icons.IconUtils;
import uno.anahata.ai.swing.icons.MicrophoneIcon;
import uno.anahata.ai.swing.icons.RecordingIcon;
import uno.anahata.ai.swing.internal.AnyChangeDocumentListener;
import uno.anahata.ai.swing.internal.SwingTask;
import uno.anahata.ai.swing.internal.UICapture;

/**
 * A fully functional and responsive user input component for the V2 chat.
 * <p>
 * This panel manages a "live" {@link InputUserMessage} object that is updated in
 * real-time as the user types. It uses a {@link SwingTask} to send messages
 * asynchronously, ensuring the UI never freezes during API calls.
 *
 * @author pablo
 */
@Slf4j
@Getter
public class InputPanel extends JXTitledPanel {

    private final Chat chat;
    private final ChatPanel chatPanel; // New field for ChatPanel

    // UI Components
    private JXTextArea inputTextArea;
    private JButton sendButton;
    private JToggleButton micButton;
    private JButton attachButton;
    private JButton screenshotButton;
    private JButton captureFramesButton;
    private InputMessageRenderer inputMessageRenderer; // Renamed Field
    private JScrollPane previewScrollPane; // New field to hold the scroll pane reference

    /**
     * The "live" message being composed by the user. This is the single source
     * of truth for the current input.
     */
    private InputUserMessage currentMessage;

    public InputPanel(ChatPanel chatPanel) { // Changed constructor argument
        super("User Input");
        setLayout(new BorderLayout(5, 5));
        this.chatPanel = chatPanel;
        this.chat = chatPanel.getChat(); // Get Chat from ChatPanel
        initComponents();
        // Initial message reset is now handled in initComponents
    }

    private void initComponents() {
        // setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5)); // Removed: JXTitledPanel manages its own border

        inputTextArea = new JXTextArea("Type your message here (Ctrl+Enter to send)");
        inputTextArea.setLineWrap(true);
        inputTextArea.setWrapStyleWord(true);

        // --- REAL-TIME MODEL UPDATE ---
        // Listen for changes in the text area and update the live message model instantly.
        inputTextArea.getDocument().addDocumentListener(new AnyChangeDocumentListener(this::updateMessageText));

        // Ctrl+Enter to send
        KeyStroke ctrlEnter = KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, KeyEvent.CTRL_DOWN_MASK);
        inputTextArea.getInputMap(JComponent.WHEN_FOCUSED).put(ctrlEnter, "sendMessage");
        inputTextArea.getActionMap().put("sendMessage", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                sendMessage();
            }
        });

        JScrollPane inputScrollPane = new JScrollPane(inputTextArea);
        
        // --- INITIAL PREVIEW PANEL INTEGRATION ---
        // Create initial message and renderer
        this.currentMessage = new InputUserMessage(chat);
        this.inputMessageRenderer = new InputMessageRenderer(chatPanel, currentMessage);
        
        // Store the scroll pane reference
        previewScrollPane = new JScrollPane(inputMessageRenderer);
        
        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, inputScrollPane, previewScrollPane);
        splitPane.setResizeWeight(0.5); // Equal split initially
        splitPane.setDividerLocation(0.5);
        
        add(splitPane, BorderLayout.CENTER); // Add the split pane instead of the input scroll pane

        // Panel for buttons on the south side
        JPanel southButtonPanel = new JPanel(new BorderLayout(5, 0));

        // Panel for action buttons (mic, attach, etc.) on the west
        JPanel actionButtonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));

        micButton = new JToggleButton(new MicrophoneIcon(24));
        micButton.setSelectedIcon(new RecordingIcon(24));
        micButton.setToolTipText("Click to start/stop recording");

        attachButton = new JButton(IconUtils.getIcon("attach.png"));
        attachButton.setToolTipText("Attach Files");
        attachButton.addActionListener(e -> attachFiles());

        screenshotButton = new JButton(IconUtils.getIcon("desktop_screenshot.png"));
        screenshotButton.setToolTipText("Attach Desktop Screenshot");
        screenshotButton.addActionListener(e -> attachScreenshot());

        captureFramesButton = new JButton(IconUtils.getIcon("capture_frames.png"));
        captureFramesButton.setToolTipText("Attach Application Frames");
        captureFramesButton.addActionListener(e -> attachWindowCaptures());

        actionButtonPanel.add(micButton);
        actionButtonPanel.add(attachButton);
        actionButtonPanel.add(screenshotButton);
        actionButtonPanel.add(captureFramesButton);

        sendButton = new JButton("Send");
        sendButton.addActionListener(e -> sendMessage());

        southButtonPanel.add(actionButtonPanel, BorderLayout.WEST);
        southButtonPanel.add(sendButton, BorderLayout.EAST);

        add(southButtonPanel, BorderLayout.SOUTH);
    }

    /**
     * Updates the underlying {@code currentMessage} model with the current text
     * from the input area and updates the preview panel.
     */
    private void updateMessageText() {
        currentMessage.setText(inputTextArea.getText());
        inputMessageRenderer.render();
    }
    
    private void attachFiles() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setMultiSelectionEnabled(true);
        int result = fileChooser.showOpenDialog(this);
        
        if (result == JFileChooser.APPROVE_OPTION) {
            File[] selectedFiles = fileChooser.getSelectedFiles();
            
            SwingTask.run(
                () -> {
                    // Collect Paths from selected Files
                    List<Path> paths = Arrays.stream(selectedFiles)
                        .map(File::toPath)
                        .collect(Collectors.toList());
                    
                    // FIX: Use the new convenience method
                    currentMessage.addAttachments(paths);
                    return null;
                },
                (v) -> {
                    // On Success (UI Thread)
                    inputMessageRenderer.render(); // Refresh preview
                },
                (error) -> {
                    // On Error (UI Thread)
                    log.error("Failed to attach files", error);
                    JOptionPane.showMessageDialog(this,
                                                  "Failed to attach files: " + error.getMessage(),
                                                  "Error",
                                                  JOptionPane.ERROR_MESSAGE);
                }
            );
        }
    }
    
    private void attachScreenshot() {
        SwingTask.run(
            () -> {
                // Get Paths from UICapture
                List<Path> files = UICapture.screenshotAllScreenDevices();
                
                // FIX: Use the new convenience method
                currentMessage.addAttachments(files);
                return null;
            },
            (v) -> {
                // On Success (UI Thread)
                inputMessageRenderer.render(); // Refresh preview
            },
            (error) -> {
                // On Error (UI Thread)
                log.error("Failed to capture screenshot", error);
                JOptionPane.showMessageDialog(this,
                                              "Failed to capture screenshot: " + error.getMessage(),
                                              "Error",
                                              JOptionPane.ERROR_MESSAGE);
            }
        );
    }
    
    private void attachWindowCaptures() {
        SwingTask.run(
            () -> {
                // Get Paths from UICapture
                List<Path> files = UICapture.screenshotAllJFrames();
                
                // FIX: Use the new convenience method
                currentMessage.addAttachments(files);
                return null;
            },
            (v) -> {
                // On Success (UI Thread)
                inputMessageRenderer.render(); // Refresh preview
            },
            (error) -> {
                // On Error (UI Thread)
                log.error("Failed to capture application frames", error);
                JOptionPane.showMessageDialog(this,
                                              "Failed to capture application frames: " + error.getMessage(),
                                              "Error",
                                              JOptionPane.ERROR_MESSAGE);
            }
        );
    }

    /**
     * Sends the {@code currentMessage} to the chat asynchronously.
     */
    private void sendMessage() {
        if (currentMessage.isEmpty()) {
            return; // Don't send empty messages
        }

        final InputUserMessage messageToSend = this.currentMessage;
        final String textToRestoreOnError = inputTextArea.getText(); // Keep a copy
        
        // --- UX IMPROVEMENT ---
        // Clear the input immediately and create a new message for the next turn.
        resetMessage();
        // --------------------

        setButtonsEnabled(false);

        // Use the generic SwingTask to run the blocking chat method in the background.
        SwingTask.run(
            () -> {
                chat.sendMessage(messageToSend);
                return null; // Return Void
            },
            (result) -> {
                // On Success (UI Thread)
                log.info("Message sent successfully.");
                setButtonsEnabled(true);
                inputTextArea.requestFocusInWindow();
            },
            (error) -> {
                // On Error (UI Thread)
                log.error("Failed to send message", error);
                JOptionPane.showMessageDialog(this,
                                              "An error occurred: " + error.getMessage(),
                                              "Error",
                                              JOptionPane.ERROR_MESSAGE);
                // Restore UI state
                setButtonsEnabled(true);
                inputTextArea.setText(textToRestoreOnError); // Restore the text
                
                // FIX: Create a new renderer for the restored message and replace the old one
                replaceRenderer(messageToSend);
            }
        );
    }
    
    /**
     * Resets the input field and creates a new, empty {@link InputUserMessage}
     * for the next turn.
     */
    private void resetMessage() {
        inputTextArea.setText("");
        replaceRenderer(new InputUserMessage(chat));
    }
    
    /**
     * Replaces the current InputMessageRenderer with a new one, updating the JScrollPane.
     * @param newMessage The new message model to use.
     */
    private void replaceRenderer(InputUserMessage newMessage) {
        // 1. Create the new renderer
        InputMessageRenderer newRenderer = new InputMessageRenderer(chatPanel, newMessage);
        
        // 2. Replace the viewport view of the existing scroll pane
        previewScrollPane.setViewportView(newRenderer);
        
        // 3. Update internal fields
        this.currentMessage = newMessage;
        this.inputMessageRenderer = newRenderer;
        
        // 4. Revalidate and repaint the scroll pane
        previewScrollPane.revalidate();
        previewScrollPane.repaint();
    }

    private void setButtonsEnabled(boolean enabled) {
        sendButton.setEnabled(enabled);
        micButton.setEnabled(enabled);
        attachButton.setEnabled(enabled);
        screenshotButton.setEnabled(enabled);
        captureFramesButton.setEnabled(enabled);
    }
}