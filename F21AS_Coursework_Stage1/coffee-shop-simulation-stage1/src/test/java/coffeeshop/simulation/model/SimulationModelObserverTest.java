package coffeeshop.simulation.model;

import static org.junit.jupiter.api.Assertions.assertEquals;

import coffeeshop.simulation.observer.SimulationObserver;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.Test;

class SimulationModelObserverTest {

    @Test
    void registerAndRemoveObserverShouldControlNotifications() {
        SimulationModel model = new SimulationModel();
        AtomicInteger updateCount = new AtomicInteger(0);
        SimulationObserver observer = updateCount::incrementAndGet;

        model.registerObserver(observer);
        model.appendLog("event-1");
        assertEquals(1, updateCount.get());

        model.removeObserver(observer);
        model.appendLog("event-2");
        assertEquals(1, updateCount.get());
    }
}
