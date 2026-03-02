package coffeeshop.app;

import coffeeshop.io.CsvLoader;
import coffeeshop.model.Category;
import coffeeshop.model.Menu;
import coffeeshop.model.Order;
import coffeeshop.model.OrderStatistics;
import coffeeshop.model.ReportGenerator;
import coffeeshop.ui.CoffeeShopFrame;
import java.awt.GraphicsEnvironment;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Locale;
import java.util.TreeMap;
import javax.swing.SwingUtilities;

public class CoffeeShopApp {

    private static final String DEFAULT_MENU_PATH = "data/menu.csv";
    private static final String DEFAULT_ORDERS_PATH = "data/orders.csv";
    public static final String DEFAULT_REPORT_PATH = "data/report.txt";

    public static void main(String[] args) {
        Path menuCsvPath = resolveMenuCsvPath(args);
        Path ordersCsvPath = resolveOrdersCsvPath(args);
        Path reportPath = Paths.get(DEFAULT_REPORT_PATH);

        Menu menu = CsvLoader.loadMenu(menuCsvPath);
        TreeMap<LocalDateTime, Order> allOrders = CsvLoader.loadOrders(ordersCsvPath, menu);
        OrderStatistics stats = new OrderStatistics();
        for (Order order : allOrders.values()) {
            stats.recordOrder(order);
        }

        int loadedCount = 0;
        for (Category category : Category.values()) {
            loadedCount += menu.listByCategory(category).size();
        }

        double totalSales = 0.0;
        for (Order order : allOrders.values()) {
            totalSales += order.getTotal();
        }

        System.out.println("CoffeeShopApp started");
        System.out.println("Loaded menu item count: " + loadedCount);
        System.out.println("Loaded order count: " + allOrders.size());
        System.out.println(String.format(Locale.ROOT, "Total sales: %.2f", totalSales));
        if (GraphicsEnvironment.isHeadless()) {
            generateAndWriteReport(allOrders, stats, reportPath);
            System.out.println("Report written to: " + DEFAULT_REPORT_PATH);
            return;
        }

        SwingUtilities.invokeLater(
                () -> {
                    CoffeeShopFrame frame = new CoffeeShopFrame(menu, allOrders, stats);
                    frame.setVisible(true);
                });
    }

    private static Path resolveMenuCsvPath(String[] args) {
        if (args != null && args.length > 0 && args[0] != null && !args[0].isBlank()) {
            return Paths.get(args[0]);
        }
        return Paths.get(DEFAULT_MENU_PATH);
    }

    private static Path resolveOrdersCsvPath(String[] args) {
        if (args != null && args.length > 1 && args[1] != null && !args[1].isBlank()) {
            return Paths.get(args[1]);
        }
        return Paths.get(DEFAULT_ORDERS_PATH);
    }

    public static void generateAndWriteReport(
            TreeMap<LocalDateTime, Order> allOrders, OrderStatistics stats, Path reportPath) {
        if (allOrders == null) {
            throw new IllegalArgumentException("allOrders must not be null.");
        }
        if (stats == null) {
            throw new IllegalArgumentException("stats must not be null.");
        }
        if (reportPath == null) {
            throw new IllegalArgumentException("reportPath must not be null.");
        }

        ReportGenerator reportGenerator = new ReportGenerator();
        String itemReport = reportGenerator.generateItemReport(stats);
        String salesReport = reportGenerator.generateSalesReport(new ArrayList<>(allOrders.values()));
        String reportContent = itemReport + System.lineSeparator() + System.lineSeparator() + salesReport;
        writeReport(reportPath, reportContent);
    }

    private static void writeReport(Path reportPath, String reportContent) {
        try {
            Path parent = reportPath.getParent();
            if (parent != null) {
                Files.createDirectories(parent);
            }
            Files.writeString(reportPath, reportContent);
        } catch (IOException exception) {
            System.err.println(
                    "Failed to write report to '" + reportPath + "': " + exception.getMessage());
        }
    }
}
