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
    private final RuleSet     rules;
    private final Scorer      scorer;

    public GameController(GameState state, ConsoleView view) {
        this(state, view, new RuleSet(), new Scorer());
    }

    public GameController(GameState state, ConsoleView view, RuleSet rules, Scorer scorer) {
        this.state = state;
        this.view  = view;
        this.rules = rules;
        this.scorer = scorer;
    }

    /**
     * Plays one complete game. Returns the index of the winning player,
     * or -1 if the safety limit was reached.
     */
    public int playGame() {
        state.reset();

        for (int guard = 0; guard < 3000; guard++) {
            int winner = playTurn();
            if (winner >= 0) return winner;
        }

        view.showSafetyLimit();
        return -1;
    }

    // ---- turn steps ----

    int playTurn() {
        view.showTurnHeader(state);

        PlayChoice choice = chooseTurn();

        if (choice.kind == PlayChoice.Kind.INVALID) {
            handleInvalidChoice();
            return -1;
        }

        choice = handleDrawChoice(choice);

        if (choice.kind == PlayChoice.Kind.PLAY) {
            Card card = state.currentHand().get(choice.index);

            if (!rules.isLegal(card, state.upCard, state.calledColor)) {
                handleIllegalPlay(card);
                return -1;
            }

            applyPlay(choice.index, card);

            int winner = handleWinIfNeeded();
            if (winner >= 0) return winner;

            card.effect().apply(state);
        } else {
            handleUnplayedDraw();
        }

        return -1;
    }

    PlayChoice chooseTurn() {
        if (state.currentIsHuman()) {
            return view.askHuman(state.currentHand(), state.upCard, state.calledColor);
        }
        int idx = rules.chooseBotCard(state.currentHand(), state.upCard, state.calledColor);
        return idx >= 0 ? PlayChoice.play(idx) : PlayChoice.draw();
    }

    PlayChoice handleDrawChoice(PlayChoice choice) {
        if (choice.kind != PlayChoice.Kind.DRAW) return choice;

        Card drawn = state.drawCard();
        state.currentHand().add(drawn);
        view.showDraw(state.currentName(), drawn);

        if (rules.isLegal(drawn, state.upCard, state.calledColor)) {
            boolean willPlay = state.currentIsHuman()
                    ? view.askPlayDrawn(drawn)
                    : true;
            if (willPlay) return PlayChoice.play(state.currentHand().size() - 1);
        }

        return choice;
    }

    void handleInvalidChoice() {
        view.showPenaltyInvalidIndex(state.currentName());
        state.currentHand().add(state.drawCard());
        state.nextPlayer();
    }

    void handleIllegalPlay(Card card) {
        view.showPenaltyIllegal(state.currentName(), card);
        state.currentHand().add(state.drawCard());
        state.nextPlayer();
    }

    /** Removes the card from hand, updates upCard, handles wild colour call. */
    void applyPlay(int index, Card card) {
        state.currentHand().remove(index);
        state.discard.add(state.upCard);
        state.upCard      = card;
        state.calledColor = "";
        view.showPlay(state.currentName(), card);

        if (card.isWild()) {
            String color = state.currentIsHuman()
                    ? view.askColor()
                    : rules.chooseBotColor(state.currentHand());
            state.calledColor = color;
            view.showColorCall(state.currentName(), color);
        }

        if (state.currentHand().size() == 1) view.showUno(state.currentName());
    }

    int handleWinIfNeeded() {
        if (!state.currentHand().isEmpty()) return -1;

        int winner = state.currentPlayer;
        int points = scorer.scoreOtherHands(state, winner);
        state.scores[winner] += points;
        view.showWin(state.currentName(), points);
        return winner;
    }

    void handleUnplayedDraw() {
        state.nextPlayer();
    }
}
