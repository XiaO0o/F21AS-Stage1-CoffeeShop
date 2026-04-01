# Coffee Shop Simulation

Java 17 + Maven coursework project covering:

- `Stage 1`: menu, order, discount, CSV loading, report generation, basic GUI
- `Stage 2`: multithreaded coffee shop simulation with Swing GUI, MVC, Observer, Strategy, graceful shutdown, and executable jars

## Build

Run tests:

```bash
mvn test
```

Package executable jars:

```bash
mvn clean package
```

Generated jars:

- `target/coffee-shop-simulation-stage1-1.0-SNAPSHOT-stage1.jar`
- `target/coffee-shop-simulation-stage1-1.0-SNAPSHOT-stage2.jar`

For Windows users, these are also the executable jar files you can open directly by double-clicking:

- `target/coffee-shop-simulation-stage1-1.0-SNAPSHOT-stage1.jar` (Stage 1)
- `target/coffee-shop-simulation-stage1-1.0-SNAPSHOT-stage2.jar` (Stage 2)

## Run Stage 1

Stage 1 executable jar:

```bash
java -jar target/coffee-shop-simulation-stage1-1.0-SNAPSHOT-stage1.jar
```

## Run Stage 2

Stage 2 GUI jar:

```bash
java -jar target/coffee-shop-simulation-stage1-1.0-SNAPSHOT-stage2.jar
```

You can also run Stage 2 from Maven:

```bash
mvn -DskipTests compile exec:java
```

## Stage 2 Default Behaviour

By default, Stage 2 loads bundled classpath resources:

- `src/main/resources/data/menu.csv`
- `src/main/resources/data/orders.csv`

The GUI starts with:

- menu source: `classpath:data/menu.csv`
- orders source: `classpath:data/orders.csv`
- initial staff count: `2`
- order arrival delay: `300 ms`
- speed: `1x`

## Stage 2 Custom Inputs

In the GUI, you can change:

- menu CSV source
- orders CSV source
- initial staff count
- order arrival delay
- speed mode

Stage 2 also supports headless/CLI execution. Arguments are:

```text
menuSource ordersSource staffCount producerDelayMs logPath reportPath speedMode
```

Example:

```bash
java "-Djava.awt.headless=true" -jar target/coffee-shop-simulation-stage1-1.0-SNAPSHOT-stage2.jar classpath:data/menu.csv classpath:data/orders.csv 2 300 data/simulation.log data/report.txt 1x
```

Accepted speed values:

- `0.5x`
- `1x`
- `2x`
- `4x`

## Stage 2 Output Files

When the simulation finishes, it writes:

- `data/simulation.log`
- `data/report.txt`

The program creates the output directory automatically if needed.

## Notes

- Closing the Stage 2 window triggers graceful shutdown rather than immediate forced stop.
- `online` orders are prioritised over regular waiting-queue orders.
- If you want a longer simulation, edit `src/main/resources/data/orders.csv` and rebuild the jar.
- Development summary for Stage 2 is in `docs/stage2-development-notes.md`.
