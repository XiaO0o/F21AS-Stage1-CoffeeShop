package coffeeshop.simulation;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;

import org.junit.jupiter.api.Test;

class SimulationLoggerTest {

    @Test
    void getInstanceShouldReturnSameSingletonInstance() {
        SimulationLogger logger1 = SimulationLogger.getInstance();
        SimulationLogger logger2 = SimulationLogger.getInstance();
        assertSame(logger1, logger2);
    }

    @Test
    void logShouldStoreMessagesInOrder() {
        SimulationLogger logger = SimulationLogger.getInstance();
        logger.clear();

        logger.log("first message");
        logger.log("second message");

        assertEquals(2, logger.getEntriesSnapshot().size());
    }
}
