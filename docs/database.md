# UNO Persistence

## Database

The project uses H2 for local development and tests.

Default application database:

```text
jdbc:h2:file:./data/uno;AUTO_SERVER=TRUE
```

Override the database URL with either:

```bash
UNO_DB_URL="jdbc:h2:file:./data/custom-uno" mvn exec:java -Dexec.args="--bots 3 --games 1"
```

or:

```bash
mvn exec:java -Duno.db.url="jdbc:h2:file:./data/custom-uno" -Dexec.args="--bots 3 --games 1"
```

No private database server or local credentials are required.
If a custom database needs credentials, provide them with `UNO_DB_USER` and
`UNO_DB_PASSWORD` or with JVM properties `uno.db.user` and `uno.db.password`.

## Persistence Framework

The project uses MyBatis as the structured persistence mapper.

Game code calls `GameHistoryRepository`; SQL is isolated in mapper interfaces:

- `PlayerMapper`
- `GameMapper`
- `RoundMapper`
- `ScoreMapper`

## Schema

Schema initialization runs automatically at startup from:

```text
src/main/resources/db/schema.sql
```

Tables:

- `players`: player names
- `games`: game start/completion timestamp and final winner
- `rounds`: round number, completion timestamp, and round winner
- `scores`: per-player round points and cumulative total scores

## Run Persistence Tests

```bash
mvn test
```

Persistence tests use isolated in-memory H2 databases such as:

```text
jdbc:h2:mem:uno_recent;DB_CLOSE_DELAY=-1
```

They do not depend on developer machine state.

## View Game History And Statistics

Run games to create history:

```bash
mvn exec:java -Dexec.args="--bots 3 --games 5 --quiet --seed 42"
```

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
