package uno;

import java.io.IOException;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

/**
 * Central logging setup for the CLI.
 */
public class LoggingConfig {

    private LoggingConfig() {}

    public static void configureForApplication() {
        Logger root = Logger.getLogger("");
        for (java.util.logging.Handler handler : root.getHandlers()) {
            root.removeHandler(handler);
        }

        try {
            FileHandler handler = new FileHandler("uno.log", true);
            handler.setFormatter(new SimpleFormatter());
            handler.setLevel(Level.INFO);
            root.addHandler(handler);
            root.setLevel(Level.INFO);
        } catch (IOException e) {
            System.err.println("Logging disabled: could not open uno.log");
            root.setLevel(Level.OFF);
        }
    }

    public static void disableForTests() {
        Logger root = Logger.getLogger("");
        for (java.util.logging.Handler handler : root.getHandlers()) {
            root.removeHandler(handler);
        }
        root.setLevel(Level.OFF);
    }
}
