import java.util.ArrayList;

/**
 * Runs one UNO game by coordinating GameState (rules/data) and ConsoleView (I/O).
 *
 * The controller does NOT contain rule logic or card-effect logic.
 * Card effects are delegated to card.effect().apply(state) -- no switch needed here.
 */
public class GameController {

    private final GameState   state;
    private final ConsoleView view;

    public GameController(GameState state, ConsoleView view) {
        this.state = state;
        this.view  = view;
    }

    /**
     * Plays one complete game. Returns the index of the winning player,
     * or -1 if the safety limit was reached.
     */
    public int playGame() {
        state.reset();

        for (int guard = 0; guard < 3000; guard++) {
            view.showTurnHeader(state);

            PlayChoice choice = getTurnChoice();

            if (choice.kind == PlayChoice.Kind.INVALID) {
                view.showPenaltyInvalidIndex(state.currentName());
                state.currentHand().add(state.drawCard());
                state.nextPlayer();
                continue;
            }

            if (choice.kind == PlayChoice.Kind.DRAW) {
                Card drawn = state.drawCard();
                state.currentHand().add(drawn);
                view.showDraw(state.currentName(), drawn);

                if (Rules.isLegal(drawn, state.upCard, state.calledColor)) {
                    boolean willPlay = state.currentIsHuman()
                            ? view.askPlayDrawn(drawn)
                            : true;   // bots always play a drawn legal card
                    if (willPlay) {
                        choice = PlayChoice.play(state.currentHand().size() - 1);
                    }
                }
            }

            if (choice.kind == PlayChoice.Kind.PLAY) {
                Card card = state.currentHand().get(choice.index);

                if (!Rules.isLegal(card, state.upCard, state.calledColor)) {
                    view.showPenaltyIllegal(state.currentName(), card);
                    state.currentHand().add(state.drawCard());
                    state.nextPlayer();
                    continue;
                }

                applyPlay(choice.index, card);

                if (state.currentHand().isEmpty()) {
                    int winner = state.currentPlayer;
                    int points = scoreOtherHands(winner);
                    state.scores[winner] += points;
                    view.showWin(state.currentName(), points);
                    return winner;
                }

                // Delegate turn advancement + draw effects to the card itself.
                // No switch needed: each card knows its own effect.
                card.effect().apply(state);

            } else {
                // DRAW choice where drawn card was not played
                state.nextPlayer();
            }
        }

        view.showSafetyLimit();
        return -1;
    }

    // ---- private helpers ----

    private PlayChoice getTurnChoice() {
        if (state.currentIsHuman()) {
            return view.askHuman(state.currentHand(), state.upCard, state.calledColor);
        }
        int idx = Rules.chooseBotCard(state.currentHand(), state.upCard, state.calledColor);
        return idx >= 0 ? PlayChoice.play(idx) : PlayChoice.draw();
    }

    /** Removes the card from hand, updates upCard, handles wild colour call. */
    private void applyPlay(int index, Card card) {
        state.currentHand().remove(index);
        state.discard.add(state.upCard);
        state.upCard      = card;
        state.calledColor = "";
        view.showPlay(state.currentName(), card);

        if (card.isWild()) {
            String color = state.currentIsHuman()
                    ? view.askColor()
                    : Rules.chooseBotColor(state.currentHand());
            state.calledColor = color;
            view.showColorCall(state.currentName(), color);
        }

        if (state.currentHand().size() == 1) view.showUno(state.currentName());
    }

    private int scoreOtherHands(int winner) {
        int total = 0;
        for (int i = 0; i < state.hands.size(); i++) {
            if (i != winner) for (Card c : state.hands.get(i)) total += c.points();
        }
        return total;
    }
}
