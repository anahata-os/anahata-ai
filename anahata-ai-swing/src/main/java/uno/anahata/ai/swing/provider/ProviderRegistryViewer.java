/*
 * Licensed under the Anahata Software License (ASL) v 108. See the LICENSE file for details. Força Barça!
 */
package uno.anahata.ai.swing.provider;

import java.awt.BorderLayout;
import java.awt.Point;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;
import java.util.function.Consumer;
import javax.swing.*;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableRowSorter;
import uno.anahata.ai.model.provider.AbstractModel;
import uno.anahata.ai.swing.internal.AnyChangeDocumentListener;

public class ProviderRegistryViewer extends JPanel {

    private final JTable table;
    private final ModelTableModel tableModel;
    private final JTextField filterField;
    private final TableRowSorter<ModelTableModel> sorter;
    private final Consumer<AbstractModel> modelSelectionCallback;

    public ProviderRegistryViewer(List<AbstractModel> models, Consumer<AbstractModel> modelSelectionCallback) {
        super(new BorderLayout(10, 10));
        this.modelSelectionCallback = modelSelectionCallback;
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Filter Panel
        JPanel filterPanel = new JPanel(new BorderLayout(5, 5));
        filterField = new JTextField();
        filterPanel.add(new JLabel("Filter:"), BorderLayout.WEST);
        filterPanel.add(filterField, BorderLayout.CENTER);
        add(filterPanel, BorderLayout.NORTH);

        // Table
        tableModel = new ModelTableModel(models);
        sorter = new TableRowSorter<>(tableModel);
        
        table = new JTable(tableModel) {
            
            @Override
            public String getToolTipText(MouseEvent e) {
                Point p = e.getPoint();
                int viewRow = rowAtPoint(p);
                if (viewRow >= 0) {
                    int modelRow = convertRowIndexToModel(viewRow);
                    AbstractModel model = tableModel.getModelAt(modelRow);
                    if (model != null) {
                        // Return the HTML version for the JTextPane in the custom tooltip
                        return model.getRawDescription();
                    }
                }
                return super.getToolTipText(e);
            }
        };
        
        table.setRowSorter(sorter);
        table.setFillsViewportHeight(true);
        
        // Add double-click listener
        table.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2 && modelSelectionCallback != null) {
                    int viewRow = table.getSelectedRow();
                    if (viewRow >= 0) {
                        int modelRow = table.convertRowIndexToModel(viewRow);
                        AbstractModel model = tableModel.getModelAt(modelRow);
                        if (model != null) {
                            modelSelectionCallback.accept(model);
                        }
                    }
                }
            }
        });
        
        // Set preferred column widths
        table.getColumnModel().getColumn(0).setPreferredWidth(150); // Model ID
        table.getColumnModel().getColumn(1).setPreferredWidth(150); // Display Name
        table.getColumnModel().getColumn(2).setPreferredWidth(80);  // Version
        table.getColumnModel().getColumn(3).setPreferredWidth(250); // Description
        table.getColumnModel().getColumn(4).setPreferredWidth(200); // Supported Actions
        table.getColumnModel().getColumn(5).setPreferredWidth(100); // Input Tokens
        table.getColumnModel().getColumn(6).setPreferredWidth(100); // Output Tokens

        JScrollPane scrollPane = new JScrollPane(table);
        add(scrollPane, BorderLayout.CENTER);

        // Filter logic
        filterField.getDocument().addDocumentListener(new AnyChangeDocumentListener(this::applyFilter));
    }

    private void applyFilter() {
        String text = filterField.getText();
        sorter.setRowFilter(text.trim().isEmpty() ? null : RowFilter.regexFilter("(?i)" + text));
    }

    private static class ModelTableModel extends AbstractTableModel {
        private final String[] columnNames = {
            "Model ID", "Display Name", "Version", "Description",
            "Supported Actions", "Input Tokens", "Output Tokens"
        };
        private final List<AbstractModel> models;

        public ModelTableModel(List<AbstractModel> models) {
            this.models = models;
        }
        
        public AbstractModel getModelAt(int rowIndex) {
            if (rowIndex >= 0 && rowIndex < models.size()) {
                return models.get(rowIndex);
            }
            return null;
        }

        @Override public int getRowCount() { return models.size(); }
        @Override public int getColumnCount() { return columnNames.length; }
        @Override public String getColumnName(int column) { return columnNames[column]; }
        @Override public Class<?> getColumnClass(int columnIndex) {
            // All columns are now String or the default Object
            return super.getColumnClass(columnIndex);
        }

        @Override
        public Object getValueAt(int rowIndex, int columnIndex) {
            AbstractModel model = models.get(rowIndex);
            switch (columnIndex) {
                case 0: return model.getModelId();
                case 1: return model.getDisplayName();
                case 2: return model.getVersion();
                case 3: return model.getDescription();
                case 4: 
                    return String.join(", ", model.getSupportedActions());
                case 5: return model.getMaxInputTokens();
                case 6: return model.getMaxOutputTokens();
                default: return null;
            }
        }
    }

}