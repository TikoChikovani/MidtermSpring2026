package uno;

/**
 * Concrete CardEffect implementations, one per distinct UNO card behavior.
 *
 * Adding a new card type (e.g. Swap Hands) means adding a new class here
 * and registering it in Card.effect() -- no existing code changes.
 */
public class CardEffects {

    /** Normal number card: just advance to the next player. */
    public static final CardEffect NORMAL = state -> state.nextPlayer();

    /** Skip: advance twice, so the next player loses their turn. */
    public static final CardEffect SKIP = state -> {
        state.nextPlayer();
        state.nextPlayer();
    };

    /**
     * Reverse: flip direction.
     * In a two-player game this acts as a skip (documented quirk preserved).
     */
    public static final CardEffect REVERSE = state -> {
        state.direction *= -1;
        state.nextPlayer();
        if (state.playerNames.size() == 2) {
            // Two-player reverse skips back to the same player
            state.nextPlayer();
        }
    };

    /** Draw Two: next player draws 2 cards and loses their turn. */
    public static final CardEffect DRAW_TWO = state -> {
        state.nextPlayer();
        state.currentHand().add(state.drawCard());
        state.currentHand().add(state.drawCard());
        state.nextPlayer();
    };

    /**
     * Wild: color is already recorded in state.calledColor by the controller
     * before this effect runs; just advance normally.
     */
    public static final CardEffect WILD = state -> state.nextPlayer();

    /** Wild Draw Four: next player draws 4 cards and loses their turn. */
    public static final CardEffect WILD_DRAW_FOUR = state -> {
        state.nextPlayer();
        for (int i = 0; i < 4; i++) state.currentHand().add(state.drawCard());
        state.nextPlayer();
    };
}
