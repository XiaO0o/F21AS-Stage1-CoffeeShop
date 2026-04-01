# Stage 2 Development Notes (Final)

## Sprint Summary

- **Sprint 1:** producer-consumer multithreading core (`wait/notifyAll`, graceful stop, file logger).
- **Sprint 2:** strict MVC + Observer GUI structure (`SimulationModel` + `SimulationController` + Swing views).
- **Sprint 3:** runtime speed strategy, online priority queue, dynamic add/remove staff, richer staff states.

## Agile Evidence

- Delivered in 3 incremental sprints.
- Kept a working baseline and test pass after each sprint.
- Added feature-focused tests before final integration checks.

## Pattern Evidence

- **Singleton:** `coffeeshop.simulation.SimulationLogger`
- **Observer:** `Subject`, `SimulationObserver`, `SimulationModel`, `SimulationFrame`
- **MVC:** `simulation.model.*`, `simulation.view.*`, `simulation.controller.SimulationController`
- **Strategy:** `ProcessingTimeStrategy` + speed strategy implementations used by `SimulationEngine`

## Thread Synchronization

- Queue uses plain `LinkedList` with `synchronized`, `wait()`, `notifyAll()`.
- Workers support graceful stop; no unsafe thread termination APIs.
- Engine monitor waits for producer completion and worker count to reach zero.

## Anti-patterns Avoided

- Blob class (responsibilities split by model/controller/view/engine).
- Busy waiting (blocking wait instead of polling loops).
- Magic numbers (constants/enums/strategies).
- Silent failure (resource and I/O errors are explicit and logged).

## Jar + Resource Notes

- Executable jar main class: `coffeeshop.app.Stage2SimulationGuiApp`
- Bundled default resources:
  - `src/main/resources/data/menu.csv`
  - `src/main/resources/data/orders.csv`
- Unified loader:
  - `InputResourceHelper`
  - `CsvFileReader.readAllLines(String)`
  - `CsvLoader.loadMenu(String)` / `CsvLoader.loadOrders(String, Menu)`
