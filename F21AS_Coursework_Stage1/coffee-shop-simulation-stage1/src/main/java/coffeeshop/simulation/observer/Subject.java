package coffeeshop.simulation.observer;

public interface Subject {
    void registerObserver(SimulationObserver observer);

    void removeObserver(SimulationObserver observer);

    void notifyObservers();
}
