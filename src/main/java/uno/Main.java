package uno;

import java.util.Random;
import java.util.ArrayList;
import java.util.logging.Logger;

/**
 * Entry point. Parses CLI arguments, wires up the collaborators, and runs games.
 *
 * All game logic:  Card, Rules, GameState, GameController
 * All console I/O: ConsoleView
 * All char tests:  CharacterizationTests
 */
public class Main {

    private static final Logger LOGGER = Logger.getLogger(Main.class.getName());

    public static void main(String[] args) {
        LoggingConfig.configureForApplication();

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
            else if (args[i].equals("--self-test")) {
                System.out.println("Use `mvn test` to run the automated test suite.");
                return;
            }
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
            LOGGER.info("Game start requested: gameNumber=" + g);
            ctrl.playGame();
        }

        view.showFinalScores(names, scores);
        LOGGER.info("Game end: completedGames=" + games);
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
}
