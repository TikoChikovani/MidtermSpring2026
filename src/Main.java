import java.util.ArrayList;
import java.util.Random;

/**
 * Entry point. Parses CLI arguments, wires up the collaborators, and runs games.
 *
 * All game logic:  Card, Rules, GameState, GameController
 * All console I/O: ConsoleView
 * All char tests:  CharacterizationTests
 */
public class Main {

    public static void main(String[] args) {
        int     bots   = 3;
        int     games  = 1;
        boolean human  = false;
        boolean quiet  = false;
        long    seed   = System.currentTimeMillis();

        for (int i = 0; i < args.length; i++) {
            if      (args[i].equals("--bots")  && i + 1 < args.length) bots  = Integer.parseInt(args[++i]);
            else if (args[i].equals("--games") && i + 1 < args.length) games = Integer.parseInt(args[++i]);
            else if (args[i].equals("--human"))    human = true;
            else if (args[i].equals("--quiet"))    quiet = true;
            else if (args[i].equals("--seed") && i + 1 < args.length) seed = Long.parseLong(args[++i]);
            else if (args[i].equals("--self-test")) { selfTest(); return; }
            else if (args[i].equals("--help")) {
                System.out.println("Usage: scripts/run.sh [--bots N] [--games N] [--human] [--quiet] [--seed N]");
                return;
            }
        }

        ArrayList<String>  names  = buildPlayerNames(bots, human);
        ArrayList<Boolean> humans = buildHumanFlags(bots, human);
        int[]              scores = new int[names.size()];

        if (names.size() < 2 || names.size() > 4) {
            System.out.println("UNO needs 2 to 4 players.");
            return;
        }

        Random         random = new Random(seed);
        ConsoleView    view   = new ConsoleView(quiet);
        GameState      state  = new GameState(names, humans, scores, random);
        GameController ctrl   = new GameController(state, view);

        for (int g = 1; g <= games; g++) {
            view.showGameHeader(g);
            ctrl.playGame();
        }

        view.showFinalScores(names, scores);
    }

    // ---- player setup ----

    static ArrayList<String> buildPlayerNames(int bots, boolean human) {
        ArrayList<String> names = new ArrayList<>();
        if (human) names.add("You");
        for (int i = 1; i <= bots; i++) names.add("Bot" + i);
        return names;
    }

    static ArrayList<Boolean> buildHumanFlags(int bots, boolean human) {
        ArrayList<Boolean> flags = new ArrayList<>();
        if (human) flags.add(Boolean.TRUE);
        for (int i = 0; i < bots; i++) flags.add(Boolean.FALSE);
        return flags;
    }

    // ---- self-test ----

    static void selfTest() {
        int passed = 0;

        // Original lightweight unit checks (preserved)
        if (new Card("R5").color().equals("R"))              passed++; else fail("color R5");
        if (new Card("G+2").rank() == Card.Rank.DRAW_TWO)   passed++; else fail("rank +2");
        if (new Card("W4").points() == 50)                   passed++; else fail("wild points");
        if (Rules.isLegal(new Card("R2"), new Card("R9"), ""))  passed++; else fail("same color");
        if (Rules.isLegal(new Card("G9"), new Card("R9"), ""))  passed++; else fail("same number");
        if (Rules.isLegal(new Card("B3"), new Card("W"),  "B")) passed++; else fail("called color");
        if (!Rules.isLegal(new Card("B3"), new Card("R9"), "")) passed++; else fail("illegal mismatch");

        ArrayList<Card> h = new ArrayList<>();
        h.add(new Card("B3")); h.add(new Card("R4")); h.add(new Card("W"));
        if (Rules.chooseBotCard(h, new Card("R9"), "") == 1) passed++; else fail("bot normal before wild");

        ArrayList<Card> h2 = new ArrayList<>();
        h2.add(new Card("B1")); h2.add(new Card("B2")); h2.add(new Card("R3"));
        if (Rules.chooseBotColor(h2).equals("B")) passed++; else fail("bot color");

        System.out.println("Passed " + passed + "/9 unit checks.");

        // Full characterization test suite (gameplay effects)
        int charPassed = CharacterizationTests.run();

        System.out.println("Total self-test passed: " + (passed + charPassed));
    }

    static void fail(String name) {
        throw new RuntimeException("Failed: " + name);
    }
}
