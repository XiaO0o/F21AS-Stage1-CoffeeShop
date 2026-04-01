package coffeeshop.simulation.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
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
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.function.BooleanSupplier;
import org.junit.jupiter.api.Test;

class SimulationEngineSmokeTest {

    @Test
    void producerAndSingleWorkerShouldFinishGracefully() throws Exception {
        Path tempLog = Files.createTempFile("simulation-smoke", ".log");
        Path tempReport = Files.createTempFile("simulation-smoke-report", ".txt");
        try {
            SimulationModel model = new SimulationModel();
            SimulationEngine engine =
                    new SimulationEngine(
                            model,
                            buildStrategies(1L),
                            SimulationLogger.getInstance(),
                            tempLog,
                            tempReport);

            List<QueuedOrder> orders =
                    List.of(
                            new QueuedOrder(createOrder("ORD-SMOKE-001"), QueueType.REGULAR),
                            new QueuedOrder(createOrder("ORD-SMOKE-002"), QueueType.ONLINE));
            engine.start(orders, 1, 0L, SimulationSpeedMode.ONE_X);
            assertTrue(waitUntil(() -> !engine.isRunning(), 5_000L));

            assertTrue(model.getQueueManager().isClosedAndEmpty());
            assertEquals(
                    List.of(ServiceStaffStatus.STOPPED),
                    model.getSnapshot().staffStates().stream().map(staff -> staff.status()).toList());
            assertTrue(Files.exists(tempLog));
            assertTrue(Files.size(tempReport) > 0);

            String logContent = Files.readString(tempLog);
            assertTrue(logContent.contains("Producer started"));
            assertTrue(logContent.contains("Simulation monitor detected all threads completed."));
        } finally {
            Files.deleteIfExists(tempLog);
            Files.deleteIfExists(tempReport);
        }
    }

    private static Map<SimulationSpeedMode, ProcessingTimeStrategy> buildStrategies(long durationMs) {
        EnumMap<SimulationSpeedMode, ProcessingTimeStrategy> strategies =
                new EnumMap<>(SimulationSpeedMode.class);
        for (SimulationSpeedMode mode : SimulationSpeedMode.values()) {
            strategies.put(mode, order -> durationMs);
        }
        return strategies;
    }

    private static Order createOrder(String orderId) throws InvalidMenuItemDataException {
        Order order = new Order(orderId, LocalDateTime.parse("2026-03-02T15:00:00"));
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
