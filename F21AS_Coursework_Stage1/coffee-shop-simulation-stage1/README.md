# Coffee Shop Simulation Stage 1

Java 17+ Maven project for Coffee Shop Simulation Stage 1.

## Prerequisites

- JDK 17+
- Maven 3.9+

## Data files

- Default menu file: `data/menu.csv`
- Default orders file: `data/orders.csv`
- Both files include valid rows and intentionally invalid rows.
- Invalid rows are skipped and logged to `System.err`.

## Run tests

```bash
mvn test
```

## Run application

For a new device (or after cleaning `target/`), run compile first:

```bash
mvn -DskipTests compile exec:java
```

Compiled runnable JAR location: `target/coffee-shop-simulation-stage1-1.0-SNAPSHOT.jar`.

The app reads `data/menu.csv` and `data/orders.csv` by default.
Path resolution behavior:
- First try current working directory.
- If not found, fallback to the application/JAR location.

You can also run the packaged JAR directly:

```bash
mvn -DskipTests package
java -jar target/coffee-shop-simulation-stage1-1.0-SNAPSHOT.jar
```

Example startup output:

```text
CoffeeShopApp started
Loaded menu item count: 8
Loaded order count: 4
Total sales: 43.14
```

## GUI workflow

1. Enter `Customer ID` and click `New Order`.
2. Select one or more items from the left list.
3. Set `Quantity` and click `Add Selected Items`.
4. `Bill Details` updates automatically after each add (line subtotals, subtotal, RuleA, RuleB, total).
5. `Has Own Cup` is only available for `DRINK` items. When `FOOD` or `SNACK` is selected, it is disabled.
6. Click `Complete Order` to save the order and update statistics.
7. Click `Exit` to write `data/report.txt` and close the app.

## Report output

- Generated file: `data/report.txt` (same directory as the resolved menu CSV)
- Contains:
  - item list and item counts
  - best-selling ranking
  - total order count and total sales

## Run with custom CSV paths

```bash
mvn -DskipTests compile exec:java -Dexec.args="data/menu.csv data/orders.csv"
```
