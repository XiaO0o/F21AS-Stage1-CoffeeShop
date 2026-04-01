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
import coffeeshop.simulation.model.SimulationModel;
import coffeeshop.simulation.model.SimulationSpeedMode;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BooleanSupplier;
import org.junit.jupiter.api.Test;

class SimulationEngineSpeedStrategyTest {

    @Test
    void changingSpeedModeShouldSwitchProcessingStrategyAtRuntime() throws Exception {
        AtomicInteger oneXCalls = new AtomicInteger(0);
        AtomicInteger fourXCalls = new AtomicInteger(0);
        Path logPath = Files.createTempFile("engine-speed", ".log");
        Path reportPath = Files.createTempFile("engine-speed-report", ".txt");
        try {
            SimulationModel model = new SimulationModel();
            SimulationEngine engine =
                    new SimulationEngine(
                            model,
                            buildStrategies(oneXCalls, fourXCalls),
                            SimulationLogger.getInstance(),
                            logPath,
                            reportPath);

            engine.start(buildOrders(4), 1, 0L, SimulationSpeedMode.ONE_X);
            assertTrue(waitUntil(() -> oneXCalls.get() > 0, 2_000L));

            engine.changeSpeedMode(SimulationSpeedMode.FOUR_X);
            assertTrue(waitUntil(() -> fourXCalls.get() > 0, 2_000L));
            assertTrue(waitUntil(() -> !engine.isRunning(), 5_000L));

            assertTrue(oneXCalls.get() > 0);
            assertTrue(fourXCalls.get() > 0);
            String logs = String.join(System.lineSeparator(), model.getSnapshot().eventLogs());
            assertTrue(logs.contains("Speed changed to 4x."));
        } finally {
            Files.deleteIfExists(logPath);
            Files.deleteIfExists(reportPath);
        }
    }

    private static Map<SimulationSpeedMode, ProcessingTimeStrategy> buildStrategies(
            AtomicInteger oneXCalls, AtomicInteger fourXCalls) {
        EnumMap<SimulationSpeedMode, ProcessingTimeStrategy> strategies =
                new EnumMap<>(SimulationSpeedMode.class);
        strategies.put(SimulationSpeedMode.HALF_X, order -> 120L);
        strategies.put(
                SimulationSpeedMode.ONE_X,
                order -> {
                    oneXCalls.incrementAndGet();
                    return 120L;
                });
        strategies.put(SimulationSpeedMode.TWO_X, order -> 60L);
        strategies.put(
                SimulationSpeedMode.FOUR_X,
                order -> {
                    fourXCalls.incrementAndGet();
                    return 10L;
                });
        return strategies;
    }

    private static List<QueuedOrder> buildOrders(int count) throws InvalidMenuItemDataException {
        List<QueuedOrder> orders = new ArrayList<>(count);
        for (int i = 0; i < count; i++) {
            orders.add(new QueuedOrder(createOrder("ORD-SPEED-" + i), QueueType.REGULAR));
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
