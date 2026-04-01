package coffeeshop.simulation.model;

import java.util.List;

public record SimulationSnapshot(
        boolean running,
        boolean stopRequested,
        SimulationSpeedMode speedMode,
        List<QueueOrderViewData> queueOrders,
        List<StaffViewData> staffStates,
        List<String> eventLogs) {
    public SimulationSnapshot {
        if (speedMode == null) {
            throw new IllegalArgumentException("speedMode must not be null.");
        }
        if (queueOrders == null) {
            throw new IllegalArgumentException("queueOrders must not be null.");
        }
        if (staffStates == null) {
            throw new IllegalArgumentException("staffStates must not be null.");
        }
        if (eventLogs == null) {
            throw new IllegalArgumentException("eventLogs must not be null.");
        }
        queueOrders = List.copyOf(queueOrders);
        staffStates = List.copyOf(staffStates);
        eventLogs = List.copyOf(eventLogs);
    }
}
