package uno;

/**
 * Strategy interface for the side-effect a card has on game state
 * after it is legally played.
 *
 * Implementing classes know how to advance the turn and apply any
 * forced draws or skips. GameController no longer needs a switch
 * on card rank; it simply calls card.effect().apply(state).
 */
public interface CardEffect {
    void apply(GameState state);
}
