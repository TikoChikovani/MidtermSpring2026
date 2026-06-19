package uno;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Random;

/**
 * Standard UNO rule decisions used by the controller, view, and state.
 *
 * Keeping this as an object lets tests or future game variants inject a
 * different rule set without changing the turn loop.
 */
public class RuleSet {

    public boolean isLegal(Card card, Card upCard, String calledColor) {
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

    public ArrayList<Card> buildDeck(Random random) {
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

    public int chooseBotCard(ArrayList<Card> hand, Card upCard, String calledColor) {
        for (int pass = 0; pass < 3; pass++) {
            for (int i = 0; i < hand.size(); i++) {
                Card card = hand.get(i);
                if (!isLegal(card, upCard, calledColor)) continue;
                if (pass == 0 && card.rank() == Card.Rank.DRAW_TWO)  return i;
                if (pass == 1 && card.rank() == Card.Rank.SKIP)      return i;
                if (pass == 2 && card.rank() == Card.Rank.NUMBER)    return i;
            }
        }
        for (int i = 0; i < hand.size(); i++) {
            if (hand.get(i).isWild()) return i;
        }
        return -1;
    }

    public String chooseBotColor(ArrayList<Card> hand) {
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
