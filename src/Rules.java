import java.util.ArrayList;
import java.util.Collections;
import java.util.Random;

/**
 * Encapsulates UNO legality rules and deck construction.
 *
 * Moving these out of Main means they can be tested without
 * running the full CLI game.
 */
public class Rules {

    private Rules() {}

    /**
     * Returns true if {@code card} is a legal play given the
     * current up-card and the called colour (empty string if none).
     *
     * This is the single authoritative legality check; the old code
     * had identical logic duplicated in three places.
     */
    public static boolean isLegal(Card card, Card upCard, String calledColor) {
        if (card.isWild()) return true;

        String cc = card.color();
        String uc = upCard.color();

        if (cc.equals(uc)) return true;
        if (!calledColor.equals("") && cc.equals(calledColor)) return true;

        Card.Rank cr = card.rank();
        Card.Rank ur = upCard.rank();

        if (cr == ur && cr != Card.Rank.NUMBER) return true;
        if (cr == Card.Rank.NUMBER && ur == Card.Rank.NUMBER
                && card.number() == upCard.number()) return true;

        return false;
    }

    /** Builds and shuffles a fresh standard 108-card UNO deck. */
    public static ArrayList<Card> buildDeck(Random random) {
        ArrayList<Card> deck = new ArrayList<>();
        String[] colors = {"R", "Y", "G", "B"};
        for (String c : colors) {
            deck.add(new Card(c + "0"));
            for (int n = 1; n <= 9; n++) {
                deck.add(new Card(c + n));
                deck.add(new Card(c + n));
            }
            deck.add(new Card(c + "S"));  deck.add(new Card(c + "S"));
            deck.add(new Card(c + "R"));  deck.add(new Card(c + "R"));
            deck.add(new Card(c + "+2")); deck.add(new Card(c + "+2"));
        }
        for (int i = 0; i < 4; i++) {
            deck.add(new Card("W"));
            deck.add(new Card("W4"));
        }
        Collections.shuffle(deck, random);
        return deck;
    }

    /**
     * Returns the index of the card the bot wants to play, or -1 to draw.
     * Priority: DRAW_TWO > SKIP > NUMBER > WILD.
     */
    public static int chooseBotCard(ArrayList<Card> hand, Card upCard, String calledColor) {
        int[] priorities = {0, 1, 2};  // 0=DRAW_TWO, 1=SKIP, 2=NUMBER
        for (int pass = 0; pass < 3; pass++) {
            for (int i = 0; i < hand.size(); i++) {
                Card card = hand.get(i);
                if (!isLegal(card, upCard, calledColor)) continue;
                if (pass == 0 && card.rank() == Card.Rank.DRAW_TWO)  return i;
                if (pass == 1 && card.rank() == Card.Rank.SKIP)      return i;
                if (pass == 2 && card.rank() == Card.Rank.NUMBER)    return i;
            }
        }
        // Prefer wild last
        for (int i = 0; i < hand.size(); i++) {
            if (hand.get(i).isWild()) return i;
        }
        return -1;
    }

    /**
     * Picks the colour the bot should call: whichever colour appears
     * most in the remaining hand.
     */
    public static String chooseBotColor(ArrayList<Card> hand) {
        int r = 0, y = 0, g = 0, b = 0;
        for (Card c : hand) {
            switch (c.color()) {
                case "R": r++; break;
                case "Y": y++; break;
                case "G": g++; break;
                case "B": b++; break;
            }
        }
        if (r >= y && r >= g && r >= b) return "R";
        if (y >= r && y >= g && y >= b) return "Y";
        if (g >= r && g >= y && g >= b) return "G";
        return "B";
    }
}
