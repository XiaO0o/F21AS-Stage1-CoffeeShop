package coffeeshop.model;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.time.LocalDateTime;
import org.junit.jupiter.api.Test;

class OrderTest {

    private static final double DELTA = 1e-9;

    @Test
    void ruleAShouldCountQuantityForOwnCupDrinks() throws InvalidMenuItemDataException {
        Order order = new Order("ORD-001", LocalDateTime.now());
        order.addLine(new MenuItem("DRINK-001", "Americano", 2.50, Category.DRINK), 2, true);

        assertEquals(5.00, order.getSubtotal(), DELTA);
        assertEquals(2.00, order.applyRuleA(), DELTA);
        assertEquals(0.00, order.applyRuleB(), DELTA);
        assertEquals(3.00, order.getTotal(), DELTA);
    }

    @Test
    void ruleAShouldBeZeroWhenOwnCupIsFalse() throws InvalidMenuItemDataException {
        Order order = new Order("ORD-002", LocalDateTime.now());
        order.addLine(new MenuItem("DRINK-001", "Americano", 2.50, Category.DRINK), 2, false);

        assertEquals(5.00, order.getSubtotal(), DELTA);
        assertEquals(0.00, order.applyRuleA(), DELTA);
        assertEquals(0.00, order.applyRuleB(), DELTA);
        assertEquals(5.00, order.getTotal(), DELTA);
    }

    @Test
    void ruleAShouldAccumulateAcrossMultipleDrinkLines() throws InvalidMenuItemDataException {
        Order order = new Order("ORD-003", LocalDateTime.now());
        order.addLine(new MenuItem("DRINK-001", "Americano", 2.50, Category.DRINK), 1, true);
        order.addLine(new MenuItem("DRINK-002", "Latte", 3.80, Category.DRINK), 3, true);

        assertEquals(13.90, order.getSubtotal(), DELTA);
        assertEquals(4.00, order.applyRuleA(), DELTA);
        assertEquals(0.00, order.applyRuleB(), DELTA);
        assertEquals(9.90, order.getTotal(), DELTA);
    }

    @Test
    void shouldApplyOnlyRuleBWhenSubtotalIsExactlyFifteen() throws InvalidMenuItemDataException {
        Order order = new Order("ORD-004", LocalDateTime.now());
        order.addLine(new MenuItem("FOOD-001", "Sandwich", 10.00, Category.FOOD), 1, false);
        order.addLine(new MenuItem("SNACK-001", "Brownie", 5.00, Category.SNACK), 1, false);

        assertEquals(15.00, order.getSubtotal(), DELTA);
        assertEquals(0.00, order.applyRuleA(), DELTA);
        assertEquals(1.50, order.applyRuleB(), DELTA);
        assertEquals(13.50, order.getTotal(), DELTA);
    }

    @Test
    void shouldApplyRuleAAndRuleBTogether() throws InvalidMenuItemDataException {
        Order order = new Order("ORD-005", LocalDateTime.now());
        order.addLine(new MenuItem("DRINK-001", "Latte", 6.00, Category.DRINK), 2, true);
        order.addLine(new MenuItem("FOOD-001", "Wrap", 5.00, Category.FOOD), 1, false);

        assertEquals(17.00, order.getSubtotal(), DELTA);
        assertEquals(2.00, order.applyRuleA(), DELTA);
        assertEquals(1.50, order.applyRuleB(), DELTA);
        assertEquals(13.50, order.getTotal(), DELTA);
    }

    @Test
    void shouldNotApplyRuleBWhenSubtotalIsBelowFifteen() throws InvalidMenuItemDataException {
        Order order = new Order("ORD-006", LocalDateTime.now());
        order.addLine(new MenuItem("DRINK-001", "Mocha", 7.00, Category.DRINK), 2, false);

        assertEquals(14.00, order.getSubtotal(), DELTA);
        assertEquals(0.00, order.applyRuleA(), DELTA);
        assertEquals(0.00, order.applyRuleB(), DELTA);
        assertEquals(14.00, order.getTotal(), DELTA);
    }

    @Test
    void shouldReturnZeroSubtotalAndTotalForEmptyOrder() {
        Order order = new Order("ORD-007", LocalDateTime.now());

        assertEquals(0.00, order.getSubtotal(), DELTA);
        assertEquals(0.00, order.applyRuleA(), DELTA);
        assertEquals(0.00, order.applyRuleB(), DELTA);
        assertEquals(0.00, order.getTotal(), DELTA);
    }

    @Test
    void ruleAShouldBeCappedBySubtotal() throws InvalidMenuItemDataException {
        Order order = new Order("ORD-008", LocalDateTime.now());
        order.addLine(new MenuItem("DRINK-001", "Espresso", 0.20, Category.DRINK), 3, true);

        assertEquals(0.60, order.getSubtotal(), DELTA);
        assertEquals(0.60, order.applyRuleA(), DELTA);
        assertEquals(0.00, order.getTotal(), DELTA);
    }
}
