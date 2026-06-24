package uno;

import java.util.ArrayList;
import java.util.Random;
import java.util.Scanner;

/**
 * Characterization tests for gameplay effects.
 *
 * These tests describe what THIS implementation does -- including quirks.
 * They are not tests for ideal UNO; they lock in observed behavior so that
 * refactoring cannot silently break it.
 *
 * Run via: scripts/test.sh  (--self-test delegates here)
 */
public class CharacterizationTests {

    private int passed = 0;
    private int total  = 0;

    public static int run() {
        return new CharacterizationTests().runAll();
    }

    private int runAll() {

        // ----------------------------------------------------------------
        // 1. DECK COMPOSITION
        // ----------------------------------------------------------------
        {
            ArrayList<Card> deck = Rules.buildDeck(new Random(1));
            assertEquals("deck has 108 cards", 108, deck.size());
            assertEquals("deck has one R0", 1, count(deck, "R0"));
            assertEquals("deck has two R9", 2, count(deck, "R9"));
            assertEquals("deck has two skips per color", 2, count(deck, "YS"));
            assertEquals("deck has two reverses per color", 2, count(deck, "BR"));
            assertEquals("deck has two draw twos per color", 2, count(deck, "G+2"));
            assertEquals("deck has four wilds", 4, count(deck, "W"));
            assertEquals("deck has four wild draw fours", 4, count(deck, "W4"));
        }

        // ----------------------------------------------------------------
        // 2. MATCHING BY COLOR
        // ----------------------------------------------------------------
        check("color match: R on R",     Rules.isLegal(card("R2"), card("R9"), ""));
        check("color match: Y on Y",     Rules.isLegal(card("Y3"), card("Y7"), ""));
        check("no color match: G on R", !Rules.isLegal(card("G2"), card("R9"), ""));

        // ----------------------------------------------------------------
        // 3. MATCHING BY NUMBER
        // ----------------------------------------------------------------
        check("number match: 9 on 9 diff color", Rules.isLegal(card("G9"), card("R9"), ""));
        check("number match: 0 on 0",             Rules.isLegal(card("B0"), card("R0"), ""));
        check("number mismatch illegal",          !Rules.isLegal(card("G3"), card("R5"), ""));

        // ----------------------------------------------------------------
        // 4. MATCHING BY ACTION TYPE
        // ----------------------------------------------------------------
        check("SKIP matches SKIP",          Rules.isLegal(card("GS"),  card("RS"),  ""));
        check("REVERSE matches REVERSE",    Rules.isLegal(card("BR"),  card("RR"),  ""));
        check("DRAW_TWO matches DRAW_TWO",  Rules.isLegal(card("G+2"), card("R+2"), ""));
        check("SKIP does not match REVERSE",!Rules.isLegal(card("GS"), card("RR"),  ""));

        // ----------------------------------------------------------------
        // 5. WILD AND WILD DRAW FOUR BEHAVIOR
        // ----------------------------------------------------------------
        check("W always legal",              Rules.isLegal(card("W"),  card("R5"), ""));
        check("W4 always legal",             Rules.isLegal(card("W4"), card("B3"), ""));
        check("W legal on action card",      Rules.isLegal(card("W"),  card("RS"), ""));
        check("called color lets mismatched color play",
              Rules.isLegal(card("B3"), card("W"), "B"));
        check("called color: wrong color still illegal",
             !Rules.isLegal(card("G3"), card("W"), "B"));

        // ----------------------------------------------------------------
        // 6. SKIP -- effect: next player loses their turn
        // ----------------------------------------------------------------
        {
            GameState s = threePlayerState(42);
            int start = s.currentPlayer;
            CardEffects.SKIP.apply(s);
            check("SKIP skips one player (3p)", s.currentPlayer == (start + 2) % 3);
        }

        // ----------------------------------------------------------------
        // 7. REVERSE -- effect: direction flips; 2-player acts as skip
        // ----------------------------------------------------------------
        {
            GameState s = threePlayerState(1);
            assertEquals("initial direction is 1", 1, s.direction);
            CardEffects.REVERSE.apply(s);
            assertEquals("direction after REVERSE", -1, s.direction);
        }
        {
            GameState s = threePlayerState(2);
            CardEffects.REVERSE.apply(s);
            CardEffects.REVERSE.apply(s);
            assertEquals("double REVERSE restores direction", 1, s.direction);
        }
        {
            // Documented quirk: 2-player REVERSE acts as a SKIP
            GameState s = twoPlayerState(7);
            int start = s.currentPlayer;
            CardEffects.REVERSE.apply(s);
            check("REVERSE in 2-player returns to same player",
                  s.currentPlayer == start);
        }

        // ----------------------------------------------------------------
        // 8. DRAW TWO -- next player draws 2 and loses their turn
        // ----------------------------------------------------------------
        {
            GameState s = threePlayerState(10);
            int nextP     = (s.currentPlayer + 1) % 3;
            int handBefore = s.hands.get(nextP).size();
            CardEffects.DRAW_TWO.apply(s);
            assertEquals("DRAW_TWO adds 2 cards to next player",
                         handBefore + 2, s.hands.get(nextP).size());
            check("DRAW_TWO skips punished player", s.currentPlayer != nextP);
        }

        // ----------------------------------------------------------------
        // 9. WILD DRAW FOUR -- next player draws 4 and loses their turn
        // ----------------------------------------------------------------
        {
            GameState s = threePlayerState(15);
            int nextP     = (s.currentPlayer + 1) % 3;
            int handBefore = s.hands.get(nextP).size();
            CardEffects.WILD_DRAW_FOUR.apply(s);
            assertEquals("W4 adds 4 cards to next player",
                         handBefore + 4, s.hands.get(nextP).size());
            check("W4 skips punished player", s.currentPlayer != nextP);
        }

        // ----------------------------------------------------------------
        // 10. DRAWING FROM DECK
        // ----------------------------------------------------------------
        {
            GameState s = threePlayerState(5);
            int before = s.deck.size();
            s.drawCard();
            assertEquals("drawCard reduces deck by 1", before - 1, s.deck.size());
        }
        {
            GameState s = threePlayerState(3);
            s.discard.addAll(s.deck);
            s.deck.clear();
            Card drawn = s.drawCard();
            check("draw from empty deck reshuffles discard", drawn != null);
            check("deck or discard not both empty after reshuffle",
                  !s.deck.isEmpty() || !s.discard.isEmpty());
        }
        {
            GameState s = threePlayerState(9);
            s.deck.clear();
            s.discard.clear();
            Card fallback = s.drawCard();
            check("empty deck+discard returns W fallback", fallback.token.equals("W"));
        }

        // ----------------------------------------------------------------
        // 11. SCORING
        // ----------------------------------------------------------------
        check("number card points == face value", card("R7").points() == 7);
        check("0 card points == 0",               card("G0").points() == 0);
        check("SKIP points == 20",                card("RS").points() == 20);
        check("REVERSE points == 20",             card("BR").points() == 20);
        check("DRAW_TWO points == 20",            card("Y+2").points() == 20);
        check("WILD points == 50",                card("W").points()  == 50);
        check("W4 points == 50",                  card("W4").points() == 50);
        {
            GameState s = threePlayerState(11);
            s.hands.get(1).clear();
            s.hands.get(1).add(card("R5"));   // 5 pts
            s.hands.get(1).add(card("W4"));   // 50 pts
            s.hands.get(2).clear();
            s.hands.get(2).add(card("GS"));   // 20 pts
            int total = 0;
            for (int i = 0; i < s.hands.size(); i++)
                if (i != 0) for (Card c : s.hands.get(i)) total += c.points();
            assertEquals("score sums all opponents", 75, total);
        }
        {
            GameState s = threePlayerState(12);
            s.hands.get(0).clear();
            s.hands.get(1).clear();
            s.hands.get(1).add(card("R4"));
            s.hands.get(1).add(card("W"));
            s.hands.get(2).clear();
            s.hands.get(2).add(card("G+2"));
            assertEquals("Scorer sums non-winner hands", 74, new Scorer().scoreOtherHands(s, 0));
        }

        // ----------------------------------------------------------------
        // 12. CONTROLLER TURN STEPS
        // ----------------------------------------------------------------
        {
            GameState s = threePlayerState(20);
            s.hands.get(0).clear();
            s.hands.get(0).add(card("G3"));
            s.hands.get(0).add(card("R7"));
            GameController ctrl = new GameController(s, new ConsoleView(true));
            PlayChoice choice = ctrl.chooseTurn();
            check("chooseTurn returns bot play", choice.kind == PlayChoice.Kind.PLAY && choice.index == 1);
        }
        {
            GameState s = threePlayerState(21);
            s.hands.get(0).clear();
            s.deck.clear();
            s.deck.add(card("R7"));
            GameController ctrl = new GameController(s, new ConsoleView(true));
            PlayChoice choice = ctrl.handleDrawChoice(PlayChoice.draw());
            check("draw handling plays legal bot draw",
                  choice.kind == PlayChoice.Kind.PLAY && choice.index == 0);
            assertEquals("draw handling adds drawn card to hand", 1, s.currentHand().size());
        }
        {
            GameState s = threePlayerState(22);
            s.hands.get(0).clear();
            s.hands.get(0).add(card("R7"));
            s.hands.get(0).add(card("G2"));
            GameController ctrl = new GameController(s, new ConsoleView(true));
            ctrl.applyPlay(0, s.currentHand().get(0));
            check("applyPlay updates up card", s.upCard.token.equals("R7"));
            check("applyPlay discards old up card", s.discard.get(s.discard.size() - 1).token.equals("R5"));
            assertEquals("applyPlay removes card from hand", 1, s.currentHand().size());
        }
        {
            GameState s = threePlayerState(23);
            s.currentPlayer = 0;
            s.hands.get(0).clear();
            s.hands.get(1).clear();
            s.hands.get(1).add(card("R5"));
            s.hands.get(2).clear();
            s.hands.get(2).add(card("W4"));
            GameController ctrl = new GameController(s, new ConsoleView(true));
            int winner = ctrl.handleWinIfNeeded();
            assertEquals("win handling returns winner", 0, winner);
            assertEquals("win handling adds score", 55, s.scores[0]);
        }
        {
            GameState s = threePlayerState(24);
            int start = s.currentPlayer;
            GameController ctrl = new GameController(s, new ConsoleView(true));
            ctrl.handleUnplayedDraw();
            check("unplayed draw advances player", s.currentPlayer == (start + 1) % 3);
        }
        {
            GameState s = threePlayerState(25);
            s.hands.get(0).clear();
            s.hands.get(0).add(card("R7"));
            s.hands.get(0).add(card("G2"));
            GameController ctrl = new GameController(s, new ConsoleView(true));
            ctrl.applyPlay(PlayChoice.play(0, true), s.currentHand().get(0));
            assertEquals("called UNO leaves one-card hand", 1, s.currentHand().size());
        }
        {
            GameState s = threePlayerState(26);
            s.hands.get(0).clear();
            s.hands.get(0).add(card("R7"));
            s.hands.get(0).add(card("G2"));
            s.deck.clear();
            s.deck.add(card("B1"));
            s.deck.add(card("B2"));
            GameController ctrl = new GameController(s, new ConsoleView(true));
            ctrl.applyPlay(PlayChoice.play(0, false), s.currentHand().get(0));
            assertEquals("missed UNO draws two penalty cards", 3, s.currentHand().size());
        }
        {
            GameState s = threePlayerState(27);
            s.hands.get(0).clear();
            s.hands.get(0).add(card("R7"));
            s.hands.get(0).add(card("G2"));
            GameController ctrl = new GameController(s, new ConsoleView(true));
            PlayChoice choice = ctrl.chooseTurn();
            check("bot calls UNO when play leaves one card", choice.kind == PlayChoice.Kind.PLAY && choice.unoCalled);
        }

        // ----------------------------------------------------------------
        // 13. SCRIPTED HUMAN INPUT
        // ----------------------------------------------------------------
        {
            ArrayList<Card> h = new ArrayList<>();
            h.add(card("R5"));
            ConsoleView view = new ConsoleView(true, new Scanner("draw\n"));
            check("human input draw command", view.askHuman(h, card("R9"), "").kind == PlayChoice.Kind.DRAW);
        }
        {
            ArrayList<Card> h = new ArrayList<>();
            h.add(card("R5"));
            h.add(card("G2"));
            ConsoleView view = new ConsoleView(true, new Scanner("R5\n"));
            PlayChoice choice = view.askHuman(h, card("R9"), "");
            check("human input card token chooses legal card",
                  choice.kind == PlayChoice.Kind.PLAY && choice.index == 0);
        }
        {
            ArrayList<Card> h = new ArrayList<>();
            h.add(card("R5"));
            h.add(card("G2"));
            ConsoleView view = new ConsoleView(true, new Scanner("R5 UNO\n"));
            PlayChoice choice = view.askHuman(h, card("R9"), "");
            check("human input can call UNO with card token",
                  choice.kind == PlayChoice.Kind.PLAY && choice.index == 0 && choice.unoCalled);
        }
        {
            ArrayList<Card> h = new ArrayList<>();
            h.add(card("R5"));
            ConsoleView view = new ConsoleView(true, new Scanner("9\n"));
            check("human input bad index returns invalid",
                  view.askHuman(h, card("R9"), "").kind == PlayChoice.Kind.INVALID);
        }
        {
            ConsoleView view = new ConsoleView(true, new Scanner("yes\n"));
            check("human input says yes to drawn card", view.askPlayDrawn(card("R5")));
        }
        {
            ConsoleView view = new ConsoleView(true, new Scanner("x\nb\n"));
            check("human input retries color until valid", view.askColor().equals("B"));
        }

        // ----------------------------------------------------------------
        // 14. INJECTABLE RULE SET
        // ----------------------------------------------------------------
        {
            RuleSet allCardsLegal = new RuleSet() {
                @Override public boolean isLegal(Card card, Card upCard, String calledColor) {
                    return true;
                }
            };
            ArrayList<Card> h = new ArrayList<>();
            h.add(card("G2"));
            ConsoleView view = new ConsoleView(true, new Scanner("G2\n"), allCardsLegal);
            PlayChoice choice = view.askHuman(h, card("R9"), "");
            check("ConsoleView uses injected rule set", choice.kind == PlayChoice.Kind.PLAY);
        }

        // ----------------------------------------------------------------
        // 15. TARGET SCORE FLOW
        // ----------------------------------------------------------------
        {
            check("target score keeps playing below target",
                  Main.shouldPlayRound(3, 1, 100, new int[] {90, 20}));
            check("target score stops when reached",
                  !Main.shouldPlayRound(4, 1, 100, new int[] {100, 20}));
            assertEquals("final winner is highest score", 1, Main.finalWinnerIndex(new int[] {80, 120, 90}));
        }

        // ----------------------------------------------------------------
        // 16. BOT PRIORITY -- surprising edge case
        // ----------------------------------------------------------------
        {
            ArrayList<Card> h = new ArrayList<>();
            h.add(card("R3")); h.add(card("RS")); h.add(card("R+2")); h.add(card("W"));
            int chosen = Rules.chooseBotCard(h, card("R9"), "");
            check("bot prefers DRAW_TWO over all", h.get(chosen).rank() == Card.Rank.DRAW_TWO);
        }
        {
            ArrayList<Card> h = new ArrayList<>();
            h.add(card("R3")); h.add(card("RS")); h.add(card("W"));
            int chosen = Rules.chooseBotCard(h, card("R9"), "");
            check("bot prefers SKIP over NUMBER and WILD", h.get(chosen).rank() == Card.Rank.SKIP);
        }
        {
            ArrayList<Card> h = new ArrayList<>();
            h.add(card("G3")); h.add(card("GS"));
            check("bot draws when no legal card",
                  Rules.chooseBotCard(h, card("R9"), "") == -1);
        }

        // ----------------------------------------------------------------
        // 17. END-TO-END GAMEPLAY -- full game with seeded RNG
        // ----------------------------------------------------------------
        {
            // A complete bot-only game must finish within the safety limit
            // and accumulate a non-negative score for the winner.
            ArrayList<String>  names  = names("Bot1", "Bot2", "Bot3");
            ArrayList<Boolean> humans = bools(false, false, false);
            int[]              scores = new int[3];
            GameState      state = new GameState(names, humans, scores, new Random(42));
            ConsoleView    view  = new ConsoleView(true);   // quiet: no stdout
            GameController ctrl  = new GameController(state, view);

            int winner = ctrl.playGame();
            check("end-to-end: game finishes (winner >= 0)", winner >= 0);
            check("end-to-end: winner's score is positive",  scores[winner] > 0);
        }
        {
            // Multi-game: scores accumulate correctly across 5 games.
            // Seed 123 is verified to complete all 5 games within the safety limit.
            ArrayList<String>  names  = names("Bot1", "Bot2", "Bot3");
            ArrayList<Boolean> humans = bools(false, false, false);
            int[]              scores = new int[3];
            GameState      state = new GameState(names, humans, scores, new Random(123));
            ConsoleView    view  = new ConsoleView(true);
            GameController ctrl  = new GameController(state, view);

            int totalWins = 0;
            for (int g = 0; g < 5; g++) {
                int w = ctrl.playGame();
                if (w >= 0) totalWins++;
            }
            check("end-to-end: all 5 games finish", totalWins == 5);
            int totalScore = 0;
            for (int s : scores) totalScore += s;
            check("end-to-end: total score > 0 after 5 games", totalScore > 0);
        }
        {
            // Behavior preservation: same seed produces same winner and score
            int[] scores1 = new int[3];
            int[] scores2 = new int[3];
            ArrayList<String>  names  = names("Bot1", "Bot2", "Bot3");
            ArrayList<Boolean> humans = bools(false, false, false);

            GameController c1 = new GameController(
                    new GameState(names, humans, scores1, new Random(999)), new ConsoleView(true));
            GameController c2 = new GameController(
                    new GameState(names, humans, scores2, new Random(999)), new ConsoleView(true));

            int w1 = c1.playGame();
            int w2 = c2.playGame();
            check("same seed produces same winner", w1 == w2);
            if (w1 >= 0 && w2 >= 0) {
                check("same seed produces same score", scores1[w1] == scores2[w2]);
            }
        }

        System.out.println("CharacterizationTests: passed " + passed + "/" + total);
        return passed;
    }

    // ---- helpers ----

    private Card card(String token) { return new Card(token); }

    private int count(ArrayList<Card> cards, String token) {
        int total = 0;
        for (Card card : cards) {
            if (card.token.equals(token)) total++;
        }
        return total;
    }

    private void check(String label, boolean condition) {
        total++;
        if (condition) passed++;
        else System.out.println("  FAIL: " + label);
    }

    private void assertEquals(String label, int expected, int actual) {
        check(label + " (expected=" + expected + " got=" + actual + ")", expected == actual);
    }

    private GameState threePlayerState(long seed) {
        GameState s = new GameState(names("Bot1","Bot2","Bot3"), bools(false,false,false),
                                    new int[3], new Random(seed));
        s.reset();
        s.currentPlayer = 0;
        s.direction     = 1;
        s.upCard        = card("R5");
        s.calledColor   = "";
        return s;
    }

    private GameState twoPlayerState(long seed) {
        GameState s = new GameState(names("Bot1","Bot2"), bools(false,false),
                                    new int[2], new Random(seed));
        s.reset();
        s.currentPlayer = 0;
        s.direction     = 1;
        s.upCard        = card("R5");
        s.calledColor   = "";
        return s;
    }

    private ArrayList<String> names(String... ns) {
        ArrayList<String> l = new ArrayList<>();
        for (String n : ns) l.add(n);
        return l;
    }

    private ArrayList<Boolean> bools(boolean... bs) {
        ArrayList<Boolean> l = new ArrayList<>();
        for (boolean b : bs) l.add(b);
        return l;
    }
}
