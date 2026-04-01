package coffeeshop.simulation.controller;

import static org.junit.jupiter.api.Assertions.assertTrue;

import coffeeshop.simulation.SimulationLogger;
import coffeeshop.simulation.model.ProcessingTimeStrategy;
import coffeeshop.simulation.model.SimulationModel;
import coffeeshop.simulation.model.SimulationSnapshot;
import coffeeshop.simulation.model.SimulationSpeedMode;
import coffeeshop.simulation.view.SimulationView;
import java.awt.event.ActionListener;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.EnumMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BooleanSupplier;
import org.junit.jupiter.api.Test;

class SimulationControllerTest {

    @Test
    void startAndStopActionsShouldDriveModelState() throws Exception {
        Path menuCsv = Files.createTempFile("stage2-menu", ".csv");
        Path ordersCsv = Files.createTempFile("stage2-orders", ".csv");
        Path logPath = Files.createTempFile("stage2-sim", ".log");
        Path reportPath = Files.createTempFile("stage2-report", ".txt");
        try {
            Files.writeString(
                    menuCsv,
                    String.join(
                            System.lineSeparator(),
                            "id,name,price,category",
                            "DRINK-001,Americano,2.80,DRINK",
                            "FOOD-001,Sandwich,5.00,FOOD"));
            Files.writeString(
                    ordersCsv,
                    String.join(
                            System.lineSeparator(),
                            "timestamp,customerId,itemId,quantity,hasOwnCup",
                            "2026-03-02T10:00:00,CUST-001,DRINK-001,1,false",
                        "2026-03-02T10:00:01,ONLINE-CUST-002,FOOD-001,1,false",
                            "2026-03-02T10:00:02,CUST-003,DRINK-001,1,false",
                            "2026-03-02T10:00:03,ONLINE-CUST-004,FOOD-001,1,false",
                            "2026-03-02T10:00:04,CUST-005,DRINK-001,1,false",
                            "2026-03-02T10:00:05,CUST-006,DRINK-001,1,false",
                            "2026-03-02T10:00:06,ONLINE-CUST-007,FOOD-001,1,false",
                            "2026-03-02T10:00:07,CUST-008,DRINK-001,1,false"));

            SimulationModel model = new SimulationModel();
            FakeSimulationView view =
                    new FakeSimulationView(
                            menuCsv, ordersCsv, 2, 5L, SimulationSpeedMode.ONE_X, model);

            SimulationEngineFactory engineFactory =
                    simulationModel ->
                            new SimulationEngine(
                                    simulationModel,
                                    buildStrategies(200L, 120L, 80L, 30L),
                                    SimulationLogger.getInstance(),
                                    logPath,
                                    reportPath);

            new SimulationController(model, view, engineFactory);
            view.triggerStart();

            assertTrue(waitUntil(() -> model.getSnapshot().running(), 1_000L));
            view.setSelectedSpeedMode(SimulationSpeedMode.FOUR_X);
            view.triggerSpeedChange();
            assertTrue(
                    waitUntil(
                            () -> model.getSnapshot().speedMode() == SimulationSpeedMode.FOUR_X,
                            1_000L));

            view.triggerAddStaff();
            assertTrue(waitUntil(() -> model.getSnapshot().staffStates().size() >= 3, 1_500L));

            view.triggerRemoveStaff();
            assertTrue(
                    waitUntil(
                            () ->
                                    model.getSnapshot().eventLogs().stream()
                                            .anyMatch(log -> log.contains("ONLINE order")),
                            2_000L));
            assertTrue(
                    waitUntil(
                            () ->
                                    model.getSnapshot().eventLogs().stream()
                                            .anyMatch(log -> log.contains("online priority applied")),
                            2_000L));
            view.triggerStop();
            assertTrue(waitUntil(() -> !model.getSnapshot().running(), 3_000L));

            SimulationSnapshot snapshot = model.getSnapshot();
            assertTrue(snapshot.stopRequested());
            assertTrue(snapshot.speedMode() == SimulationSpeedMode.FOUR_X);
            assertTrue(
                    snapshot.eventLogs().stream()
                            .anyMatch(log -> log.contains("Graceful stop requested.")));
            assertTrue(
                    snapshot.eventLogs().stream()
                            .anyMatch(log -> log.contains("Simulation finished.")));
            assertTrue(Files.size(reportPath) > 0);
            assertTrue(view.getUpdateCount() > 0);
        } finally {
            Files.deleteIfExists(menuCsv);
            Files.deleteIfExists(ordersCsv);
            Files.deleteIfExists(logPath);
            Files.deleteIfExists(reportPath);
        }
    }

    private static Map<SimulationSpeedMode, ProcessingTimeStrategy> buildStrategies(
            long half, long one, long two, long four) {
        EnumMap<SimulationSpeedMode, ProcessingTimeStrategy> strategies =
                new EnumMap<>(SimulationSpeedMode.class);
        strategies.put(SimulationSpeedMode.HALF_X, order -> half);
        strategies.put(SimulationSpeedMode.ONE_X, order -> one);
        strategies.put(SimulationSpeedMode.TWO_X, order -> two);
        strategies.put(SimulationSpeedMode.FOUR_X, order -> four);
        return strategies;
    }

    private static boolean waitUntil(BooleanSupplier condition, long timeoutMs) throws InterruptedException {
        long deadline = System.currentTimeMillis() + timeoutMs;
        while (System.currentTimeMillis() < deadline) {
            if (condition.getAsBoolean()) {
                return true;
            }
            Thread.sleep(20L);
        }
        return condition.getAsBoolean();
    }

    private static final class FakeSimulationView implements SimulationView {
        private final String menuCsvPath;
        private final String ordersCsvPath;
        private final int staffCount;
        private final long producerDelayMs;
        private SimulationSpeedMode speedMode;
        private final SimulationModel model;

        private ActionListener startListener;
        private ActionListener stopListener;
        private ActionListener speedListener;
        private ActionListener addStaffListener;
        private ActionListener removeStaffListener;
        private final AtomicInteger updateCount = new AtomicInteger(0);

        FakeSimulationView(
                Path menuCsvPath,
                Path ordersCsvPath,
                int staffCount,
                long producerDelayMs,
                SimulationSpeedMode speedMode,
                SimulationModel model) {
            this.menuCsvPath = menuCsvPath.toString();
            this.ordersCsvPath = ordersCsvPath.toString();
            this.staffCount = staffCount;
            this.producerDelayMs = producerDelayMs;
            this.speedMode = speedMode;
            this.model = model;
        }

        @Override
        public void addStartSimulationListener(ActionListener listener) {
            this.startListener = listener;
        }

        @Override
        public void addStopSimulationListener(ActionListener listener) {
            this.stopListener = listener;
        }

        @Override
        public void addSpeedChangeListener(ActionListener listener) {
            this.speedListener = listener;
        }

        @Override
        public void addAddStaffListener(ActionListener listener) {
            this.addStaffListener = listener;
        }

        @Override
        public void addRemoveStaffListener(ActionListener listener) {
            this.removeStaffListener = listener;
        }

        @Override
        public String getMenuCsvPath() {
            return menuCsvPath;
        }

        @Override
        public String getOrdersCsvPath() {
            return ordersCsvPath;
        }

        @Override
        public int getStaffCountInput() {
            return staffCount;
        }

        @Override
        public long getProducerDelayMsInput() {
            return producerDelayMs;
        }

        @Override
        public SimulationSpeedMode getSelectedSpeedMode() {
            return speedMode;
        }

        @Override
        public void showError(String message) {
            model.appendLog("VIEW_ERROR: " + message);
        }

        @Override
        public void update() {
            updateCount.incrementAndGet();
            model.getSnapshot();
        }

        void triggerStart() {
            if (startListener != null) {
                startListener.actionPerformed(null);
            }
        }

        void triggerStop() {
            if (stopListener != null) {
                stopListener.actionPerformed(null);
            }
        }

        void triggerSpeedChange() {
            if (speedListener != null) {
                speedListener.actionPerformed(null);
            }
        }

        void triggerAddStaff() {
            if (addStaffListener != null) {
                addStaffListener.actionPerformed(null);
            }
        }

        void triggerRemoveStaff() {
            if (removeStaffListener != null) {
                removeStaffListener.actionPerformed(null);
            }
        }

        void setSelectedSpeedMode(SimulationSpeedMode speedMode) {
            this.speedMode = speedMode;
        }

        int getUpdateCount() {
            return updateCount.get();
        }
    }
}
