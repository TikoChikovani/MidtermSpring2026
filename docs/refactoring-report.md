# Refactoring Report

## What behavior did you characterize before refactoring?

Before touching any logic I ran the original `--self-test` (7 checks) and
confirmed they all passed. I then mapped every behavior named in the exam
to a concrete check in `CharacterizationTests.java`.

### Behaviors characterized (53 total checks)

| Category | Detail |
|---|---|
| Color matching | R on R; Y on Y; G not on R |
| Number matching | 9 on 9 (diff color); 0 on 0; mismatch illegal |
| Action-type matching | SKIP/REVERSE/DRAW_TWO each match their own type; SKIP ≠ REVERSE |
| Wild / W4 | Always legal, even on action cards |
| Called-color rule | Matching called color is legal; wrong color still illegal |
| SKIP effect | Next player's turn skipped (currentPlayer advances by 2, 3-player) |
| REVERSE effect | `direction` field flips; double-reverse restores original |
| REVERSE 2-player quirk | Acts as a skip — documented in `rules.html` |
| DRAW_TWO effect | Punished player receives exactly 2 cards and loses their turn |
| WILD DRAW FOUR effect | Punished player receives exactly 4 cards and loses their turn |
| Draw from deck | Deck shrinks by 1; empty deck reshuffles discard; both-empty returns W fallback |
| Scoring | Every card rank returns correct point value; opponent-sum logic verified |
| Bot priority | DRAW_TWO > SKIP > NUMBER > WILD; returns -1 when no legal card exists |
| End-to-end single game | Full game completes, winner ≥ 0, winner score > 0 |
| End-to-end multi-game | 5 consecutive games all finish; scores accumulate across games |
| Determinism | Same seed produces same winner and same score |

Surprising edge case: `chooseBotCard` prefers DRAW_TWO before SKIP before
NUMBER even when all are legal — not just "first legal card".

## What were the worst design problems?

From `docs/expected-smells.md`, confirmed by reading `Main.java`:

* **Duplicated legality logic** — the same five-condition block appeared in
  `playGame`, `chooseBotCard`, and `isLegal`.
* **Global mutable state** — all fields were `static`; no isolation for testing.
* **Console I/O tangled with game logic** — `System.out.println` and
  `scanner` calls interleaved with legality checks and turn flow.
* **Monolithic turn loop** — ~130 lines with no named sub-steps.
* **Primitive card representation** — card parsing scattered as free static methods.
* **Centralized card-effect dispatch** — a large if/else chain inside the
  turn loop owned all skip/reverse/draw logic with no clear home.

## Which refactorings did you perform?

### Step 1 — Extract `Card` value object
Moved `color()`, `rank()`, `number()`, `points()` into `Card` as instance
methods. Added `effect()` returning a `CardEffect` strategy.

### Step 2 — Extract `Rules`
Single authoritative `isLegal(Card, Card, String)` replaces three duplicated
inline blocks. `chooseBotCard` and `chooseBotColor` moved here as pure functions.

### Step 3 — Extract `GameState`
All mutable game data moved into a `GameState` instance. Breaks global-state
coupling and makes single-turn testing possible without running the CLI.

### Step 4 — Extract `ConsoleView`
Every `System.out` and `scanner` call moved here. The controller never calls
`System.out` directly. Human input returns a `PlayChoice` value instead of a
sentinel integer.

### Step 5 — Introduce `PlayChoice`
Replaces the fragile `int` convention (`-1` = draw, `hand.size()` = invalid).
Each case is now a named enum: `PLAY`, `DRAW`, `INVALID`.

### Step 6 — Introduce `CardEffect` + `CardEffects`
Each card rank is paired with a `CardEffect` strategy object in `CardEffects`.
`Card.effect()` returns the right one. `GameController` calls
`card.effect().apply(state)` — the large switch is gone.

### Step 7 — Extract `GameController`
Turn loop extracted into `GameController`, which coordinates `GameState` and
`ConsoleView` without owning rule or effect logic.

### Step 8 — Add `CharacterizationTests`
53 checks covering all required behaviors including end-to-end full-game
tests and determinism verification.

### Step 9 — Thin `Main`
Parses CLI flags, builds collaborators, runs games, delegates `--self-test`.

## What behavior did you intentionally preserve?

All behavior from `docs/rules.html`:

* All hands visible in the terminal on each turn.
* Humans may type `draw` even when holding a legal card.
* Out-of-range or invalid numeric index → penalty card and turn loss.
* Bot automatically plays a drawn card when legal.
* Two-player REVERSE acts as a SKIP.
* Bot colour strategy: most-frequent colour in remaining hand.
* Bot card priority: DRAW_TWO > SKIP > NUMBER > WILD.
* Safety limit of 3000 turns per game.
* Emergency fallback: empty deck + empty discard returns a W card.

## What risks remain?

* `Card.effect()` still has a small switch on `rank()` to map to the right
  `CardEffect`. Removing it entirely would require a factory or enum-attached
  strategies.
* `Card` still stores a raw string token. A proper enum-based card identity
  (or a factory) would eliminate all remaining string parsing.
* No integration test covers human-player input paths (those require stdin
  mocking).
