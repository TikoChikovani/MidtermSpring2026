# UNO CLI

A standalone Java UNO-like command-line game.

This project is configured as a Maven application. Maven compiles the game,
runs the characterization and persistence tests, packages an executable JAR,
and can launch the CLI without manual classpath setup.

## Requirements

- Java 17 or newer
- Maven 3.9 or newer
- Docker, only for the Docker commands

## Local Build

```bash
mvn compile
```

## Local Test

```bash
mvn test
```

The Maven test phase runs the existing characterization test suite through
JUnit and runs MyBatis repository tests against isolated in-memory H2 databases.

## Local Run

Run one bot game:

```bash
mvn exec:java -Dexec.args="--bots 3 --games 1"
```

Run five quiet bot games with a deterministic seed:

```bash
mvn exec:java -Dexec.args="--bots 3 --games 5 --quiet --seed 42"
```

Run a multi-round game until a target score:

```bash
mvn exec:java -Dexec.args="--bots 3 --target-score 500"
```

Run an interactive game:

```bash
mvn exec:java -Dexec.args="--human --bots 2 --games 1"
```

Card input examples:

```text
R5   red 5
YS   yellow skip
BR   blue reverse
G+2  green draw two
W    wild
W4   wild draw four
draw draw a card
R5 UNO   play red 5 and call UNO
0 UNO    play card index 0 and call UNO
```

Completed games are persisted to H2 automatically.

## Package Creation

```bash
mvn package
```

The packaged application is created at:

```text
target/uno-cli-1.0.0.jar
```

Run the packaged JAR:

```bash
java -jar target/uno-cli-1.0.0.jar --bots 3 --games 1
```

## Docker Build

```bash
docker build -t uno-cli .
```

## Docker Run

Run the default bot game:

```bash
docker run --rm uno-cli
```

Pass game arguments:

```bash
docker run --rm uno-cli --bots 3 --games 5 --quiet --seed 42
```

Run an interactive game:

```bash
docker run --rm -it uno-cli --human --bots 2 --games 1
```

Persist Docker history on the host:

```bash
docker run --rm -v uno-data:/app/data uno-cli --bots 3 --games 5 --quiet --seed 42
```

## Logging

The game uses `java.util.logging` for diagnostic events while keeping normal
player-facing CLI output unchanged. Logged events include game start, player
turns, card draws, card plays, invalid input, and game or round end.

Application logs are written to:

```text
uno.log
```

## Database And Reports

The project uses MyBatis with H2. Schema setup runs automatically from:

```text
src/main/resources/db/schema.sql
```

Default database:

```text
jdbc:h2:file:./data/uno;AUTO_SERVER=TRUE
```

Override the database URL with `UNO_DB_URL` or the JVM property `uno.db.url`.

List recent games:

```bash
mvn exec:java -Dexec.args="--recent-games --limit 10"
```

Show player win counts:

```bash
mvn exec:java -Dexec.args="--player-wins"
```

Show highest scores:

```bash
mvn exec:java -Dexec.args="--highest-scores --limit 10"
```

More detail is in [docs/database.md](docs/database.md).

## Legacy Helper Scripts

The shell scripts in `scripts/` now delegate to Maven:

```bash
scripts/compile.sh
scripts/test.sh
scripts/run.sh --bots 3 --games 1
```

## Rules

See `docs/rules.html` for the implemented game rules.
See `docs/rules-supported.md` for final-project supported rules and documented variants.
See `docs/final-report.md` for the final project report.
