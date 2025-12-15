/*
 * Licensed under the Anahata Software License (ASL) v 108. See the LICENSE file for details. Força Barça!
 */
package uno.anahata.ai.swing.chat;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.io.File;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import javax.swing.*;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import org.jdesktop.swingx.JXTextArea;

import uno.anahata.ai.chat.Chat;
import uno.anahata.ai.model.core.InputUserMessage;
import uno.anahata.ai.swing.icons.IconUtils;
import uno.anahata.ai.swing.internal.AnyChangeDocumentListener;
import uno.anahata.ai.swing.internal.SwingTask;
import uno.anahata.ai.swing.internal.UICapture;
import uno.anahata.ai.swing.media.util.MicrophonePanel;

/**
 * A fully functional and responsive user input component for the V2 chat.
 * <p>
 * This panel manages a "live" {@link InputUserMessage} object that is updated
 * in real-time as the user types. It uses a {@link SwingTask} to send messages
 * asynchronously, ensuring the UI never freezes during API calls.
 *
 * @author pablo
 */
@Slf4j
@Getter
public class InputPanel extends JPanel { // Changed from JXTitledPanel

    private final Chat chat;
    private final ChatPanel chatPanel;

    // UI Components
    private JXTextArea inputTextArea;
    private JButton sendButton;
    private JButton attachButton;
    private JButton screenshotButton;
    private JButton captureFramesButton;
    private InputMessagePanel inputMessageRenderer;
    private JScrollPane previewScrollPane;
    private JSplitPane splitPane; // Added splitPane field
    private MicrophonePanel microphonePanel; // Instance of MicrophonePanel

    /**
     * The "live" message being composed by the user. This is the single source
     * of truth for the current input.
     */
    @Getter
    private InputUserMessage currentMessage;

    public InputPanel(ChatPanel chatPanel) {
        super(new BorderLayout(5, 5)); // Use JPanel constructor
        this.chatPanel = chatPanel;
        this.chat = chatPanel.getChat();
        initComponents();
    }

    private void initComponents() {
        setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5)); // Added border back

        inputTextArea = new JXTextArea("Type your message here (Ctrl+Enter to send)");
        inputTextArea.setLineWrap(true);
        inputTextArea.setWrapStyleWord(true);

        // --- REAL-TIME MODEL UPDATE ---
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
        // Set preferred height for the input area (approx. 4 lines)
        inputScrollPane.setPreferredSize(new Dimension(0, 80));

        // --- PREVIEW PANEL INTEGRATION ---
        this.currentMessage = new InputUserMessage(chat);
        this.inputMessageRenderer = new InputMessagePanel(chatPanel, currentMessage);

        previewScrollPane = new JScrollPane(inputMessageRenderer);
        // Set a fixed preferred and minimum height for the preview scroll pane
        previewScrollPane.setPreferredSize(new Dimension(0, 150)); // Example: 150 pixels height
        previewScrollPane.setMinimumSize(new Dimension(0, 100)); // Example: minimum 100 pixels height

        // --- HORIZONTAL SPLIT PANE ---
        splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, inputScrollPane, previewScrollPane);
        splitPane.setResizeWeight(0.5); // Give equal weight to both sides
        splitPane.setDividerLocation(0.5); // Initial split at 50% width

        add(splitPane, BorderLayout.CENTER);

        // Panel for buttons on the south side
        JPanel southButtonPanel = new JPanel(new BorderLayout(5, 0));

        // Panel for action buttons (mic, attach, etc.) on the west
        JPanel actionButtonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));

        // Instantiate MicrophonePanel and add it
        microphonePanel = new MicrophonePanel(this);
        actionButtonPanel.add(microphonePanel);

        attachButton = new JButton(IconUtils.getIcon("attach.png"));
        attachButton.setToolTipText("Attach Files");
        attachButton.addActionListener(e -> attachFiles());

        screenshotButton = new JButton(IconUtils.getIcon("desktop_screenshot.png"));
        screenshotButton.setToolTipText("Attach Desktop Screenshot");
        screenshotButton.addActionListener(e -> attachScreenshot());

        captureFramesButton = new JButton(IconUtils.getIcon("capture_frames.png"));
        captureFramesButton.setToolTipText("Attach Application Frames");
        captureFramesButton.addActionListener(e -> attachWindowCaptures());

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

    void attach(Path p) throws Exception {
        currentMessage.addAttachment(p);
    }

    private void attachFiles() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setMultiSelectionEnabled(true);
        int result = fileChooser.showOpenDialog(this);

        if (result == JFileChooser.APPROVE_OPTION) {
            File[] selectedFiles = fileChooser.getSelectedFiles();

            new SwingTask<>(
                    "Attach Files",
                    () -> {
                        // Collect Paths from selected Files
                        List<Path> paths = Arrays.stream(selectedFiles)
                                .map(File::toPath)
                                .collect(Collectors.toList());

                        // Use the new convenience method
                        currentMessage.addAttachments(paths);
                        return null;
                    },
                    (v) -> inputMessageRenderer.render() // Refresh preview
            ).execute();
        }
    }

    private void attachScreenshot() {
        new SwingTask<>(
                "Attach Screenshot",
                () -> {
                    // Get Paths from UICapture
                    List<Path> files = UICapture.screenshotAllScreenDevices();

                    // Use the new convenience method
                    currentMessage.addAttachments(files);
                    return null;
                },
                (v) -> inputMessageRenderer.render() // Refresh preview
        ).execute();
    }

    private void attachWindowCaptures() {
        new SwingTask<>(
                "Attach Application Frames",
                () -> {
                    // Get Paths from UICapture
                    List<Path> files = UICapture.screenshotAllJFrames();

                    // Use the new convenience method
                    currentMessage.addAttachments(files);
                    return null;
                },
                (v) -> inputMessageRenderer.render() // Refresh preview
        ).execute();
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
        new SwingTask<>(
                "Send Message",
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
                    // Error dialog handled by SwingTask
                    // Restore UI state
                    setButtonsEnabled(true);
                    inputTextArea.setText(textToRestoreOnError); // Restore the text

                    // Create a new renderer for the restored message and replace the old one
                    replaceRenderer(messageToSend);
                }
        ).execute();
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
     * Replaces the current InputMessageRenderer with a new one, updating the
     * JScrollPane.
     *
     * @param newMessage The new message model to use.
     */
    private void replaceRenderer(InputUserMessage newMessage) {
        // 1. Create the new renderer
        InputMessagePanel newRenderer = new InputMessagePanel(chatPanel, newMessage);

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
        attachButton.setEnabled(enabled);
        screenshotButton.setEnabled(enabled);
        captureFramesButton.setEnabled(enabled);
        microphonePanel.setMicrophoneComponentsEnabled(enabled);
    }
}
