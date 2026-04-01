package coffeeshop.simulation.model;

public enum SimulationSpeedMode {
    HALF_X("0.5x"),
    ONE_X("1x"),
    TWO_X("2x"),
    FOUR_X("4x");

    private final String displayText;

    SimulationSpeedMode(String displayText) {
        this.displayText = displayText;
    }

    public String displayText() {
        return displayText;
    }

    @Override
    public String toString() {
        return displayText;
    }
}
