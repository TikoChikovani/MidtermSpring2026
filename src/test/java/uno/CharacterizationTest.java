package uno;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class CharacterizationTest {

    @Test
    void characterizationSuitePassesThroughMaven() {
        LoggingConfig.disableForTests();

        assertEquals(60, CharacterizationTests.run());
    }
}
