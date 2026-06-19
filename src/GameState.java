import java.util.ArrayList;
import java.util.Collections;
import java.util.Random;

/**
 * Pure game state: hands, deck, discard pile, scores, turn order.
 *
 * Contains no console I/O. All methods that previously lived inside
 * the main turn loop and accessed static fields now live here.
 */
public class GameState {

    public final ArrayList<String>         playerNames;
    public final ArrayList<Boolean>        humanPlayers;
    public final ArrayList<ArrayList<Card>> hands;
    public final ArrayList<Card>           deck    = new ArrayList<>();
    public final ArrayList<Card>           discard = new ArrayList<>();
    public final int[]                     scores;
    public int     currentPlayer = 0;
    public int     direction     = 1;
    public Card    upCard;
    public String  calledColor   = "";

    private final Random random;
    private final RuleSet rules;

    public GameState(ArrayList<String> playerNames,
                     ArrayList<Boolean> humanPlayers,
                     int[] scores,
                     Random random) {
        this(playerNames, humanPlayers, scores, random, new RuleSet());
    }

    public GameState(ArrayList<String> playerNames,
                     ArrayList<Boolean> humanPlayers,
                     int[] scores,
                     Random random,
                     RuleSet rules) {
        this.playerNames  = playerNames;
        this.humanPlayers = humanPlayers;
        this.scores       = scores;
        this.random       = random;
        this.rules        = rules;
        this.hands        = new ArrayList<>();
        for (int i = 0; i < playerNames.size(); i++) {
            hands.add(new ArrayList<>());
        }
    }

    /** Sets up a fresh game: shuffle deck, deal hands, choose starting card. */
    public void reset() {
        deck.clear();
        deck.addAll(rules.buildDeck(random));
        discard.clear();
        for (ArrayList<Card> hand : hands) hand.clear();

        for (int i = 0; i < playerNames.size(); i++) {
            for (int j = 0; j < 7; j++) {
                hands.get(i).add(drawCard());
            }
        }

        upCard = drawCard();
        while (upCard.isWild()) {
            discard.add(upCard);
            upCard = drawCard();
        }
        calledColor   = "";
        direction     = 1;
        currentPlayer = random.nextInt(playerNames.size());
    }

    /** Draws one card, reshuffling the discard pile if the deck is empty. */
    public Card drawCard() {
        if (deck.isEmpty()) {
            deck.addAll(discard);
            discard.clear();
            Collections.shuffle(deck, random);
        }
        if (deck.isEmpty()) return new Card("W");   // emergency fallback (preserved from original)
        return deck.remove(0);
    }

    /** Advances currentPlayer by one step in the current direction. */
    public void nextPlayer() {
        currentPlayer += direction;
        if (currentPlayer >= playerNames.size()) currentPlayer = 0;
        if (currentPlayer < 0)                  currentPlayer = playerNames.size() - 1;
    }

    public ArrayList<Card> currentHand() {
        return hands.get(currentPlayer);
    }

    public String currentName() {
        return playerNames.get(currentPlayer);
    }

    public boolean currentIsHuman() {
        return humanPlayers.get(currentPlayer);
    }
}
