package coffeeshop.model;

public class OrderLine {

    private final MenuItem item;
    private final int quantity;
    private final boolean hasOwnCup;

    public OrderLine(MenuItem item, int quantity, boolean hasOwnCup) {
        if (item == null) {
            throw new IllegalArgumentException("MenuItem must not be null.");
        }
        if (quantity < 1) {
            throw new IllegalArgumentException("Quantity must be at least 1.");
        }

        this.item = item;
        this.quantity = quantity;
        this.hasOwnCup = hasOwnCup;
    }

    public double getSubtotal() {
        return item.getPrice() * quantity;
    }

    MenuItem getItem() {
        return item;
    }

    int getQuantity() {
        return quantity;
    }

    boolean isOwnCupDrinkLine() {
        return item.getCategory() == Category.DRINK && hasOwnCup;
    }
}
