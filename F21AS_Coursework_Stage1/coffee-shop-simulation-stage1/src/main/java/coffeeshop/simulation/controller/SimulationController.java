package coffeeshop.simulation.controller;

import coffeeshop.io.CsvLoader;
import coffeeshop.model.Menu;
import coffeeshop.model.Order;
import coffeeshop.simulation.model.QueueType;
import coffeeshop.simulation.model.QueuedOrder;
import coffeeshop.simulation.model.SimulationModel;
import coffeeshop.simulation.model.SimulationSpeedMode;
import coffeeshop.simulation.view.SimulationView;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.TreeMap;

public class SimulationController {

    private final SimulationModel model;
    private final SimulationView view;
    private final SimulationEngineFactory engineFactory;

    private SimulationEngine currentEngine;

    public SimulationController(
            SimulationModel model, SimulationView view, SimulationEngineFactory engineFactory) {
        if (model == null) {
            throw new IllegalArgumentException("model must not be null.");
        }
        if (view == null) {
            throw new IllegalArgumentException("view must not be null.");
        }
        if (engineFactory == null) {
            throw new IllegalArgumentException("engineFactory must not be null.");
        }

        this.model = model;
        this.view = view;
        this.engineFactory = engineFactory;

        model.registerObserver(view);
        view.addStartSimulationListener(new StartSimulationListener());
        view.addStopSimulationListener(new StopSimulationListener());
        view.addSpeedChangeListener(new SpeedChangeListener());
        view.addAddStaffListener(new AddStaffListener());
        view.addRemoveStaffListener(new RemoveStaffListener());
        model.setSpeedMode(view.getSelectedSpeedMode());
        model.notifyObservers();
    }

    private final class StartSimulationListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent event) {
            try {
                startSimulation();
            } catch (RuntimeException exception) {
                model.appendLog("Failed to start simulation: " + exception.getMessage());
                view.showError("Failed to start simulation: " + exception.getMessage());
            }
        }
    }

    private final class StopSimulationListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent event) {
            requestStop();
        }
    }

    private final class SpeedChangeListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent event) {
            SimulationSpeedMode speedMode = view.getSelectedSpeedMode();
            if (speedMode == null) {
                return;
            }
            if (currentEngine != null && currentEngine.isRunning()) {
                currentEngine.changeSpeedMode(speedMode);
            } else {
                model.setSpeedMode(speedMode);
            }
        }
    }

    private final class AddStaffListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent event) {
            if (currentEngine == null || !currentEngine.isRunning()) {
                view.showError("Start simulation before adding staff.");
                return;
            }
            currentEngine.addStaff();
        }
    }

    private final class RemoveStaffListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent event) {
            if (currentEngine == null || !currentEngine.isRunning()) {
                view.showError("No running simulation to remove staff from.");
                return;
            }
            if (!currentEngine.requestRemoveOneStaff()) {
                view.showError("Cannot remove more staff right now.");
            }
        }
    }

    public void shutdownAndClose(Runnable closeAction) {
        if (closeAction == null) {
            throw new IllegalArgumentException("closeAction must not be null.");
        }

        if (currentEngine == null || !currentEngine.isRunning()) {
            closeAction.run();
            return;
        }

        currentEngine.requestGracefulStop();
        Thread shutdownThread =
                new Thread(
                        () -> {
                            currentEngine.awaitCompletion(0);
                            closeAction.run();
                        },
                        "simulation-gui-shutdown");
        shutdownThread.start();
    }

    private void startSimulation() {
        if (currentEngine != null && currentEngine.isRunning()) {
            view.showError("Simulation is already running.");
            return;
        }

        String menuSource = requireSource(view.getMenuCsvPath(), "menu source");
        String ordersSource = requireSource(view.getOrdersCsvPath(), "orders source");
        int staffCount = view.getStaffCountInput();
        long producerDelayMs = view.getProducerDelayMsInput();

        Menu menu = CsvLoader.loadMenu(menuSource);
        TreeMap<LocalDateTime, Order> loadedOrders = CsvLoader.loadOrders(ordersSource, menu);
        List<Order> orders = new ArrayList<>(loadedOrders.values());
        List<QueuedOrder> queuedOrders = classifyOrders(orders);
        SimulationSpeedMode speedMode = view.getSelectedSpeedMode();

        currentEngine = engineFactory.create(model);
        currentEngine.start(queuedOrders, staffCount, producerDelayMs, speedMode);
    }

    private void requestStop() {
        if (currentEngine == null || !currentEngine.isRunning()) {
            view.showError("No running simulation to stop.");
            return;
        }
        currentEngine.requestGracefulStop();
    }

    private static List<QueuedOrder> classifyOrders(List<Order> orders) {
        List<QueuedOrder> queuedOrders = new ArrayList<>(orders.size());
        for (Order order : orders) {
            queuedOrders.add(new QueuedOrder(order, classifyQueueType(order)));
        }
        return queuedOrders;
    }

    private static QueueType classifyQueueType(Order order) {
        String orderId = order.getOrderId().trim().toUpperCase(Locale.ROOT);
        if (orderId.startsWith("ONLINE-") || orderId.contains("|ONLINE")) {
            return QueueType.ONLINE;
        }
        return QueueType.REGULAR;
    }

    private static String requireSource(String text, String fieldName) {
        if (text == null || text.isBlank()) {
            throw new IllegalArgumentException(fieldName + " must not be blank.");
        }
        return text.trim();
    }
}
