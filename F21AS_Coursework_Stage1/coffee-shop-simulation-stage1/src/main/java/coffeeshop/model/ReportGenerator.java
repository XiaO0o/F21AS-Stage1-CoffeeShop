package coffeeshop.model;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TreeMap;

public class ReportGenerator {

    public String generateItemReport(OrderStatistics statistics) {
        if (statistics == null) {
            throw new IllegalArgumentException("statistics must not be null.");
        }

        // Item report has grouped menu items, counts, and top sellers.
        Map<MenuItem, Integer> countMap = statistics.getCountMapView();
        String lineSeparator = System.lineSeparator();
        StringBuilder report = new StringBuilder();
        report.append("Item Report").append(lineSeparator);
        report.append("===========").append(lineSeparator);

        Map<Category, List<Map.Entry<MenuItem, Integer>>> grouped =
                new EnumMap<>(Category.class);
        for (Category category : Category.values()) {
            grouped.put(category, new ArrayList<>());
        }

        for (Map.Entry<MenuItem, Integer> entry : countMap.entrySet()) {
            grouped.get(entry.getKey().getCategory()).add(entry);
        }

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
                report.append(
                                String.format(
                                        Locale.ROOT,
                                        "  %s | %s | %.2f | %s | count=%d",
                                        item.getId(),
                                        item.getName(),
                                        item.getPrice(),
                                        item.getCategory(),
                                        count))
                        .append(lineSeparator);
            }
        }

        report.append(lineSeparator).append("Best-selling Items (Top 5 with ties)").append(lineSeparator);
        List<Map.Entry<MenuItem, Integer>> ranking = new ArrayList<>(countMap.entrySet());
        ranking.sort(
                Comparator.<Map.Entry<MenuItem, Integer>>comparingInt(Map.Entry::getValue)
                        .reversed()
                        .thenComparing(entry -> entry.getKey().getId()));

        if (ranking.isEmpty()) {
            report.append("  (no items)").append(lineSeparator);
            return report.toString();
        }

        int cutoffCount =
                ranking.size() <= 5 ? ranking.get(ranking.size() - 1).getValue() : ranking.get(4).getValue();

        int rank = 0;
        int previousCount = Integer.MIN_VALUE;
        int seen = 0;
        for (Map.Entry<MenuItem, Integer> entry : ranking) {
            int count = entry.getValue();
            if (count < cutoffCount) {
                break;
            }

            if (count != previousCount) {
                rank = seen + 1;
                previousCount = count;
            }
            seen++;

            MenuItem item = entry.getKey();
            report.append(
                            String.format(
                                    Locale.ROOT,
                                    "  %d) %s | %s | count=%d",
                                    rank,
                                    item.getId(),
                                    item.getName(),
                                    count))
                    .append(lineSeparator);
        }

        return report.toString();
    }

    public String generateSalesReport(List<Order> orders) {
        if (orders == null) {
            throw new IllegalArgumentException("orders must not be null.");
        }

        // Sales report shows total orders, total sales, and daily totals.
        String lineSeparator = System.lineSeparator();
        StringBuilder report = new StringBuilder();
        report.append("Sales Report").append(lineSeparator);
        report.append("============").append(lineSeparator);
        report.append("Total orders: ").append(orders.size()).append(lineSeparator);

        double totalSales = 0.0;
        Map<LocalDate, Double> salesByDate = new TreeMap<>();
        for (Order order : orders) {
            if (order == null) {
                continue;
            }

            double total = order.getTotal();
            totalSales += total;
            LocalDate date = order.getDateTime().toLocalDate();
            salesByDate.merge(date, total, Double::sum);
        }

        report.append(String.format(Locale.ROOT, "Total sales: %.2f", totalSales)).append(lineSeparator);
        report.append("Sales by date:").append(lineSeparator);
        if (salesByDate.isEmpty()) {
            report.append("  (no sales)").append(lineSeparator);
        } else {
            for (Map.Entry<LocalDate, Double> entry : salesByDate.entrySet()) {
                report.append(
                                String.format(
                                        Locale.ROOT,
                                        "  %s -> %.2f",
                                        entry.getKey(),
                                        entry.getValue()))
                        .append(lineSeparator);
            }
        }

        return report.toString();
    }
}
