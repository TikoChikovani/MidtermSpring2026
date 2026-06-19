package uno;

import java.util.ArrayList;
import java.util.Random;

/**
 * Encapsulates UNO legality rules and deck construction.
 *
 * Moving these out of Main means they can be tested without
 * running the full CLI game.
 */
public class Rules {

    private static final RuleSet STANDARD = new RuleSet();

    private Rules() {}

    /**
     * Returns true if {@code card} is a legal play given the
     * current up-card and the called colour (empty string if none).
     *
     * This is the single authoritative legality check; the old code
     * had identical logic duplicated in three places.
     */
    public static boolean isLegal(Card card, Card upCard, String calledColor) {
        return STANDARD.isLegal(card, upCard, calledColor);
    }

    /** Builds and shuffles a fresh standard 108-card UNO deck. */
    public static ArrayList<Card> buildDeck(Random random) {
        return STANDARD.buildDeck(random);
    }

    /**
     * Returns the index of the card the bot wants to play, or -1 to draw.
     * Priority: DRAW_TWO > SKIP > NUMBER > WILD.
     */
    public static int chooseBotCard(ArrayList<Card> hand, Card upCard, String calledColor) {
        return STANDARD.chooseBotCard(hand, upCard, calledColor);
    }

    /**
     * Picks the colour the bot should call: whichever colour appears
     * most in the remaining hand.
     */
    public static String chooseBotColor(ArrayList<Card> hand) {
        return STANDARD.chooseBotColor(hand);
    }
}
