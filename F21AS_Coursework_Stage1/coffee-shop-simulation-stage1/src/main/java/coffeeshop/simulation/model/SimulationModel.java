package coffeeshop.simulation.model;

import coffeeshop.model.Order;
import coffeeshop.model.OrderStatistics;
import coffeeshop.model.ReportGenerator;
import coffeeshop.simulation.observer.SimulationObserver;
import coffeeshop.simulation.observer.Subject;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class SimulationModel implements Subject {

    private static final DateTimeFormatter LOG_TIME_FORMAT =
            DateTimeFormatter.ofPattern("HH:mm:ss.SSS", Locale.ROOT);

    private final List<SimulationObserver> observers = new ArrayList<>();
    private final List<QueueOrderViewData> queueOrders = new ArrayList<>();
    private final Map<String, StaffViewData> staffStates = new LinkedHashMap<>();
    private final List<String> eventLogs = new ArrayList<>();
    private final List<Order> completedOrders = new ArrayList<>();

    private OrderQueueManager queueManager = new OrderQueueManager();
    private boolean running;
    private boolean stopRequested;
    private SimulationSpeedMode speedMode = SimulationSpeedMode.ONE_X;

    @Override
    public void registerObserver(SimulationObserver observer) {
        if (observer == null) {
            throw new IllegalArgumentException("observer must not be null.");
        }
        synchronized (this) {
            observers.add(observer);
        }
    }

    @Override
    public void removeObserver(SimulationObserver observer) {
        if (observer == null) {
            return;
        }
        synchronized (this) {
            observers.remove(observer);
        }
    }

    @Override
    public void notifyObservers() {
        List<SimulationObserver> observerSnapshot;
        synchronized (this) {
            observerSnapshot = new ArrayList<>(observers);
        }
        for (SimulationObserver observer : observerSnapshot) {
            observer.update();
        }
    }

    public void startNewSimulation(List<ServiceStaff> staffMembers) {
        if (staffMembers == null) {
            throw new IllegalArgumentException("staffMembers must not be null.");
        }

        synchronized (this) {
            queueManager = new OrderQueueManager();
            queueOrders.clear();
            staffStates.clear();
            eventLogs.clear();
            completedOrders.clear();
            for (ServiceStaff staff : staffMembers) {
                if (staff == null) {
                    throw new IllegalArgumentException("staffMembers must not contain null.");
                }
                staffStates.put(staff.getStaffId(), toViewData(staff));
            }
            running = true;
            stopRequested = false;
            eventLogs.add(formatLogEntry("Simulation started. speed=" + speedMode.displayText()));
        }
        notifyObservers();
    }

    public void markStopRequested() {
        synchronized (this) {
            stopRequested = true;
            eventLogs.add(formatLogEntry("Graceful stop requested."));
        }
        notifyObservers();
    }

    public void markSimulationFinished() {
        synchronized (this) {
            running = false;
            eventLogs.add(formatLogEntry("Simulation finished."));
        }
        notifyObservers();
    }

    public void appendLog(String message) {
        if (message == null || message.isBlank()) {
            throw new IllegalArgumentException("message must not be null or blank.");
        }
        synchronized (this) {
            eventLogs.add(formatLogEntry(message));
        }
        notifyObservers();
    }

    public void updateStaffState(ServiceStaff staff) {
        if (staff == null) {
            throw new IllegalArgumentException("staff must not be null.");
        }
        synchronized (this) {
            staffStates.put(staff.getStaffId(), toViewData(staff));
        }
        notifyObservers();
    }

    public synchronized void recordCompletedOrder(Order order) {
        if (order == null) {
            throw new IllegalArgumentException("order must not be null.");
        }
        completedOrders.add(order);
    }

    public void refreshQueueSnapshot() {
        List<QueuedOrder> queueSnapshot = getQueueManager().snapshotQueuedOrders();
        List<QueueOrderViewData> queueViewData = new ArrayList<>(queueSnapshot.size());
        for (QueuedOrder queuedOrder : queueSnapshot) {
            queueViewData.add(
                    new QueueOrderViewData(
                            queuedOrder.order().getOrderId(),
                            queuedOrder.order().getTotalItemCount(),
                            queuedOrder.queueType()));
        }

        synchronized (this) {
            queueOrders.clear();
            queueOrders.addAll(queueViewData);
        }
        notifyObservers();
    }

    public void setSpeedMode(SimulationSpeedMode speedMode) {
        if (speedMode == null) {
            throw new IllegalArgumentException("speedMode must not be null.");
        }
        synchronized (this) {
            this.speedMode = speedMode;
            eventLogs.add(formatLogEntry("Speed changed to " + speedMode.displayText() + "."));
        }
        notifyObservers();
    }

    public synchronized OrderQueueManager getQueueManager() {
        return queueManager;
    }

    public synchronized boolean isStopRequested() {
        return stopRequested;
    }

    public synchronized boolean isRunning() {
        return running;
    }

    public synchronized SimulationSpeedMode getSpeedMode() {
        return speedMode;
    }

    public synchronized int getActiveStaffCount() {
        int activeCount = 0;
        for (StaffViewData staff : staffStates.values()) {
            if (staff.status() != ServiceStaffStatus.STOPPED) {
                activeCount++;
            }
        }
        return activeCount;
    }

    public synchronized SimulationSnapshot getSnapshot() {
        return new SimulationSnapshot(
                running,
                stopRequested,
                speedMode,
                new ArrayList<>(queueOrders),
                new ArrayList<>(staffStates.values()),
                new ArrayList<>(eventLogs));
    }

    public String buildReportContent() {
        List<Order> ordersSnapshot;
        synchronized (this) {
            ordersSnapshot = new ArrayList<>(completedOrders);
        }

        OrderStatistics statistics = new OrderStatistics();
        for (Order order : ordersSnapshot) {
            statistics.recordOrder(order);
        }

        ReportGenerator reportGenerator = new ReportGenerator();
        String itemReport = reportGenerator.generateItemReport(statistics);
        String salesReport = reportGenerator.generateSalesReport(ordersSnapshot);
        return itemReport + System.lineSeparator() + System.lineSeparator() + salesReport;
    }

    private static StaffViewData toViewData(ServiceStaff staff) {
        return new StaffViewData(
                staff.getStaffId(),
                staff.getStatus(),
                normalize(staff.getCurrentOrderId(), "-"),
                normalize(staff.getCurrentActionText(), "-"));
    }

    private static String normalize(String value, String fallback) {
        if (value == null || value.isBlank()) {
            return fallback;
        }
        return value;
    }

    private static String formatLogEntry(String message) {
        return LOG_TIME_FORMAT.format(LocalDateTime.now()) + " " + message;
    }
}
