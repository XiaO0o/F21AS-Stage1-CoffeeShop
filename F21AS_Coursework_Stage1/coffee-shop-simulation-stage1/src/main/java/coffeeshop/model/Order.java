package coffeeshop.model;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Represents a customer order, containing multiple order lines.
 * Handles the calculation of subtotals and the application of discount rules.
 */
public class Order {

    private final String orderId;
    private final List<OrderLine> lines;
    private final LocalDateTime dateTime;

    /**
     * Constructs a new Order.
     * * @param orderId  The unique identifier for the customer/order.
     * @param dateTime The timestamp when the order was created.
     */
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

    /**
     * Adds a new line item to the order.
     */
    public void addLine(MenuItem item, int qty, boolean hasCup) {
        lines.add(new OrderLine(item, qty, hasCup));
    }

    /**
     * Calculates the subtotal of the order before any discounts.
     */
    public double getSubtotal() {
        double subtotal = 0.0;
        for (OrderLine line : lines) {
            subtotal += line.getSubtotal();
        }
        return subtotal;
    }

    /**
     * Applies Rule A: £1.00 discount per eligible drink (Category.DRINK with own cup).
     * The total discount cannot exceed the order subtotal.
     */
    public double applyRuleA() {
        double subtotal = getSubtotal();
        int eligibleDrinkUnits = countEligibleDrinkUnitsForRuleA();
        
        // Rule A gives £1 off per eligible drink unit.
        double discount = eligibleDrinkUnits;
        
        // Never return a discount bigger than the subtotal to prevent negative bills.
        return Math.min(discount, subtotal);
    }

    /**
     * Applies Rule B: 10% discount on the remaining amount if the initial subtotal >= £15.00.
     * This rule is applied AFTER Rule A deductions.
     */
    public double applyRuleB() {
        double subtotal = getSubtotal();
        if (subtotal < 15.0) {
            return 0.0; // Rule B threshold not met
        }

        // Rule B uses the amount left after Rule A is applied.
        double base = Math.max(0.0, subtotal - applyRuleA());
        return 0.10 * base;
    }

    /**
     * Calculates the final total amount after applying all discount rules.
     */
    public double getTotal() {
        double total = getSubtotal() - applyRuleA() - applyRuleB();
        return Math.max(0.0, total); // Ensure the final total is never negative
    }

    /**
     * Helper method to count the total quantity of drinks using a reusable cup.
     */
    private int countEligibleDrinkUnitsForRuleA() {
        int eligibleUnits = 0;
        for (OrderLine line : lines) {
            if (line.isOwnCupDrinkLine()) {
                eligibleUnits += line.getQuantity();
            }
        }
        return eligibleUnits;
    }

    /**
     * Returns an unmodifiable view of the order lines to maintain encapsulation.
     */
    List<OrderLine> getLinesView() {
        return Collections.unmodifiableList(lines);
    }

    LocalDateTime getDateTime() {
        return dateTime;
    }
}