package coffeeshop.simulation.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import coffeeshop.model.Category;
import coffeeshop.model.InvalidMenuItemDataException;
import coffeeshop.model.MenuItem;
import coffeeshop.model.Order;
import java.time.LocalDateTime;
import org.junit.jupiter.api.Test;

class OrderQueueManagerPriorityTest {

    @Test
    void onlineQueueShouldBeServedBeforeRegularQueue() throws Exception {
        OrderQueueManager manager = new OrderQueueManager();
        Order regularOrder = createOrder("CUST-001");
        Order onlineOrder = createOrder("ONLINE-CUST-001");

        manager.enqueueRegular(regularOrder);
        manager.enqueueOnline(onlineOrder);

        QueuedOrder first = manager.takeNextOrder();
        QueuedOrder second = manager.takeNextOrder();

        assertNotNull(first);
        assertNotNull(second);
        assertEquals(QueueType.ONLINE, first.queueType());
        assertEquals("ONLINE-CUST-001", first.order().getOrderId());
        assertEquals(QueueType.REGULAR, second.queueType());
        assertEquals("CUST-001", second.order().getOrderId());
    }

    private static Order createOrder(String orderId) throws InvalidMenuItemDataException {
        Order order = new Order(orderId, LocalDateTime.parse("2026-03-03T10:00:00"));
        order.addLine(new MenuItem("DRINK-001", "Americano", 2.80, Category.DRINK), 1, false);
        return order;
    }
}
