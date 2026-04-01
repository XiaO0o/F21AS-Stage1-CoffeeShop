package coffeeshop.simulation.model;

import java.util.Locale;

public enum QueueType {
    REGULAR,
    ONLINE;

    public String toDisplayText() {
        return name().toLowerCase(Locale.ROOT);
    }
}
