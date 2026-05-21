# Extension-Readiness Note

## Which extension would your design support best?

**Adding a new card effect** (e.g. a "Swap Hands" card, a "Draw Until Color"
card, or a stacking draw-two rule).

## Where would that change be implemented?

With the `CardEffect` strategy pattern now in place, adding a card effect
requires exactly three steps and zero changes to existing code:

1. **Add a new `CardEffect` in `CardEffects.java`:**
   ```java
   public static final CardEffect SWAP_HANDS = state -> {
       // swap currentPlayer's hand with an opponent's hand
       ...
       state.nextPlayer();
   };
   ```

2. **Register it in `Card.effect()`** by adding one case to the rank switch.

3. **Add the new token to `Rules.buildDeck()`** if the card is a new physical card.

No changes to `GameController`, `GameState`, `Rules`, `ConsoleView`, or
`Main` are needed. The effect is fully contained.

**Adding a rule variant** (e.g. stacking draw-twos, "0/7 swap" house rule):

`Rules.isLegal` is the single place where legality is decided. A rule variant
that changes which cards are legal is a change in one method. For more complex
variants, extracting a `RuleSet` interface and injecting it into
`GameController` and `Rules` would make variants swappable at construction time.

**Replacing or improving the CLI view** (e.g. JSON event log, GUI, test spy):

`ConsoleView` is the only class that produces output or reads input. To replace
it, implement the same public API (`showTurnHeader`, `askHuman`, etc.) and pass
the new implementation to `GameController`. No game logic changes.

## What part of your design still makes change difficult?

* **`Card.effect()` contains a small switch on rank.** Adding a card type
  that reuses an existing `Rank` enum value (e.g. a variant Wild) has nowhere
  clean to live without either expanding the enum or creating a subclass.
  A `CardFactory` or a `Map<Rank, CardEffect>` would close this.

* **`Rules` is a static utility class.** Rule variants that change legality
  globally (tournament mode, custom house rules) cannot be injected — they
  require editing `Rules.isLegal` directly. A `RuleSet` interface passed
  through the constructor would make this open/closed.

* **Scoring is inline in `GameController`.** Adding a scoring variant (e.g.
  negative points for wilds) requires editing the controller. A `Scorer`
  collaborator would separate this responsibility.
