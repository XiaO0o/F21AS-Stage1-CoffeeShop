package coffeeshop.simulation.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import coffeeshop.model.Category;
import coffeeshop.model.InvalidMenuItemDataException;
import coffeeshop.model.MenuItem;
import coffeeshop.model.Order;
import java.time.LocalDateTime;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import org.junit.jupiter.api.Test;

class OrderQueueManagerTest {

    @Test
    void enqueueThenTakeShouldReturnHeadOrder() throws InvalidMenuItemDataException, InterruptedException {
        OrderQueueManager manager = new OrderQueueManager();
        Order order = createOrder("CUST-001");

        manager.enqueueRegular(order);

        QueuedOrder taken = manager.takeNextOrder();
        assertEquals(order.getOrderId(), taken.order().getOrderId());
        assertEquals(0, manager.snapshotQueuedOrders().size());
        assertFalse(manager.isClosedAndEmpty());
    }

    @Test
    void takeShouldReturnNullAfterProducerFinishedWhenQueueIsEmpty() throws Exception {
        OrderQueueManager manager = new OrderQueueManager();
        CountDownLatch started = new CountDownLatch(1);
        AtomicReference<QueuedOrder> result = new AtomicReference<>();

        Thread taker =
                new Thread(
                        () -> {
                            started.countDown();
                            try {
                                result.set(manager.takeNextOrder());
                            } catch (InterruptedException exception) {
                                Thread.currentThread().interrupt();
                                fail("Unexpected interruption in test thread.");
                            }
                        });
        taker.start();

        assertTrue(started.await(1, TimeUnit.SECONDS));
        Thread.sleep(50L);
        assertTrue(taker.isAlive());

        manager.markProducerFinished();

        taker.join(1_000L);
        assertFalse(taker.isAlive());
        assertNull(result.get());
        assertTrue(manager.isClosedAndEmpty());
    }

    private static Order createOrder(String orderId) throws InvalidMenuItemDataException {
        Order order = new Order(orderId, LocalDateTime.parse("2026-03-02T10:00:00"));
        order.addLine(new MenuItem("DRINK-001", "Americano", 2.80, Category.DRINK), 1, false);
        return order;
    }
}
