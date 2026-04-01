package coffeeshop.simulation.view;

import coffeeshop.simulation.model.SimulationModel;
import coffeeshop.simulation.model.SimulationSnapshot;
import coffeeshop.simulation.model.SimulationSpeedMode;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionListener;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSplitPane;
import javax.swing.SwingUtilities;

public class SimulationFrame extends JFrame implements SimulationView {

    private final SimulationModel model;
    private final ControlPanel controlPanel = new ControlPanel();
    private final QueuePanel queuePanel = new QueuePanel();
    private final StaffPanel staffPanel = new StaffPanel();
    private final LogPanel logPanel = new LogPanel();

    public SimulationFrame(SimulationModel model) {
        if (model == null) {
            throw new IllegalArgumentException("model must not be null.");
        }
        this.model = model;

        setTitle("Coffee Shop Stage 2 Simulation");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setMinimumSize(new Dimension(1000, 700));
        setLayout(new BorderLayout(8, 8));

        add(controlPanel, BorderLayout.NORTH);

        JSplitPane centerSplit = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, queuePanel, staffPanel);
        // Keep queue:staff width ratio close to 1:5.
        centerSplit.setResizeWeight(1.0 / 6.0);

        JPanel centerPanel = new JPanel(new BorderLayout());
        centerPanel.add(centerSplit, BorderLayout.CENTER);

        JSplitPane verticalSplit = new JSplitPane(JSplitPane.VERTICAL_SPLIT, centerPanel, logPanel);
        verticalSplit.setResizeWeight(0.62);
        add(verticalSplit, BorderLayout.CENTER);

        pack();
        setLocationRelativeTo(null);
    }

    @Override
    public void update() {
        SimulationSnapshot snapshot = model.getSnapshot();
        SwingUtilities.invokeLater(() -> applySnapshot(snapshot));
    }

    @Override
    public void addStartSimulationListener(ActionListener listener) {
        controlPanel.addStartSimulationListener(listener);
    }

    @Override
    public void addStopSimulationListener(ActionListener listener) {
        controlPanel.addStopSimulationListener(listener);
    }

    @Override
    public void addSpeedChangeListener(ActionListener listener) {
        controlPanel.addSpeedChangeListener(listener);
    }

    @Override
    public void addAddStaffListener(ActionListener listener) {
        controlPanel.addAddStaffListener(listener);
    }

    @Override
    public void addRemoveStaffListener(ActionListener listener) {
        controlPanel.addRemoveStaffListener(listener);
    }

    @Override
    public String getMenuCsvPath() {
        return controlPanel.getMenuCsvPath();
    }

    @Override
    public String getOrdersCsvPath() {
        return controlPanel.getOrdersCsvPath();
    }

    @Override
    public int getStaffCountInput() {
        return controlPanel.getStaffCountInput();
    }

    @Override
    public long getProducerDelayMsInput() {
        return controlPanel.getProducerDelayMsInput();
    }

    @Override
    public SimulationSpeedMode getSelectedSpeedMode() {
        return controlPanel.getSelectedSpeedMode();
    }

    @Override
    public void showError(String message) {
        JOptionPane.showMessageDialog(this, message, "Error", JOptionPane.ERROR_MESSAGE);
    }

    private void applySnapshot(SimulationSnapshot snapshot) {
        queuePanel.setQueueOrders(snapshot.queueOrders());
        staffPanel.setStaffStates(snapshot.staffStates());
        logPanel.setLogs(snapshot.eventLogs());
        controlPanel.setSelectedSpeedMode(snapshot.speedMode());
        controlPanel.setRunning(snapshot.running());
    }
}
