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

    private PlayChoice(Kind kind, int index) {
        this.kind  = kind;
        this.index = index;
    }

    public static PlayChoice play(int index) { return new PlayChoice(Kind.PLAY,    index); }
    public static PlayChoice draw()          { return new PlayChoice(Kind.DRAW,    -1);    }
    public static PlayChoice invalid()       { return new PlayChoice(Kind.INVALID, -1);    }
}
