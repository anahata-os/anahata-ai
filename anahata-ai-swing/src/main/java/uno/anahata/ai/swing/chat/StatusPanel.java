/*
 * Licensed under the Anahata Software License (ASL) v 108. See the LICENSE file for details. Força Barça!
 */
package uno.anahata.ai.swing.chat;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridLayout;
import java.awt.RenderingHints;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JToggleButton;
import javax.swing.Timer;
import org.apache.commons.lang3.StringUtils;
import uno.anahata.ai.chat.Chat;
import uno.anahata.ai.internal.TimeUtils;
import uno.anahata.ai.model.core.Response;
import uno.anahata.ai.model.core.ResponseUsageMetadata;
import uno.anahata.ai.status.ApiErrorRecord;
import uno.anahata.ai.status.ChatStatus;
import uno.anahata.ai.status.StatusManager;
import uno.anahata.ai.swing.icons.IconUtils;

/**
 * A panel that displays the real-time status of the chat session, including
 * API call status, context usage, and error/retry information.
 *
 * @author anahata
 */
public class StatusPanel extends JPanel {
    private static final SimpleDateFormat TIME_FORMAT = new SimpleDateFormat("HH:mm:ss");
    private static final NumberFormat NUMBER_FORMAT = NumberFormat.getInstance();

    private final ChatPanel chatPanel;
    private final Timer refreshTimer;
    private ChatStatus lastStatus = null;

    // UI Components
    private StatusIndicator statusIndicator;
    private JLabel statusLabel;
    private ContextUsageBar contextUsageBar;
    private JPanel detailsPanel;
    private JLabel tokenDetailsLabel;
    private JToggleButton soundToggle;

    public StatusPanel(ChatPanel chatPanel) {
        super(new BorderLayout(10, 2));
        this.chatPanel = chatPanel;
        initComponents();
        
        this.refreshTimer = new Timer(1000, e -> refresh());
    }

    @Override
    public void addNotify() {
        super.addNotify();
        refreshTimer.start();
    }

    @Override
    public void removeNotify() {
        refreshTimer.stop();
        super.removeNotify();
    }

    private void initComponents() {
        setBorder(BorderFactory.createEmptyBorder(2, 5, 2, 5));

        JPanel topPanel = new JPanel(new BorderLayout(10, 0));
        JPanel statusDisplayPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));
        
        statusIndicator = new StatusIndicator();
        statusLabel = new JLabel("Initializing...");
        
        soundToggle = new JToggleButton(IconUtils.getIcon("bell.png"));
        soundToggle.setSelectedIcon(IconUtils.getIcon("bell_mute.png"));
        soundToggle.setToolTipText("Toggle Sound Notifications");
        // Use V2 ChatConfig for audio feedback setting
        soundToggle.setSelected(!chatPanel.getChatConfig().isAudioFeedbackEnabled());
        soundToggle.addActionListener(e -> chatPanel.getChatConfig().setAudioFeedbackEnabled(!soundToggle.isSelected()));

        statusDisplayPanel.add(soundToggle);
        statusDisplayPanel.add(statusIndicator);
        statusDisplayPanel.add(statusLabel);
        
        contextUsageBar = new ContextUsageBar(chatPanel); 
        
        topPanel.add(statusDisplayPanel, BorderLayout.WEST);
        topPanel.add(contextUsageBar, BorderLayout.EAST);
        
        detailsPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        detailsPanel.setVisible(false);
        
        tokenDetailsLabel = new JLabel();
        detailsPanel.add(tokenDetailsLabel);

        add(topPanel, BorderLayout.NORTH);
        add(detailsPanel, BorderLayout.CENTER);
    }

    public void refresh() {
        Chat chat = chatPanel.getChat();
        if (chat.isShutdown()) {
            if (refreshTimer.isRunning()) refreshTimer.stop();
            return;
        }
        
        StatusManager statusManager = chat.getStatusManager();
        ChatStatus currentStatus = statusManager.getCurrentStatus();
        long now = System.currentTimeMillis();
        Color statusColor = chatPanel.getChatConfig().getColor(currentStatus);

        // Play sound on status change (AudioPlayer needs to be ported/implemented)
        if (lastStatus != currentStatus && chatPanel.getChatConfig().isAudioFeedbackEnabled()) {
            // handleStatusSound(currentStatus); // Temporarily commented out
        }
        this.lastStatus = currentStatus;

        // 1. Update Status Indicator and Label
        statusIndicator.setColor(statusColor);
        statusLabel.setForeground(statusColor);
        statusLabel.setToolTipText(currentStatus.getDescription());
        
        String statusText = currentStatus.getDisplayName();
        if (currentStatus == ChatStatus.TOOL_EXECUTION_IN_PROGRESS && StringUtils.isNotBlank(statusManager.getExecutingToolName())) {
            statusText = String.format("%s (%s)", currentStatus.getDisplayName(), statusManager.getExecutingToolName());
        }
        
        if (currentStatus != ChatStatus.IDLE) {
            long duration = now - statusManager.getStatusChangeTime();
            statusLabel.setText(String.format("%s... (%s)", statusText, TimeUtils.formatMillisConcise(duration)));
        } else {
            long lastDuration = statusManager.getLastOperationDuration();
            if (lastDuration > 0) {
                statusLabel.setText(String.format("%s (took %s)", currentStatus.getDisplayName(), TimeUtils.formatMillisConcise(lastDuration)));
            } else {
                statusLabel.setText(currentStatus.getDisplayName());
            }
        }

        // 2. Refresh Context Usage Bar
        contextUsageBar.refresh();

        // 3. Update Details Panel
        List<ApiErrorRecord> errors = statusManager.getApiErrors();
        Response lastResponse = chat.getLastResponse().orElse(null);
        boolean isRetrying = !errors.isEmpty() && (currentStatus == ChatStatus.WAITING_WITH_BACKOFF || currentStatus == ChatStatus.API_CALL_IN_PROGRESS);

        if (isRetrying) {
            detailsPanel.removeAll();
            detailsPanel.setLayout(new GridLayout(0, 1)); // Vertical for errors
            ApiErrorRecord lastError = errors.get(errors.size() - 1);
            long totalErrorTime = now - lastError.getTimestamp().toEpochMilli();
            String headerText = String.format("Retrying... Total Time: %s | Attempt: %d | Next Backoff: %dms",
                                              TimeUtils.formatMillisConcise(totalErrorTime),
                                              lastError.getRetryAttempt() + 1,
                                              lastError.getBackoffAmount());
            detailsPanel.add(new JLabel(headerText));

            for (ApiErrorRecord error : errors) {
                
                String displayString = StringUtils.abbreviateMiddle(error.getException().toString(), " ... ", 108) ;
                
                String errorText = String.format("  • [%s] [..%s] %s",
                                                 TIME_FORMAT.format(error.getTimestamp().toEpochMilli()),
                                                 error.getApiKey(),
                                                 displayString);
                JLabel errorLabel = new JLabel(errorText);
                errorLabel.setForeground(Color.RED.darker());
                detailsPanel.add(errorLabel);
            }
            detailsPanel.setVisible(true);
            
        } else if (lastResponse != null) {
            detailsPanel.removeAll();
            detailsPanel.setLayout(new FlowLayout(FlowLayout.LEFT, 10, 0));
            
            // Display Prompt Feedback (Block Reason)
            lastResponse.getPromptFeedback().ifPresent(blockReason -> {
                JLabel reasonLabel = new JLabel("Prompt Blocked: " + blockReason);
                reasonLabel.setForeground(Color.RED.darker());
                detailsPanel.add(reasonLabel);
            });
            
            // Display Token Usage
            ResponseUsageMetadata usage = lastResponse.getUsageMetadata();
            if (usage != null) {
                String prompt = "Prompt: " + NUMBER_FORMAT.format(usage.getPromptTokenCount());
                String candidates = "Candidates: " + NUMBER_FORMAT.format(usage.getCandidatesTokenCount());
                String cached = "Cached: " + NUMBER_FORMAT.format(usage.getCachedContentTokenCount());
                String thoughts = "Thoughts: " + NUMBER_FORMAT.format(usage.getThoughtsTokenCount());

                tokenDetailsLabel.setText(String.join(" | ", prompt, candidates, cached, thoughts));
                detailsPanel.add(tokenDetailsLabel);
            }
            
            detailsPanel.setVisible(true);
            
        } else {
            detailsPanel.setVisible(false);
        }
        
        revalidate();
        repaint();
    }
    
    // private void handleStatusSound(ChatStatus newStatus) {
    //     String soundFileName = newStatus.name().toLowerCase() + ".wav";
    //     AudioPlayer.playSound(soundFileName);
    // }
    
    /**
     * A simple component that paints a colored circle.
     */
    private static class StatusIndicator extends JComponent {
        private Color color = Color.GRAY;

        public StatusIndicator() {
            setPreferredSize(new Dimension(16, 16));
        }

        public void setColor(Color color) {
            this.color = color;
            repaint();
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2d = (Graphics2D) g.create();
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2d.setColor(color);
            g2d.fillOval(2, 2, getWidth() - 4, getHeight() - 4);
            g2d.dispose();
        }
    }
}
