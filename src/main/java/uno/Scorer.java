package uno;

/**
 * Calculates end-of-round points.
 */
public class Scorer {

    public int scoreOtherHands(GameState state, int winner) {
        int total = 0;
        for (int i = 0; i < state.hands.size(); i++) {
            if (i == winner) continue;
            for (Card c : state.hands.get(i)) {
                total += c.points();
            }
        }
        return total;
    }
}
