package coffeeshop.simulation.view;

import coffeeshop.simulation.observer.SimulationObserver;
import coffeeshop.simulation.model.SimulationSpeedMode;
import java.awt.event.ActionListener;

public interface SimulationView extends SimulationObserver {

    void addStartSimulationListener(ActionListener listener);

    void addStopSimulationListener(ActionListener listener);

    void addSpeedChangeListener(ActionListener listener);

    void addAddStaffListener(ActionListener listener);

    void addRemoveStaffListener(ActionListener listener);

    String getMenuCsvPath();

    String getOrdersCsvPath();

    int getStaffCountInput();

    long getProducerDelayMsInput();

    SimulationSpeedMode getSelectedSpeedMode();

    void showError(String message);
}
