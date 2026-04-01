package coffeeshop.app;

import coffeeshop.io.CsvLoader;
import coffeeshop.model.Menu;
import coffeeshop.model.Order;
import coffeeshop.simulation.SimulationLogger;
import coffeeshop.simulation.controller.SimulationEngine;
import coffeeshop.simulation.model.ProcessingTimeStrategyFactory;
import coffeeshop.simulation.model.QueueType;
import coffeeshop.simulation.model.QueuedOrder;
import coffeeshop.simulation.model.SimulationModel;
import coffeeshop.simulation.model.SimulationSpeedMode;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Random;
import java.util.TreeMap;

public final class Stage2SimulationApp {

    private static final String DEFAULT_MENU_SOURCE = "classpath:data/menu.csv";
    private static final String DEFAULT_ORDERS_SOURCE = "classpath:data/orders.csv";
    private static final String DEFAULT_LOG_PATH = "data/simulation.log";
    private static final String DEFAULT_REPORT_PATH = "data/report.txt";

    private Stage2SimulationApp() {
    }

    public static void main(String[] args) {
        String menuSource = resolveStringArg(args, 0, DEFAULT_MENU_SOURCE);
        String ordersSource = resolveStringArg(args, 1, DEFAULT_ORDERS_SOURCE);
        int staffCount = resolveIntArg(args, 2, SimulationEngine.DEFAULT_STAFF_COUNT);
        long producerDelayMs = resolveLongArg(args, 3, 300L);
        Path logPath = resolvePathArg(args, 4, DEFAULT_LOG_PATH);
        Path reportPath = resolvePathArg(args, 5, DEFAULT_REPORT_PATH);
        SimulationSpeedMode speedMode = resolveSpeedModeArg(args, 6, SimulationSpeedMode.ONE_X);

        Menu menu = CsvLoader.loadMenu(menuSource);
        TreeMap<LocalDateTime, Order> allOrders = CsvLoader.loadOrders(ordersSource, menu);
        List<QueuedOrder> queuedOrders = classifyOrders(new ArrayList<>(allOrders.values()));

        SimulationModel model = new SimulationModel();
        SimulationEngine engine =
                new SimulationEngine(
                        model,
                        ProcessingTimeStrategyFactory.createDefaultStrategies(new Random()),
                        SimulationLogger.getInstance(),
                        logPath,
                        reportPath);
        engine.start(queuedOrders, staffCount, producerDelayMs, speedMode);
        engine.awaitCompletion(0);

        System.out.println("Simulation completed.");
        System.out.println("Log file: " + logPath.toAbsolutePath().normalize());
        System.out.println("Report file: " + reportPath.toAbsolutePath().normalize());
    }

    private static String resolveStringArg(String[] args, int index, String defaultValue) {
        if (args != null && args.length > index && args[index] != null && !args[index].isBlank()) {
            return args[index].trim();
        }
        return defaultValue;
    }

    private static Path resolvePathArg(String[] args, int index, String defaultValue) {
        if (args != null && args.length > index && args[index] != null && !args[index].isBlank()) {
            return Paths.get(args[index]);
        }
        return Paths.get(defaultValue);
    }

    private static int resolveIntArg(String[] args, int index, int defaultValue) {
        if (args != null && args.length > index && args[index] != null && !args[index].isBlank()) {
            try {
                return Integer.parseInt(args[index]);
            } catch (NumberFormatException exception) {
                throw new IllegalArgumentException("Invalid integer argument at index " + index + ".", exception);
            }
        }
        return defaultValue;
    }

    private static long resolveLongArg(String[] args, int index, long defaultValue) {
        if (args != null && args.length > index && args[index] != null && !args[index].isBlank()) {
            try {
                return Long.parseLong(args[index]);
            } catch (NumberFormatException exception) {
                throw new IllegalArgumentException("Invalid long argument at index " + index + ".", exception);
            }
        }
        return defaultValue;
    }

    private static SimulationSpeedMode resolveSpeedModeArg(
            String[] args, int index, SimulationSpeedMode defaultValue) {
        if (args == null || args.length <= index || args[index] == null || args[index].isBlank()) {
            return defaultValue;
        }

        String normalized = args[index].trim().toUpperCase(Locale.ROOT);
        return switch (normalized) {
            case "0.5X", "HALF_X", "HALF" -> SimulationSpeedMode.HALF_X;
            case "1X", "ONE_X", "NORMAL" -> SimulationSpeedMode.ONE_X;
            case "2X", "TWO_X" -> SimulationSpeedMode.TWO_X;
            case "4X", "FOUR_X", "FAST" -> SimulationSpeedMode.FOUR_X;
            default ->
                    throw new IllegalArgumentException(
                            "Invalid speed mode at index "
                                    + index
                                    + ": '"
                                    + args[index]
                                    + "'. Use 0.5x, 1x, 2x, or 4x.");
        };
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
}
