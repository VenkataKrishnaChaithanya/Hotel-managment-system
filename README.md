# Grand Hotel Manager — Maven Project

A JavaFX-based Hotel Property Management System, fully structured as a Maven project.

---

## Prerequisites

| Tool    | Minimum Version |
|---------|----------------|
| Java    | 17             |
| Maven   | 3.8+           |

---

## Project Structure

```
grand-hotel-manager/
├── pom.xml                                         ← Maven build descriptor
└── src/
    ├── main/
    │   └── java/
    │       └── com/grandhotel/
    │           └── Main.java                       ← Application source
    └── test/
        └── java/
            └── com/grandhotel/
                └── HotelModelTest.java             ← JUnit 5 unit tests
```

---

## Maven Commands

### Run the application
```bash
mvn javafx:run
```

### Compile only
```bash
mvn compile
```

### Run unit tests
```bash
mvn test
```

### Package into a fat JAR (includes all dependencies)
```bash
mvn package
```
Output: `target/grand-hotel-manager-1.0.0.jar`

### Run the fat JAR directly
```bash
java -jar target/grand-hotel-manager-1.0.0.jar
```

### Clean build artifacts
```bash
mvn clean
```

### Clean + package in one step
```bash
mvn clean package
```

---

## Maven Plugins Used

| Plugin                    | Purpose                                          |
|---------------------------|--------------------------------------------------|
| `maven-compiler-plugin`   | Enforces Java 17 source/target compatibility     |
| `javafx-maven-plugin`     | Enables `mvn javafx:run` for easy development    |
| `maven-surefire-plugin`   | Discovers and runs JUnit 5 tests with `mvn test` |
| `maven-shade-plugin`      | Bundles everything into a single runnable JAR    |
| `maven-resources-plugin`  | Copies non-Java assets (CSS, icons) to output    |

---

## Dependencies

| Library            | Version  | Purpose                            |
|--------------------|----------|------------------------------------|
| `javafx-controls`  | 21.0.2   | UI controls (TableView, Button…)   |
| `javafx-fxml`      | 21.0.2   | FXML support (future use)          |
| `javafx-base`      | 21.0.2   | Observable collections, properties |
| `javafx-graphics`  | 21.0.2   | Scene graph, rendering             |
| `junit-jupiter`    | 5.10.2   | Unit testing (test scope only)     |

---

## Data Files

The app auto-saves data to CSV files in the working directory:

- `hotel_rooms.csv` — room inventory
- `hotel_customers.csv` — current guests

These are created automatically on first run.
