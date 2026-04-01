package coffeeshop.simulation.view;

import coffeeshop.simulation.model.QueueOrderViewData;
import java.awt.BorderLayout;
import java.util.ArrayList;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.AbstractTableModel;

public class QueuePanel extends JPanel {

    private final QueueTableModel tableModel = new QueueTableModel();

    public QueuePanel() {
        setLayout(new BorderLayout());
        setBorder(BorderFactory.createTitledBorder("Waiting Queue"));

        JTable table = new JTable(tableModel);
        table.setFillsViewportHeight(true);
        add(new JScrollPane(table), BorderLayout.CENTER);
    }

    public void setQueueOrders(List<QueueOrderViewData> queueOrders) {
        tableModel.setRows(queueOrders);
    }

    private static final class QueueTableModel extends AbstractTableModel {
        private static final String[] COLUMNS = {"Order ID", "Total Items", "Queue Type"};

        private final List<QueueOrderViewData> rows = new ArrayList<>();

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
            QueueOrderViewData row = rows.get(rowIndex);
            return switch (columnIndex) {
                case 0 -> row.orderId();
                case 1 -> row.totalItems();
                case 2 -> row.queueType().toDisplayText();
                default -> "";
            };
        }

        private void setRows(List<QueueOrderViewData> newRows) {
            rows.clear();
            if (newRows != null) {
                rows.addAll(newRows);
            }
            fireTableDataChanged();
        }
    }
}
