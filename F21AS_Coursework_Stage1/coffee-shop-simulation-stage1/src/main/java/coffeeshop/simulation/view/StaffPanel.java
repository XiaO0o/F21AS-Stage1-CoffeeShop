package coffeeshop.simulation.view;

import coffeeshop.simulation.model.StaffViewData;
import java.awt.BorderLayout;
import java.util.ArrayList;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.AbstractTableModel;

public class StaffPanel extends JPanel {

    private final StaffTableModel tableModel = new StaffTableModel();
    private final JTable table = new JTable(tableModel);

    public StaffPanel() {
        setLayout(new BorderLayout());
        setBorder(BorderFactory.createTitledBorder("Service Staff"));

        table.setFillsViewportHeight(true);
        // Resize columns to fit available width, avoiding horizontal scrollbar.
        table.setAutoResizeMode(JTable.AUTO_RESIZE_SUBSEQUENT_COLUMNS);
        configureColumnWidths();
        add(new JScrollPane(table), BorderLayout.CENTER);
    }

    public void setStaffStates(List<StaffViewData> staffStates) {
        tableModel.setRows(staffStates);
    }

    private void configureColumnWidths() {
        table.getColumnModel().getColumn(0).setPreferredWidth(95); // Staff ID
        table.getColumnModel().getColumn(1).setPreferredWidth(190); // Status
        table.getColumnModel().getColumn(2).setPreferredWidth(120); // Current Order
        table.getColumnModel().getColumn(3).setPreferredWidth(360); // Current Action
    }

    private static final class StaffTableModel extends AbstractTableModel {
        private static final String[] COLUMNS = {"Staff ID", "Status", "Current Order", "Current Action"};

        private final List<StaffViewData> rows = new ArrayList<>();

        @Override
        public int getRowCount() {
            return rows.size();
        }

        @Override
        public int getColumnCount() {
            return COLUMNS.length;
        }

        @Override
        public String getColumnName(int column) {
            return COLUMNS[column];
        }

        @Override
        public Object getValueAt(int rowIndex, int columnIndex) {
            StaffViewData row = rows.get(rowIndex);
            return switch (columnIndex) {
                case 0 -> row.staffId();
                case 1 -> row.status();
                case 2 -> row.currentOrderId();
                case 3 -> row.currentActionText();
                default -> "";
            };
        }

        private void setRows(List<StaffViewData> newRows) {
            rows.clear();
            if (newRows != null) {
                rows.addAll(newRows);
            }
            fireTableDataChanged();
        }
    }
}
