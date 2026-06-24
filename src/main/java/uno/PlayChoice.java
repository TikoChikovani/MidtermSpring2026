package uno;

/**
 * Represents a player's decision for their turn.
 *
 * Replaces the fragile sentinel-int convention where -1 meant "draw" and
 * hand.size() meant "invalid index penalty". Each case is now named.
 */
public class PlayChoice {

    public enum Kind { PLAY, DRAW, INVALID }

    public final Kind kind;
    /** Valid only when kind == PLAY. */
    public final int index;
    /** True when the player explicitly called UNO with this play. */
    public final boolean unoCalled;

    private PlayChoice(Kind kind, int index, boolean unoCalled) {
        this.kind      = kind;
        this.index     = index;
        this.unoCalled = unoCalled;
    }

    public static PlayChoice play(int index) { return play(index, false); }
    public static PlayChoice play(int index, boolean unoCalled) {
        return new PlayChoice(Kind.PLAY, index, unoCalled);
    }
    public static PlayChoice draw()    { return new PlayChoice(Kind.DRAW,    -1, false); }
    public static PlayChoice invalid() { return new PlayChoice(Kind.INVALID, -1, false); }
}
