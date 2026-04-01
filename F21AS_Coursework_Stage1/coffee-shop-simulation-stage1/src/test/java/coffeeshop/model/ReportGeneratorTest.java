package coffeeshop.model;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.Test;

class ReportGeneratorTest {

    @Test
    void generateItemReportShouldIncludeGroupedItemsAndRanking()
            throws InvalidMenuItemDataException {
        MenuItem drink001 = new MenuItem("DRINK-001", "Americano", 2.80, Category.DRINK);
        MenuItem drink002 = new MenuItem("DRINK-002", "Latte", 3.80, Category.DRINK);
        MenuItem food001 = new MenuItem("FOOD-001", "Sandwich", 5.00, Category.FOOD);
        MenuItem snack001 = new MenuItem("SNACK-001", "Cookie", 1.50, Category.SNACK);
        MenuItem snack002 = new MenuItem("SNACK-002", "Brownie", 2.20, Category.SNACK);
        MenuItem food002 = new MenuItem("FOOD-002", "Bagel", 4.00, Category.FOOD);

        Order order = new Order("CUST-001", LocalDateTime.parse("2026-02-01T10:00:00"));
        order.addLine(food001, 3, false);
        order.addLine(drink001, 2, true);
        order.addLine(drink002, 2, false);
        order.addLine(snack001, 2, false);
        order.addLine(snack002, 1, false);
        order.addLine(food002, 1, false);

        OrderStatistics statistics = new OrderStatistics();
        statistics.recordOrder(order);

        ReportGenerator generator = new ReportGenerator();
        String report = generator.generateItemReport(statistics);

        assertTrue(report.contains("Category: DRINK"));
        assertTrue(report.contains("Category: FOOD"));
        assertTrue(report.contains("Category: SNACK"));
        assertTrue(report.contains("DRINK-001 | Americano | 2.80 | DRINK | count=2"));
        assertTrue(report.contains("FOOD-001 | Sandwich | 5.00 | FOOD | count=3"));
        assertTrue(report.contains("Best-selling Items (Top 5 with ties)"));
        assertTrue(report.contains("1) FOOD-001 | Sandwich | count=3"));
    }

    @Test
    void generateSalesReportShouldIncludeOrderCountTotalAndByDate()
            throws InvalidMenuItemDataException {
        MenuItem drink = new MenuItem("DRINK-001", "Latte", 6.00, Category.DRINK);
        MenuItem food = new MenuItem("FOOD-001", "Wrap", 10.00, Category.FOOD);

        Order order1 = new Order("CUST-001", LocalDateTime.parse("2026-02-01T10:15:30"));
        order1.addLine(food, 1, false); // total 10.00

        Order order2 = new Order("CUST-002", LocalDateTime.parse("2026-02-02T11:15:30"));
        order2.addLine(drink, 1, true);
        order2.addLine(food, 1, false); // subtotal 16.00, total 13.50

        ReportGenerator generator = new ReportGenerator();
        String report = generator.generateSalesReport(List.of(order1, order2));

        assertTrue(report.contains("Total orders: 2"));
        assertTrue(report.contains("Total sales: 23.50"));
        assertTrue(report.contains("2026-02-01 -> 10.00"));
        assertTrue(report.contains("2026-02-02 -> 13.50"));
    }
}
