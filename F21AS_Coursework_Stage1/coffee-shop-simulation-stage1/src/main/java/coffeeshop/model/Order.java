package coffeeshop.model;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Order {

    public static final double RULE_A_DISCOUNT_PER_UNIT = 1.0;
    public static final double RULE_B_THRESHOLD = 15.0;
    public static final double RULE_B_RATE = 0.10;

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
        double discount = eligibleDrinkUnits * RULE_A_DISCOUNT_PER_UNIT;
        // Never return a discount bigger than the subtotal.
        return Math.min(discount, subtotal);
    }

    public double applyRuleB() {
        double subtotal = getSubtotal();
        if (subtotal < RULE_B_THRESHOLD) {
            return 0.0;
        }

        // Rule B uses the amount left after Rule A.
        double base = Math.max(0.0, subtotal - applyRuleA());
        return RULE_B_RATE * base;
    }

    public double getTotal() {
        double total = getSubtotal() - applyRuleA() - applyRuleB();
        return Math.max(0.0, total);
    }

    public int getTotalItemCount() {
        int totalItemCount = 0;
        for (OrderLine line : lines) {
            totalItemCount += line.getQuantity();
        }
        return totalItemCount;
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

    public String getOrderId() {
        return orderId;
    }

    public List<OrderLine> getLinesView() {
        return Collections.unmodifiableList(lines);
    }

    public LocalDateTime getDateTime() {
        return dateTime;
    }
}
