package coffeeshop.simulation.controller;

import static org.junit.jupiter.api.Assertions.assertTrue;

import coffeeshop.model.Category;
import coffeeshop.model.InvalidMenuItemDataException;
import coffeeshop.model.MenuItem;
import coffeeshop.model.Order;
import coffeeshop.simulation.SimulationLogger;
import coffeeshop.simulation.model.ProcessingTimeStrategy;
import coffeeshop.simulation.model.QueueType;
import coffeeshop.simulation.model.QueuedOrder;
import coffeeshop.simulation.model.ServiceStaffStatus;
import coffeeshop.simulation.model.SimulationModel;
import coffeeshop.simulation.model.SimulationSpeedMode;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.function.BooleanSupplier;
import org.junit.jupiter.api.Test;

class SimulationEngineDynamicStaffTest {

    @Test
    void shouldAddAndRemoveStaffGracefullyDuringRun() throws Exception {
        Path logPath = Files.createTempFile("engine-dynamic-staff", ".log");
        Path reportPath = Files.createTempFile("engine-dynamic-staff-report", ".txt");
        try {
            SimulationModel model = new SimulationModel();
            SimulationEngine engine =
                    new SimulationEngine(
                            model,
                            buildStrategies(200L, 120L, 80L, 40L),
                            SimulationLogger.getInstance(),
                            logPath,
                            reportPath);

            engine.start(buildQueuedOrders(10), 2, 0L, SimulationSpeedMode.ONE_X);
            assertTrue(waitUntil(() -> model.getSnapshot().running(), 1_000L));

            assertTrue(engine.addStaff());
            assertTrue(waitUntil(() -> model.getSnapshot().staffStates().size() >= 3, 2_000L));

            assertTrue(engine.requestRemoveOneStaff());
            assertTrue(
                    waitUntil(
                            () ->
                                    model.getSnapshot().staffStates().stream()
                                            .anyMatch(staff -> staff.status() == ServiceStaffStatus.STOPPED),
                            3_000L));

            engine.requestGracefulStop();
            assertTrue(waitUntil(() -> !model.getSnapshot().running(), 5_000L));

            String logs = String.join(System.lineSeparator(), model.getSnapshot().eventLogs());
            assertTrue(logs.contains("Staff added"));
            assertTrue(logs.contains("Remove requested"));
            assertTrue(logs.contains("stopped"));
            assertTrue(Files.size(reportPath) > 0);
        } finally {
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

    private static List<QueuedOrder> buildQueuedOrders(int count) throws InvalidMenuItemDataException {
        List<QueuedOrder> orders = new ArrayList<>(count);
        for (int i = 0; i < count; i++) {
            QueueType queueType = i % 2 == 0 ? QueueType.REGULAR : QueueType.ONLINE;
            orders.add(new QueuedOrder(createOrder("ORD-" + i), queueType));
        }
        return orders;
    }

    private static Order createOrder(String orderId) throws InvalidMenuItemDataException {
        Order order = new Order(orderId, LocalDateTime.parse("2026-03-03T10:00:00"));
        order.addLine(new MenuItem("DRINK-001", "Americano", 2.80, Category.DRINK), 1, false);
        return order;
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
}
