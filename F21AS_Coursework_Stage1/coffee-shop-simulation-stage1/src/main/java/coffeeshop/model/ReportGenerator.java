package coffeeshop.model;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TreeMap;

/**
 * Generates formatted text reports for items and sales statistics.
 */
public class ReportGenerator {

    /**
     * Generates a report grouping items by category and listing the top 5 best-selling items.
     */
    public String generateItemReport(OrderStatistics statistics) {
        if (statistics == null) {
            throw new IllegalArgumentException("statistics must not be null.");
        }

        Map<MenuItem, Integer> countMap = statistics.getCountMapView();
        String lineSeparator = System.lineSeparator();
        StringBuilder report = new StringBuilder();
        report.append("Item Report").append(lineSeparator);
        report.append("===========").append(lineSeparator);

        // Group items using EnumMap for efficiency based on the Category enum
        Map<Category, List<Map.Entry<MenuItem, Integer>>> grouped = new EnumMap<>(Category.class);
        for (Category category : Category.values()) {
            grouped.put(category, new ArrayList<>());
        }

        for (Map.Entry<MenuItem, Integer> entry : countMap.entrySet()) {
            grouped.get(entry.getKey().getCategory()).add(entry);
        }

        // Print items categorized and sorted by their ID
        for (Category category : Category.values()) {
            report.append(lineSeparator).append("Category: ").append(category).append(lineSeparator);
            List<Map.Entry<MenuItem, Integer>> entries = grouped.get(category);
            entries.sort(Comparator.comparing(entry -> entry.getKey().getId()));

            if (entries.isEmpty()) {
                report.append("  (no items)").append(lineSeparator);
                continue;
            }

            for (Map.Entry<MenuItem, Integer> entry : entries) {
                MenuItem item = entry.getKey();
                int count = entry.getValue();
                report.append(String.format(Locale.ROOT, "  %s | %s | %.2f | %s | count=%d",
                                item.getId(), item.getName(), item.getPrice(), item.getCategory(), count))
                      .append(lineSeparator);
            }
        }

        // Generate Best-selling Items ranking (Top 5, including ties)
        report.append(lineSeparator).append("Best-selling Items (Top 5 with ties)").append(lineSeparator);
        List<Map.Entry<MenuItem, Integer>> ranking = new ArrayList<>(countMap.entrySet());
        
        // Sort in descending order of sales count, then by item ID
        ranking.sort(Comparator.<Map.Entry<MenuItem, Integer>>comparingInt(Map.Entry::getValue)
                        .reversed()
                        .thenComparing(entry -> entry.getKey().getId()));

        if (ranking.isEmpty()) {
            report.append("  (no items)").append(lineSeparator);
            return report.toString();
        }

        // Determine the cutoff sales count for the top 5th item to handle ties
        int cutoffCount = ranking.size() <= 5 ? ranking.get(ranking.size() - 1).getValue() : ranking.get(4).getValue();

        int rank = 0;
        int previousCount = Integer.MIN_VALUE;
        int seen = 0;
        for (Map.Entry<MenuItem, Integer> entry : ranking) {
            int count = entry.getValue();
            if (count < cutoffCount) {
                break; // Stop processing once we drop below the Top 5 count threshold
            }

            if (count != previousCount) {
                rank = seen + 1;
                previousCount = count;
            }
            seen++;

            MenuItem item = entry.getKey();
            report.append(String.format(Locale.ROOT, "  %d) %s | %s | count=%d",
                                rank, item.getId(), item.getName(), count))
                  .append(lineSeparator);
        }

        return report.toString();
    }

    /**
     * Generates a sales summary report detailing total orders and revenue broken down by date.
     */
    public String generateSalesReport(List<Order> orders) {
        if (orders == null) {
            throw new IllegalArgumentException("orders must not be null.");
        }

        String lineSeparator = System.lineSeparator();
        StringBuilder report = new StringBuilder();
        report.append("Sales Report").append(lineSeparator);
        report.append("============").append(lineSeparator);
        report.append("Total orders: ").append(orders.size()).append(lineSeparator);

        double totalSales = 0.0;
        // Use TreeMap to automatically sort sales by date
        Map<LocalDate, Double> salesByDate = new TreeMap<>();
        for (Order order : orders) {
            if (order == null) continue;

            double total = order.getTotal();
            totalSales += total;
            LocalDate date = order.getDateTime().toLocalDate();
            // Merge total revenue for the specific date
            salesByDate.merge(date, total, Double::sum);
        }

        report.append(String.format(Locale.ROOT, "Total sales: %.2f", totalSales)).append(lineSeparator);
        report.append("Sales by date:").append(lineSeparator);
        
        if (salesByDate.isEmpty()) {
            report.append("  (no sales)").append(lineSeparator);
        } else {
            for (Map.Entry<LocalDate, Double> entry : salesByDate.entrySet()) {
                report.append(String.format(Locale.ROOT, "  %s -> %.2f", entry.getKey(), entry.getValue()))
                      .append(lineSeparator);
            }
        }

        return report.toString();
    }
}