package uno;

/**
 * Value object representing a single UNO card.
 *
 * Centralizes all card parsing (color, rank, number, points, effect) that
 * was previously scattered and duplicated inside Main.
 */
public class Card {

    public enum Rank {
        NUMBER, SKIP, REVERSE, DRAW_TWO, WILD, WILD_DRAW_FOUR
    }

    /** Raw string token used in the original code, kept for compatibility. */
    public final String token;

    public Card(String token) {
        this.token = token;
    }

    /** Returns the color prefix ("R","Y","G","B") or "" for wilds. */
    public String color() {
        if (token.startsWith("R")) return "R";
        if (token.startsWith("Y")) return "Y";
        if (token.startsWith("G")) return "G";
        if (token.startsWith("B")) return "B";
        return "";
    }

    public Rank rank() {
        if (token.equals("W"))    return Rank.WILD;
        if (token.equals("W4"))   return Rank.WILD_DRAW_FOUR;
        if (token.endsWith("S"))  return Rank.SKIP;
        if (token.endsWith("R"))  return Rank.REVERSE;
        if (token.endsWith("+2")) return Rank.DRAW_TWO;
        return Rank.NUMBER;
    }

    /** Returns the face number for NUMBER cards, -1 otherwise. */
    public int number() {
        if (rank() == Rank.NUMBER) return Integer.parseInt(token.substring(1));
        return -1;
    }

    public int points() {
        switch (rank()) {
            case NUMBER:         return number();
            case SKIP:
            case REVERSE:
            case DRAW_TWO:       return 20;
            case WILD:
            case WILD_DRAW_FOUR: return 50;
            default:             return 0;
        }
    }

    public boolean isWild() { return token.startsWith("W"); }

    /**
     * Returns the turn-advancement effect for this card.
     * GameController calls card.effect().apply(state) instead of switching on rank.
     */
    public CardEffect effect() {
        switch (rank()) {
            case SKIP:           return CardEffects.SKIP;
            case REVERSE:        return CardEffects.REVERSE;
            case DRAW_TWO:       return CardEffects.DRAW_TWO;
            case WILD_DRAW_FOUR: return CardEffects.WILD_DRAW_FOUR;
            default:             return CardEffects.NORMAL;
        }
    }

    @Override public String toString() { return token; }
}
