package coffeeshop.ui;

import coffeeshop.app.CoffeeShopApp;
import coffeeshop.model.Category;
import coffeeshop.model.Menu;
import coffeeshop.model.MenuItem;
import coffeeshop.model.Order;
import coffeeshop.model.OrderStatistics;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.TreeMap;
import javax.swing.BorderFactory;
import javax.swing.DefaultListCellRenderer;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.ScrollPaneConstants;
import javax.swing.SpinnerNumberModel;
import javax.swing.table.DefaultTableModel;

public class CoffeeShopFrame extends JFrame {

    private static final DateTimeFormatter ORDER_TIME_FORMAT =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private final TreeMap<LocalDateTime, Order> allOrders;
    private final OrderStatistics stats;
    private final List<CartLine> currentLines = new ArrayList<>();

    private final JList<MenuItem> menuList;
    private final JTextField customerIdField;
    private final JSpinner quantitySpinner;
    private final JCheckBox hasOwnCupCheckBox;
    private final JTable cartTable;
    private final DefaultTableModel cartTableModel;
    private final JTextArea billArea;
    private final JLabel orderStatusLabel;
    private final JLabel orderTimeLabel;
    private final JLabel statusBarLabel;

    private Order currentOrder;
    private String currentCustomerId;

    public CoffeeShopFrame(Menu menu, TreeMap<LocalDateTime, Order> allOrders, OrderStatistics stats) {
        if (menu == null) {
            throw new IllegalArgumentException("menu must not be null.");
        }
        if (allOrders == null) {
            throw new IllegalArgumentException("allOrders must not be null.");
        }
        if (stats == null) {
            throw new IllegalArgumentException("stats must not be null.");
        }

        this.allOrders = allOrders;
        this.stats = stats;

        setTitle("Coffee Shop Stage 1");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setMinimumSize(new Dimension(900, 600));
        setPreferredSize(new Dimension(1000, 650));
        setLayout(new BorderLayout(8, 8));

        customerIdField = new JTextField(18);
        quantitySpinner = new JSpinner(new SpinnerNumberModel(1, 1, 999, 1));
        hasOwnCupCheckBox = new JCheckBox("Has Own Cup");
        billArea = new JTextArea();
        billArea.setEditable(false);
        orderStatusLabel = new JLabel("Current order: none");
        orderTimeLabel = new JLabel("Order time: -");
        statusBarLabel = new JLabel("Ready.");

        menuList = new JList<>(buildMenuModel(menu));
        menuList.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        menuList.setCellRenderer(new MenuItemListCellRenderer());
        menuList.addListSelectionListener(
                event -> {
                    if (!event.getValueIsAdjusting()) {
                        updateOwnCupControlBySelection();
                    }
                });

        cartTableModel =
                new DefaultTableModel(
                        new Object[] {"Item ID", "Name", "Qty", "Cup", "Line Subtotal"},
                        0) {
                    @Override
                    public boolean isCellEditable(int row, int column) {
                        return false;
                    }
                };
        cartTable = new JTable(cartTableModel);
        cartTable.setFillsViewportHeight(true);

        add(buildTopPanel(), BorderLayout.NORTH);
        add(buildCenterPanel(), BorderLayout.CENTER);
        add(buildStatusBarPanel(), BorderLayout.SOUTH);

        pack();
        setSize(1000, 650);
        setLocationRelativeTo(null);
    }

    private JPanel buildTopPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        panel.setBorder(BorderFactory.createTitledBorder("Order Controls"));

        JButton newOrderButton = new JButton("New Order");
        newOrderButton.addActionListener(event -> createNewOrder());

        panel.add(new JLabel("Customer ID:"));
        panel.add(customerIdField);
        panel.add(newOrderButton);
        panel.add(orderStatusLabel);
        panel.add(orderTimeLabel);
        return panel;
    }

    private JPanel buildStatusBarPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(4, 8, 4, 8));
        panel.add(statusBarLabel, BorderLayout.WEST);
        return panel;
    }

    private JSplitPane buildCenterPanel() {
        JScrollPane leftScroll = new JScrollPane(menuList);
        leftScroll.setBorder(BorderFactory.createTitledBorder("Menu Items"));
        leftScroll.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
        leftScroll.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        leftScroll.setPreferredSize(new Dimension(360, 500));

        JPanel rightPanel = new JPanel(new BorderLayout(8, 8));
        rightPanel.setBorder(BorderFactory.createTitledBorder("Current Order"));

        JPanel editPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        editPanel.add(new JLabel("Quantity:"));
        editPanel.add(quantitySpinner);
        editPanel.add(hasOwnCupCheckBox);

        JButton addSelectedButton = new JButton("Add Selected Items");
        addSelectedButton.addActionListener(event -> addSelectedItems());
        JButton completeOrderButton = new JButton("Complete Order");
        completeOrderButton.addActionListener(event -> completeOrder());
        JButton exitButton = new JButton("Exit");
        exitButton.addActionListener(event -> exitAndGenerateReport());

        editPanel.add(addSelectedButton);
        editPanel.add(completeOrderButton);
        editPanel.add(exitButton);

        JScrollPane cartScrollPane = new JScrollPane(cartTable);
        cartScrollPane.setBorder(BorderFactory.createTitledBorder("Cart"));
        cartScrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
        cartScrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);

        JScrollPane billScrollPane = new JScrollPane(billArea);
        billScrollPane.setBorder(BorderFactory.createTitledBorder("Bill Details"));
        billScrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
        billScrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);

        JSplitPane rightSplitPane =
                new JSplitPane(JSplitPane.VERTICAL_SPLIT, cartScrollPane, billScrollPane);
        rightSplitPane.setResizeWeight(0.55);

        rightPanel.add(editPanel, BorderLayout.NORTH);
        rightPanel.add(rightSplitPane, BorderLayout.CENTER);

        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, leftScroll, rightPanel);
        splitPane.setResizeWeight(0.35);
        return splitPane;
    }

    private void createNewOrder() {
        String customerId = customerIdField.getText().trim();
        if (customerId.isEmpty()) {
            showWarning("No customer ID", "Please input a non-empty customer ID.");
            return;
        }

        LocalDateTime createdAt = LocalDateTime.now();
        currentCustomerId = customerId;
        currentOrder = new Order(customerId, createdAt);
        currentLines.clear();
        refreshCartTable();
        refreshBillDetails();
        menuList.clearSelection();
        hasOwnCupCheckBox.setSelected(false);
        hasOwnCupCheckBox.setEnabled(true);
        quantitySpinner.setValue(1);
        orderStatusLabel.setText("Current order: " + customerId);
        orderTimeLabel.setText("Order time: " + formatOrderTime(createdAt));
        setStatus("Created a new order for " + customerId + ".");
    }

    private void addSelectedItems() {
        if (!ensureCurrentOrderExists()) {
            return;
        }

        List<MenuItem> selectedItems = menuList.getSelectedValuesList();
        if (selectedItems.isEmpty()) {
            showWarning("No selection", "Please select at least one menu item.");
            return;
        }

        int quantity = (Integer) quantitySpinner.getValue();
        boolean hasOwnCup = hasOwnCupCheckBox.isSelected();
        for (MenuItem item : selectedItems) {
            currentOrder.addLine(item, quantity, hasOwnCup);
            currentLines.add(new CartLine(item, quantity, hasOwnCup));
        }
        refreshCartTable();

        if (selectedItems.size() == 1) {
            MenuItem item = selectedItems.get(0);
            String ownCupText = hasOwnCup ? " (Own cup)" : "";
            setStatus("Added: " + item.getId() + " x" + quantity + ownCupText);
        } else {
            setStatus("Added " + selectedItems.size() + " lines to cart.");
        }
        refreshBillDetails();

        showInfo("Added to cart", "Added " + selectedItems.size() + " item(s) to the cart.");
    }

    private void refreshCartTable() {
        cartTableModel.setRowCount(0);
        for (CartLine line : currentLines) {
            double lineSubtotal = line.item().getPrice() * line.quantity();
            cartTableModel.addRow(
                    new Object[] {
                        line.item().getId(),
                        line.item().getName(),
                        line.quantity(),
                        getOwnCupDisplay(line),
                        String.format(Locale.ROOT, "%.2f", lineSubtotal)
                    });
        }
    }

    private String getOwnCupDisplay(CartLine line) {
        if (line.item().getCategory() != Category.DRINK) {
            return "N/A";
        }
        return line.hasOwnCup() ? "Own cup" : "-";
    }

    private void refreshBillDetails() {
        if (currentOrder == null) {
            billArea.setText("");
            return;
        }

        billArea.setText(buildBillText(currentOrder));
        billArea.setCaretPosition(0);
    }

    private String buildBillText(Order order) {
        StringBuilder text = new StringBuilder();
        text.append("Lines:").append(System.lineSeparator());
        if (currentLines.isEmpty()) {
            text.append("(no lines)").append(System.lineSeparator());
        } else {
            for (CartLine line : currentLines) {
                double lineSubtotal = line.item().getPrice() * line.quantity();
                text.append(
                                String.format(
                                        Locale.ROOT,
                                        "%s %s x%d (Own cup: %s)  Line: £%.2f",
                                        line.item().getId(),
                                        line.item().getName(),
                                        line.quantity(),
                                        line.hasOwnCup() ? "true" : "false",
                                        lineSubtotal))
                        .append(System.lineSeparator());
            }
        }

        text.append(System.lineSeparator());
        double subtotal = order.getSubtotal();
        double ruleADiscount = order.applyRuleA();
        double ruleBBase = Math.max(0.0, subtotal - ruleADiscount);
        double ruleBDiscount = order.applyRuleB();
        double total = order.getTotal();
        int eligibleDrinkUnits = countEligibleDrinkUnitsForCurrentLines();

        text.append(String.format(Locale.ROOT, "Subtotal: £%.2f", subtotal))
                .append(System.lineSeparator());
        text.append(
                        String.format(
                                Locale.ROOT,
                                "Rule A: -£%.2f (Eligible drinks: %d x £1.00)",
                                ruleADiscount,
                                eligibleDrinkUnits))
                .append(System.lineSeparator());
        if (subtotal >= 15.0) {
            text.append(
                            String.format(
                                    Locale.ROOT,
                                    "Rule B: -£%.2f (10%% off £%.2f because subtotal >= £15.00)",
                                    ruleBDiscount,
                                    ruleBBase))
                    .append(System.lineSeparator());
        } else {
            text.append("Rule B: -£0.00 (Subtotal < £15.00)").append(System.lineSeparator());
        }
        text.append(String.format(Locale.ROOT, "Total: £%.2f", total)).append(System.lineSeparator());
        return text.toString();
    }

    private int countEligibleDrinkUnitsForCurrentLines() {
        int eligibleUnits = 0;
        for (CartLine line : currentLines) {
            if (line.item().getCategory() == Category.DRINK && line.hasOwnCup()) {
                eligibleUnits += line.quantity();
            }
        }
        return eligibleUnits;
    }

    private void completeOrder() {
        if (!ensureCurrentOrderExists()) {
            return;
        }

        if (currentCustomerId == null || currentCustomerId.isBlank()) {
            showWarning("Invalid order", "Missing customer ID for the current order.");
            return;
        }

        LocalDateTime orderKey = resolveUniqueOrderKey(LocalDateTime.now());

        // Save with a final timestamp so the map key and order time are always the same.
        Order finalizedOrder = new Order(currentCustomerId, orderKey);
        for (CartLine line : currentLines) {
            finalizedOrder.addLine(line.item(), line.quantity(), line.hasOwnCup());
        }

        allOrders.put(orderKey, finalizedOrder);
        stats.recordOrder(finalizedOrder);

        int completedLineCount = currentLines.size();
        currentOrder = null;
        currentCustomerId = null;
        currentLines.clear();
        refreshCartTable();
        refreshBillDetails();
        menuList.clearSelection();
        hasOwnCupCheckBox.setSelected(false);
        hasOwnCupCheckBox.setEnabled(true);
        orderStatusLabel.setText("Current order: none");
        orderTimeLabel.setText("Order time: -");

        setStatus("Completed order at " + formatOrderTime(orderKey) + ".");
        showInfo(
                "Order completed",
                "Order completed with "
                        + completedLineCount
                        + " line(s). Stored timestamp key: "
                        + orderKey);
    }

    private void exitAndGenerateReport() {
        Path reportPath = Paths.get(CoffeeShopApp.DEFAULT_REPORT_PATH);
        CoffeeShopApp.generateAndWriteReport(allOrders, stats, reportPath);
        setStatus("Report generated at " + reportPath + ".");
        showInfo("Report generated", "Report generated at: " + reportPath);
        dispose();
    }

    private boolean ensureCurrentOrderExists() {
        if (currentOrder != null) {
            return true;
        }
        showWarning("No active order", "Please create a new order first.");
        return false;
    }

    private void updateOwnCupControlBySelection() {
        List<MenuItem> selectedItems = menuList.getSelectedValuesList();
        if (selectedItems.isEmpty()) {
            hasOwnCupCheckBox.setEnabled(true);
            return;
        }

        boolean allDrink = true;
        for (MenuItem item : selectedItems) {
            if (item.getCategory() != Category.DRINK) {
                allDrink = false;
                break;
            }
        }

        if (allDrink) {
            hasOwnCupCheckBox.setEnabled(true);
            return;
        }

        if (hasOwnCupCheckBox.isSelected()) {
            showInfo("Own cup disabled", "Own cup applies to DRINK items only.");
        }
        hasOwnCupCheckBox.setSelected(false);
        hasOwnCupCheckBox.setEnabled(false);
        setStatus("Own cup is available for DRINK items only.");
    }

    private LocalDateTime resolveUniqueOrderKey(LocalDateTime candidate) {
        LocalDateTime resolved = candidate;
        while (allOrders.containsKey(resolved)) {
            resolved = resolved.plusNanos(1);
        }
        return resolved;
    }

    private static String formatOrderTime(LocalDateTime dateTime) {
        return ORDER_TIME_FORMAT.format(dateTime);
    }

    private void setStatus(String message) {
        statusBarLabel.setText(message);
    }

    private void showInfo(String title, String message) {
        JOptionPane.showMessageDialog(this, message, title, JOptionPane.INFORMATION_MESSAGE);
    }

    private void showWarning(String title, String message) {
        JOptionPane.showMessageDialog(this, message, title, JOptionPane.WARNING_MESSAGE);
    }

    private static DefaultListModel<MenuItem> buildMenuModel(Menu menu) {
        DefaultListModel<MenuItem> model = new DefaultListModel<>();
        for (Category category : Category.values()) {
            for (MenuItem item : menu.listByCategory(category)) {
                model.addElement(item);
            }
        }
        return model;
    }

    private static final class MenuItemListCellRenderer extends DefaultListCellRenderer {

        @Override
        public Component getListCellRendererComponent(
                JList<?> list,
                Object value,
                int index,
                boolean isSelected,
                boolean cellHasFocus) {
            Component component =
                    super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
            if (value instanceof MenuItem item) {
                setText(
                        String.format(
                                Locale.ROOT,
                                "%s | %s | GBP %.2f",
                                item.getId(),
                                item.getName(),
                                item.getPrice()));
            }
            return component;
        }
    }

    private record CartLine(MenuItem item, int quantity, boolean hasOwnCup) {
    }
}
