package uno;

import uno.persistence.GameHistoryRepository;
import uno.persistence.HighScoreReport;
import uno.persistence.PersistenceFactory;
import uno.persistence.PersistedGame;
import uno.persistence.PlayerWinReport;
import uno.persistence.RecentGameReport;

import java.util.Random;
import java.util.ArrayList;
import java.util.Arrays;
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
        int     targetScore = 0;
        boolean human  = false;
        boolean quiet  = false;
        String  report = "";
        int     reportLimit = 10;
        long    seed   = System.currentTimeMillis();

        for (int i = 0; i < args.length; i++) {
            if      (args[i].equals("--bots")  && i + 1 < args.length) bots  = Integer.parseInt(args[++i]);
            else if (args[i].equals("--games") && i + 1 < args.length) games = Integer.parseInt(args[++i]);
            else if (args[i].equals("--target-score") && i + 1 < args.length) {
                targetScore = Integer.parseInt(args[++i]);
            }
            else if (args[i].equals("--human"))    human = true;
            else if (args[i].equals("--quiet"))    quiet = true;
            else if (args[i].equals("--seed") && i + 1 < args.length) seed = Long.parseLong(args[++i]);
            else if (args[i].equals("--recent-games")) report = "recent";
            else if (args[i].equals("--player-wins")) report = "wins";
            else if (args[i].equals("--highest-scores")) report = "scores";
            else if (args[i].equals("--limit") && i + 1 < args.length) reportLimit = Integer.parseInt(args[++i]);
            else if (args[i].equals("--self-test")) {
                System.out.println("Use `mvn test` to run the automated test suite.");
                return;
            }
            else if (args[i].equals("--help")) {
                System.out.println("Usage: scripts/run.sh [--bots N] [--games N] [--target-score N] [--human] [--quiet] [--seed N]");
                System.out.println("Reports: --recent-games [--limit N] | --player-wins | --highest-scores [--limit N]");
                return;
            }
        }

        GameHistoryRepository history = PersistenceFactory.createDefault();

        if (!report.equals("")) {
            showReport(history, report, reportLimit);
            return;
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
        PersistedGame  persistedGame = history.startGame(names);

        int roundNumber = 1;
        while (shouldPlayRound(roundNumber, games, targetScore, scores)) {
            int[] beforeRound = Arrays.copyOf(scores, scores.length);
            view.showGameHeader(roundNumber);
            LOGGER.info("Game start requested: gameNumber=" + roundNumber);
            int winner = ctrl.playGame();
            history.recordRound(
                    persistedGame,
                    roundNumber,
                    winner,
                    roundPoints(beforeRound, scores),
                    Arrays.copyOf(scores, scores.length)
            );
            roundNumber++;
        }
        int completedRounds = roundNumber - 1;

        history.completeGame(persistedGame, finalWinnerIndex(scores));

        view.showFinalScores(names, scores);
        LOGGER.info("Game end: completedRounds=" + completedRounds);
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

    static int[] roundPoints(int[] before, int[] after) {
        int[] points = new int[after.length];
        for (int i = 0; i < after.length; i++) {
            points[i] = after[i] - before[i];
        }
        return points;
    }

    static int finalWinnerIndex(int[] scores) {
        if (scores.length == 0) return -1;
        int winner = 0;
        for (int i = 1; i < scores.length; i++) {
            if (scores[i] > scores[winner]) winner = i;
        }
        return winner;
    }

    static boolean shouldPlayRound(int nextRound, int maxRounds, int targetScore, int[] scores) {
        if (targetScore > 0) return highestScore(scores) < targetScore;
        return nextRound <= maxRounds;
    }

    static int highestScore(int[] scores) {
        int highest = 0;
        for (int score : scores) {
            if (score > highest) highest = score;
        }
        return highest;
    }

    private static void showReport(GameHistoryRepository history, String report, int limit) {
        if (report.equals("recent")) {
            System.out.println("Recent games:");
            for (RecentGameReport game : history.recentGames(limit)) {
                System.out.println("Game " + game.getGameId()
                        + " completed=" + game.getCompletedAt()
                        + " winner=" + printable(game.getWinnerName())
                        + " winningScore=" + printable(game.getWinningScore())
                        + " rounds=" + printable(game.getRoundsPlayed()));
            }
        } else if (report.equals("wins")) {
            System.out.println("Player win counts:");
            for (PlayerWinReport row : history.playerWinCounts()) {
                System.out.println(row.getPlayerName() + ": " + row.getWinCount());
            }
        } else if (report.equals("scores")) {
            System.out.println("Highest scores:");
            for (HighScoreReport row : history.highestScores(limit)) {
                System.out.println(row.getPlayerName()
                        + " game=" + row.getGameId()
                        + " score=" + row.getScore()
                        + " completed=" + row.getCompletedAt());
            }
        }
    }

    private static String printable(Object value) {
        return value == null ? "n/a" : value.toString();
    }
}
