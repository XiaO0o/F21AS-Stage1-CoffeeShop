package coffeeshop.simulation.view;

import coffeeshop.simulation.model.SimulationSpeedMode;
import java.awt.FlowLayout;
import java.awt.event.ActionListener;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;

public class ControlPanel extends JPanel {

    private final JTextField menuPathField = new JTextField("classpath:data/menu.csv", 16);
    private final JTextField ordersPathField = new JTextField("classpath:data/orders.csv", 16);
    private final JSpinner staffCountSpinner = new JSpinner(new SpinnerNumberModel(2, 1, 20, 1));
    private final JSpinner producerDelaySpinner = new JSpinner(new SpinnerNumberModel(300, 0, 5_000, 50));
    private final JComboBox<SimulationSpeedMode> speedSelector =
            new JComboBox<>(SimulationSpeedMode.values());

    private final JButton startButton = new JButton("Start Simulation");
    private final JButton stopButton = new JButton("Stop");
    private final JButton addStaffButton = new JButton("+ Staff");
    private final JButton removeStaffButton = new JButton("- Staff");

    private boolean updatingFromModel;

    public ControlPanel() {
        setLayout(new FlowLayout(FlowLayout.LEFT));
        setBorder(BorderFactory.createTitledBorder("Simulation Control"));

        speedSelector.setSelectedItem(SimulationSpeedMode.ONE_X);
        stopButton.setEnabled(false);
        addStaffButton.setEnabled(false);
        removeStaffButton.setEnabled(false);

        add(new JLabel("Menu CSV:"));
        add(menuPathField);
        add(new JLabel("Orders CSV:"));
        add(ordersPathField);
        add(new JLabel("Initial Staff Count:"));
        add(staffCountSpinner);
        add(new JLabel("Order Arrival Delay (ms):"));
        add(producerDelaySpinner);
        add(new JLabel("Speed:"));
        add(speedSelector);
        add(startButton);
        add(stopButton);
        add(addStaffButton);
        add(removeStaffButton);
    }

    public void addStartSimulationListener(ActionListener listener) {
        startButton.addActionListener(listener);
    }

    public void addStopSimulationListener(ActionListener listener) {
        stopButton.addActionListener(listener);
    }

    public void addSpeedChangeListener(ActionListener listener) {
        speedSelector.addActionListener(
                event -> {
                    if (!updatingFromModel) {
                        listener.actionPerformed(event);
                    }
                });
    }

    public void addAddStaffListener(ActionListener listener) {
        addStaffButton.addActionListener(listener);
    }

    public void addRemoveStaffListener(ActionListener listener) {
        removeStaffButton.addActionListener(listener);
    }

    public String getMenuCsvPath() {
        return menuPathField.getText();
    }

    public String getOrdersCsvPath() {
        return ordersPathField.getText();
    }

    public int getStaffCountInput() {
        return (Integer) staffCountSpinner.getValue();
    }

    public long getProducerDelayMsInput() {
        return ((Integer) producerDelaySpinner.getValue()).longValue();
    }

    public SimulationSpeedMode getSelectedSpeedMode() {
        return (SimulationSpeedMode) speedSelector.getSelectedItem();
    }

    public void setSelectedSpeedMode(SimulationSpeedMode speedMode) {
        if (speedMode == null) {
            return;
        }
        Object current = speedSelector.getSelectedItem();
        if (speedMode.equals(current)) {
            return;
        }
        updatingFromModel = true;
        try {
            speedSelector.setSelectedItem(speedMode);
        } finally {
            updatingFromModel = false;
        }
    }

    public void setRunning(boolean running) {
        startButton.setEnabled(!running);
        stopButton.setEnabled(running);
        addStaffButton.setEnabled(running);
        removeStaffButton.setEnabled(running);
        // Initial staff count should be fixed once simulation starts.
        staffCountSpinner.setEnabled(!running);
    }
}
