import java.util.ArrayList;
import java.util.Scanner;

/**
 * Handles all console input and output.
 *
 * Game logic does not belong here. Rendering and prompting do.
 * Separating this class means game rules can be tested without
 * touching stdin or stdout.
 */
public class ConsoleView {

    private final Scanner scanner;
    private final boolean quiet;
    private final RuleSet rules;

    public ConsoleView(boolean quiet) {
        this(quiet, new Scanner(System.in), new RuleSet());
    }

    public ConsoleView(boolean quiet, Scanner scanner) {
        this(quiet, scanner, new RuleSet());
    }

    public ConsoleView(boolean quiet, Scanner scanner, RuleSet rules) {
        this.quiet = quiet;
        this.scanner = scanner;
        this.rules = rules;
    }

    // ---- Output ----

    public void showGameHeader(int gameNumber) {
        if (!quiet) System.out.println("\n=== Game " + gameNumber + " ===");
    }

    public void showTurnHeader(GameState state) {
        if (quiet) return;
        String colorNote = state.calledColor.equals("") ? "" : " called " + state.calledColor;
        System.out.println("\nUp card: " + state.upCard + colorNote);
        System.out.println(state.currentName() + " hand: " + formatHand(state.currentHand()));
    }

    public void showDraw(String name, Card drawn) {
        if (!quiet) System.out.println(name + " draws " + drawn);
    }

    public void showPlay(String name, Card card) {
        if (!quiet) System.out.println(name + " plays " + card);
    }

    public void showColorCall(String name, String color) {
        if (!quiet) System.out.println(name + " calls " + color);
    }

    public void showUno(String name) {
        if (!quiet) System.out.println(name + " says UNO!");
    }

    public void showWin(String name, int points) {
        if (!quiet) System.out.println(name + " wins and scores " + points);
    }

    public void showPenaltyInvalidIndex(String name) {
        if (!quiet) System.out.println(name + " selected an invalid index and draws a penalty card.");
    }

    public void showPenaltyIllegal(String name, Card card) {
        if (!quiet) System.out.println(name + " tried illegal card " + card + " and draws a penalty card.");
    }

    public void showDrawTwo(String name) {
        if (!quiet) System.out.println(name + " draws two.");
    }

    public void showDrawFour(String name) {
        if (!quiet) System.out.println(name + " draws four.");
    }

    public void showSafetyLimit() {
        if (!quiet) System.out.println("Game stopped at safety limit.");
    }

    public void showFinalScores(ArrayList<String> names, int[] scores) {
        System.out.println("\nFinal scores:");
        for (int i = 0; i < names.size(); i++) {
            System.out.println(names.get(i) + ": " + scores[i]);
        }
    }

    // ---- Input ----

    /**
     * Prompts the human player.
     * Returns PlayChoice.draw(), PlayChoice.play(index), or PlayChoice.invalid()
     * for an out-of-range index (which triggers the penalty-card rule in the controller).
     */
    public PlayChoice askHuman(ArrayList<Card> hand, Card upCard, String calledColor) {
        while (true) {
            if (!quiet) System.out.print("Choose card index/code or draw: ");
            String input = scanner.nextLine().trim().toUpperCase();

            if (input.equals("DRAW")) return PlayChoice.draw();

            try {
                int index = Integer.parseInt(input);
                if (index >= 0 && index < hand.size()) return PlayChoice.play(index);
                return PlayChoice.invalid();   // out-of-range index -> penalty
            } catch (NumberFormatException ignored) {}

            for (int i = 0; i < hand.size(); i++) {
                if (hand.get(i).token.equals(input)) {
                    if (rules.isLegal(hand.get(i), upCard, calledColor)) return PlayChoice.play(i);
                    if (!quiet) System.out.println("That card is not legal.");
                }
            }
            if (!quiet) System.out.println("Card not found.");
        }
    }

    public boolean askPlayDrawn(Card drawn) {
        if (!quiet) System.out.print("Play drawn card " + drawn + "? y/n: ");
        String answer = scanner.nextLine();
        return answer.equalsIgnoreCase("y") || answer.equalsIgnoreCase("yes");
    }

    public String askColor() {
        while (true) {
            if (!quiet) System.out.print("Call color R/Y/G/B: ");
            String input = scanner.nextLine().trim().toUpperCase();
            if (input.equals("R") || input.equals("Y")
                    || input.equals("G") || input.equals("B")) return input;
            if (!quiet) System.out.println("Bad color.");
        }
    }

    // ---- Helpers ----

    public static String formatHand(ArrayList<Card> hand) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < hand.size(); i++) {
            sb.append(i).append(":").append(hand.get(i).token);
            if (i < hand.size() - 1) sb.append(" ");
        }
        return sb.toString();
    }
}
