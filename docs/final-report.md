# Final Project Report

## Implemented Rules

The project implements a 108-card UNO-style deck, legal play validation, Skip,
Reverse, Draw Two, Wild, Wild Draw Four, draw/pass behavior, UNO call and
missed-UNO penalty, round scoring, and multi-round target-score play.

The implementation intentionally omits optional challenge and stacking variants.
Those simplifications are listed in `docs/rules-supported.md`.

## CLI Play

Run a normal bot game:

```bash
mvn exec:java -Dexec.args="--bots 3 --games 1"
```

Run until a target score:

```bash
mvn exec:java -Dexec.args="--bots 3 --target-score 500"
```

Run an interactive game:

```bash
mvn exec:java -Dexec.args="--human --bots 2 --target-score 500"
```

Human players can play by index or token:

```text
0
R5
draw
```

To call UNO, include `UNO` with the play:

```text
R5 UNO
0 UNO
```

## Architecture

Game logic is separated from console interaction:

- `Card`, `RuleSet`, and `Rules` own card parsing and legal-play decisions.
- `GameState` owns hands, deck, discard pile, scores, direction, and turn state.
- `CardEffects` owns action-card behavior.
- `GameController` coordinates one round without embedding console parsing.
- `ConsoleView` owns prompts and player-facing text.
- `Scorer` owns end-of-round point calculation.
- `uno.persistence` owns MyBatis/H2 persistence and report queries.

Most rule behavior can be tested directly without stdin/stdout.

## Tests Added

The characterization suite covers:

- deck composition
- legal play by color, number, action type, and wild color
- Skip, Reverse, Draw Two, Wild, and Wild Draw Four behavior
- draw/pass behavior
- scoring
- UNO calls and missed-UNO penalty
- target-score continuation logic
- seeded end-to-end bot games

Persistence tests cover:

- recording completed games
- recent game reports
- player win-count reports
- highest-score reports

Run all tests with:

```bash
mvn test
```

## Remaining Limitations

- No Draw Two or Wild Draw Four stacking.
- No Wild Draw Four challenge rule.
- Bot play is intentionally simple.
- The CLI is text-only.
- Missed UNO penalty timing is immediate rather than challenge-based.
