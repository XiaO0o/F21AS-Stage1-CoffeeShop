package coffeeshop.model;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Order {

    private final String orderId;
    private final List<OrderLine> lines;
    private final LocalDateTime dateTime;

    public Order(String orderId, LocalDateTime dateTime) {
        if (orderId == null || orderId.isBlank()) {
            throw new IllegalArgumentException("Order id must not be null or blank.");
        }
        if (dateTime == null) {
            throw new IllegalArgumentException("Order dateTime must not be null.");
        }

        this.orderId = orderId;
        this.dateTime = dateTime;
        this.lines = new ArrayList<>();
    }

    public void addLine(MenuItem item, int qty, boolean hasCup) {
        lines.add(new OrderLine(item, qty, hasCup));
    }

    public double getSubtotal() {
        double subtotal = 0.0;
        for (OrderLine line : lines) {
            subtotal += line.getSubtotal();
        }
        return subtotal;
    }

    public double applyRuleA() {
        double subtotal = getSubtotal();
        int eligibleDrinkUnits = countEligibleDrinkUnitsForRuleA();
        // Rule A gives 1 pound off per eligible drink unit.
        double discount = eligibleDrinkUnits;
        // Never return a discount bigger than the subtotal.
        return Math.min(discount, subtotal);
    }

    public double applyRuleB() {
        double subtotal = getSubtotal();
        if (subtotal < 15.0) {
            return 0.0;
        }

        // Rule B uses the amount left after Rule A.
        double base = Math.max(0.0, subtotal - applyRuleA());
        return 0.10 * base;
    }

    public double getTotal() {
        double total = getSubtotal() - applyRuleA() - applyRuleB();
        return Math.max(0.0, total);
    }

    private int countEligibleDrinkUnitsForRuleA() {
        int eligibleUnits = 0;
        for (OrderLine line : lines) {
            if (line.isOwnCupDrinkLine()) {
                eligibleUnits += line.getQuantity();
            }
        }
        return eligibleUnits;
    }

    List<OrderLine> getLinesView() {
        return Collections.unmodifiableList(lines);
    }

    LocalDateTime getDateTime() {
        return dateTime;
    }
}
