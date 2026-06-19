import java.util.ArrayList;

/**
 * Explicit entry point for lightweight unit checks and characterization tests.
 */
public class TestLauncher {

    public static void main(String[] args) {
        runAll();
    }

    public static void runAll() {
        int unitPassed = runUnitChecks();
        int charPassed = CharacterizationTests.run();

        System.out.println("Total self-test passed: " + (unitPassed + charPassed));
    }

    private static int runUnitChecks() {
        int passed = 0;

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
        return passed;
    }

    private static void fail(String name) {
        throw new RuntimeException("Failed: " + name);
    }
}
