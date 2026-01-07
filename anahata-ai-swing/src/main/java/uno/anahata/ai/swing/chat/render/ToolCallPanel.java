/*
 * Licensed under the Anahata Software License (ASL) v 108. See the LICENSE file for details. Força Barça!
 */
package uno.anahata.ai.swing.chat.render;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.event.MouseWheelEvent;
import java.util.List;
import java.util.Map;
import javax.swing.BorderFactory;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import lombok.NonNull;
import net.miginfocom.swing.MigLayout;
import uno.anahata.ai.internal.JacksonUtils;
import uno.anahata.ai.model.core.AbstractPart;
import uno.anahata.ai.model.tool.AbstractTool;
import uno.anahata.ai.model.tool.AbstractToolCall;
import uno.anahata.ai.model.tool.AbstractToolParameter;
import uno.anahata.ai.model.tool.AbstractToolResponse;
import uno.anahata.ai.model.tool.ToolExecutionStatus;
import uno.anahata.ai.model.tool.ToolPermission;
import uno.anahata.ai.swing.chat.ChatPanel;
import uno.anahata.ai.swing.components.CodeHyperlink;
import uno.anahata.ai.swing.icons.RunIcon;
import uno.anahata.ai.swing.internal.AnyChangeDocumentListener;
import uno.anahata.ai.swing.internal.EdtPropertyChangeListener;
import uno.anahata.ai.swing.internal.SwingUtils;

/**
 * A specialized panel for rendering an {@link AbstractToolCall} and its associated
 * {@link AbstractToolResponse} within a model message. It provides a split-pane
 * view for arguments and results, along with interactive controls for managing
 * permissions, execution status, and user feedback.
 * 
 * @author anahata-ai
 */
public class ToolCallPanel extends AbstractPartPanel<AbstractToolCall<?, ?>> {

    private JSplitPane splitPane;
    private JPanel argsPanel;
    
    private JTabbedPane resultsTabbedPane;
    private JTextArea outputArea;
    private JTextArea errorArea;
    private JTextArea logsArea;
    
    private JTextField feedbackField;
    private CodeHyperlink jsonLink;
    
    private JComboBox<ToolPermission> permissionCombo;
    private JComboBox<ToolExecutionStatus> statusCombo;
    private JButton runButton;

    public ToolCallPanel(@NonNull ChatPanel chatPanel, @NonNull AbstractToolCall<?, ?> part) {
        super(chatPanel, part);
        // Listen to both the call and its response for state changes
        new EdtPropertyChangeListener(this, part, null, evt -> render());
        new EdtPropertyChangeListener(this, part.getResponse(), null, evt -> render());
    }

    @Override
    protected void renderContent() {
        if (splitPane == null) {
            initComponents();
        }
        
        AbstractToolCall<?, ?> call = getPart();
        AbstractToolResponse<?> response = call.getResponse();

        // 1. Update Arguments (Left)
        renderArguments(call);

        // 2. Update Results/Logs/Errors (Right)
        renderResults(response);

        // 3. Update Controls (Bottom)
        updateControls(call, response);
        
        updateHeaderInfoText();
    }

    private void initComponents() {
        getCentralContainer().setLayout(new MigLayout("fill, insets 0", "[grow]", "[grow][grow][]"));

        // --- Split Pane ---
        argsPanel = new JPanel(new MigLayout("fillx, insets 5", "[][grow]"));
        argsPanel.setOpaque(false);
        
        resultsTabbedPane = new JTabbedPane();
        
        outputArea = createTextArea(chatConfig.getTheme().getToolOutputFg(), null);
        errorArea = createTextArea(chatConfig.getTheme().getToolErrorFg(), chatConfig.getTheme().getToolErrorBg());
        logsArea = createTextArea(chatConfig.getTheme().getToolLogsFg(), chatConfig.getTheme().getToolLogsBg());

        JScrollPane outputScrollPane = new JScrollPane(outputArea);
        outputScrollPane.addMouseWheelListener(e -> SwingUtils.redispatchMouseWheelEvent(outputScrollPane, e));
        resultsTabbedPane.addTab("Output", outputScrollPane);

        JScrollPane errorScrollPane = new JScrollPane(errorArea);
        errorScrollPane.addMouseWheelListener(e -> SwingUtils.redispatchMouseWheelEvent(errorScrollPane, e));
        resultsTabbedPane.addTab("Error", errorScrollPane);

        JScrollPane logsScrollPane = new JScrollPane(logsArea);
        logsScrollPane.addMouseWheelListener(e -> SwingUtils.redispatchMouseWheelEvent(logsScrollPane, e));
        resultsTabbedPane.addTab("Logs", logsScrollPane);

        // Directly add argsPanel to the split pane, no JScrollPane wrapper
        splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, argsPanel, resultsTabbedPane);
        splitPane.setResizeWeight(0.5); // Give equal weight to both sides initially
        splitPane.setOpaque(false);
        splitPane.setBorder(null);
        splitPane.setOneTouchExpandable(true); // Enable one-touch expandable buttons
        
        getCentralContainer().add(splitPane, "grow, wrap");

        // --- Bottom Control Bar ---
        JPanel controlBar = new JPanel(new MigLayout("fillx, insets 5", "[][grow][][grow][]"));
        controlBar.setOpaque(false);
        controlBar.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, Color.LIGHT_GRAY));

        // Feedback Row
        controlBar.add(new JLabel("Feedback:"), "split 2");
        feedbackField = new JTextField();
        feedbackField.getDocument().addDocumentListener(new AnyChangeDocumentListener(() -> {
            getPart().getResponse().setUserFeedback(feedbackField.getText());
        }));
        controlBar.add(feedbackField, "growx, wrap");

        // Permission & Status Row
        permissionCombo = new JComboBox<>(new ToolPermission[]{
            ToolPermission.APPROVE, ToolPermission.APPROVE_ALWAYS, ToolPermission.DENY_NEVER
        });
        permissionCombo.setRenderer(new ToolPermissionRenderer());
        permissionCombo.addActionListener(e -> {
            getPart().getTool().setPermission((ToolPermission) permissionCombo.getSelectedItem());
        });

        statusCombo = new JComboBox<>(ToolExecutionStatus.values());
        statusCombo.setRenderer(new ToolExecutionStatusRenderer());
        statusCombo.addActionListener(e -> {
            getPart().getResponse().setStatus((ToolExecutionStatus) statusCombo.getSelectedItem());
        });

        runButton = new JButton("Run", new RunIcon(16));
        runButton.addActionListener(e -> getPart().getResponse().execute());

        jsonLink = new CodeHyperlink("json", 
                () -> "Tool Response: " + getPart().getToolName(), 
                () -> JacksonUtils.prettyPrint(getPart().getResponse()), 
                "json");

        controlBar.add(new JLabel("Permission:"), "split 2");
        controlBar.add(permissionCombo);
        controlBar.add(new JLabel("Status:"), "split 2");
        controlBar.add(statusCombo);
        controlBar.add(runButton, "split 2");
        controlBar.add(jsonLink, "right");

        getCentralContainer().add(controlBar, "growx");
    }

    private void renderArguments(AbstractToolCall<?, ?> call) {
        argsPanel.removeAll();
        AbstractTool<?, ?> tool = call.getTool();
        Map<String, Object> args = call.getArgs();
        
        int row = 0; // Keep track of the current row for MigLayout
        for (Map.Entry<String, Object> entry : args.entrySet()) {
            String paramName = entry.getKey();
            Object value = entry.getValue();
            
            String rendererId = tool.getParameters().stream()
                    .filter(p -> p.getName().equals(paramName))
                    .map(AbstractToolParameter::getRendererId)
                    .findFirst()
                    .orElse("");

            String valStr = (value instanceof String s) ? s : JacksonUtils.prettyPrint(value);

            // Determine if the argument is 'large' and should have its name above the value
            boolean isLargeArgument = (rendererId != null && !rendererId.isEmpty()) || 
                                      (valStr.length() > 100 || valStr.contains("\n"));

            if (isLargeArgument) {
                // Large argument: Name above value, spanning both columns, then wrap
                argsPanel.add(new JLabel("<html><b>" + paramName + ":</b></html>"), "cell 0 " + row + ", span 2, wrap");
                row++; // Increment row for the value
            } else {
                // Short argument: Name beside value
                argsPanel.add(new JLabel("<html><b>" + paramName + ":</b></html>"), "cell 0 " + row); // Name in first column
            }

            if (rendererId != null && !rendererId.isEmpty()) {
                CodeBlockSegmentRenderer renderer = new CodeBlockSegmentRenderer(chatPanel, valStr, rendererId);
                renderer.render();
                // For large arguments, the name is already on its own row, so the value starts a new row. (cell 1 row is correct)
                // For short arguments, the value is in the second column of the current row. (cell 1 row is correct)
                argsPanel.add(renderer.getComponent(), "cell 1 " + row + ", growx, hmin 40, wrap"); 
            } else if (valStr.length() > 100 || valStr.contains("\n")) {
                JTextArea area = new JTextArea();
                area.setEditable(false);
                area.setLineWrap(true);
                area.setWrapStyleWord(true);
                area.setText(valStr);
                // For large arguments, the name is already on its own row, so the value starts a new row. (cell 1 row is correct)
                // For short arguments, the value is in the second column of the current row. (cell 1 row is correct)
                argsPanel.add(new JScrollPane(area), "cell 1 " + row + ", growx, hmin 40, wrap");
            } else {
                // Short argument value: in the second column, then wrap
                argsPanel.add(new JLabel(valStr), "cell 1 " + row + ", wrap"); 
            }
            row++; // Increment row for the next argument
        }
        argsPanel.revalidate();
        argsPanel.repaint(); // Ensure repaint after revalidate
    }

    private void renderResults(AbstractToolResponse<?> response) {
        // 1. Output
        if (response.getStatus() == ToolExecutionStatus.EXECUTED) {
            outputArea.setText(response.getResult() != null ? response.getResult().toString() : "null");
            resultsTabbedPane.setSelectedIndex(0);
        } else {
            outputArea.setText(response.getStatus() == ToolExecutionStatus.PENDING ? "Pending execution..." : "Not executed.");
        }

        // 2. Error
        errorArea.setText(response.getError() != null ? response.getError() : "");
        
        // 3. Logs
        StringBuilder logsBuilder = new StringBuilder();
        for (String log : response.getLogs()) {
            logsBuilder.append("• ").append(log).append("\n");
        }
        logsArea.setText(logsBuilder.toString());
        
        // Reactive Tab Selection
        if (response.getStatus() == ToolExecutionStatus.FAILED) {
            resultsTabbedPane.setSelectedIndex(1); // Select Error tab
        } else if (response.getStatus() == ToolExecutionStatus.EXECUTED) {
            if (logsArea.getText().isEmpty()) {
                resultsTabbedPane.setSelectedIndex(0); // Select Output tab
            } else {
                resultsTabbedPane.setSelectedIndex(2);
            }
        }
    }

    private void updateControls(AbstractToolCall<?, ?> call, AbstractToolResponse<?> response) {
        permissionCombo.setSelectedItem(call.getTool().getPermission());
        statusCombo.setSelectedItem(response.getStatus());
        
        if (!feedbackField.getText().equals(response.getUserFeedback())) {
            feedbackField.setText(response.getUserFeedback());
        }
        
        if (response.getStatus() == ToolExecutionStatus.EXECUTED) {
            runButton.setText("Run Again");
            runButton.setEnabled(true);
        } else if (response.getStatus() == ToolExecutionStatus.PENDING || response.getStatus() == ToolExecutionStatus.NOT_EXECUTED) {
            runButton.setText("Run");
            runButton.setEnabled(true);
        } else if (response.getStatus() == ToolExecutionStatus.FAILED) {
            runButton.setText("Retry");
            runButton.setEnabled(true);
        } else {
            runButton.setText("Executed");
            runButton.setEnabled(false);
        }
    }

    private JTextArea createTextArea(Color fg, Color bg) {
        JTextArea area = new JTextArea();
        area.setEditable(false);
        area.setLineWrap(true);
        area.setWrapStyleWord(true);
        area.setForeground(fg);
        if (bg != null) {
            area.setBackground(bg);
            area.setOpaque(true);
        } else {
            area.setOpaque(false);
        }
        area.setFont(chatConfig.getTheme().getMonoFont());
        return area;
    }

    @Override
    protected void updateHeaderInfoText() {
        AbstractToolCall<?, ?> call = getPart();
        AbstractToolResponse<?> response = call.getResponse();
        
        StringBuilder sb = new StringBuilder("<html>");
        sb.append("<b>Tool: </b>").append(call.getToolName());
        
        String statusText = response.getStatus() != null ? response.getStatus().name() : "PENDING";
        String color = chatConfig.getColor(response.getStatus());
        
        sb.append(" <font color='").append(color).append("'>[").append(statusText).append("]</font>");

        Long executionTime = response.getExecutionTimeMillis();
        if (executionTime != null && executionTime > 0) {
            sb.append(" (").append(executionTime).append(" ms)");
        }
        
        sb.append("</html>");
        
        setTitle(sb.toString());
    }

    private String getStatusColor(ToolExecutionStatus status) {
        return chatConfig.getColor(status);
    }
}
