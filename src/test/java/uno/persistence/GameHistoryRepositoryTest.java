package uno.persistence;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

class GameHistoryRepositoryTest {

    @Test
    void recordsCompletedGameAndReportsRecentGames() {
        GameHistoryRepository repository = repository("recent");

        PersistedGame game = repository.startGame(List.of("Ada", "Grace", "Linus"));
        repository.recordRound(game, 1, 1, new int[] {0, 42, 0}, new int[] {0, 42, 0});
        repository.recordRound(game, 2, 0, new int[] {30, 0, 0}, new int[] {30, 42, 0});
        repository.completeGame(game, 1);

        List<RecentGameReport> recent = repository.recentGames(5);

        assertEquals(1, recent.size());
        assertEquals("Grace", recent.get(0).getWinnerName());
        assertEquals(42, recent.get(0).getWinningScore());
        assertEquals(2, recent.get(0).getRoundsPlayed());
    }

    @Test
    void reportsPlayerWinCounts() {
        GameHistoryRepository repository = repository("wins");

        PersistedGame first = repository.startGame(List.of("Ada", "Grace"));
        repository.recordRound(first, 1, 0, new int[] {55, 0}, new int[] {55, 0});
        repository.completeGame(first, 0);

        PersistedGame second = repository.startGame(List.of("Ada", "Grace"));
        repository.recordRound(second, 1, 1, new int[] {0, 60}, new int[] {0, 60});
        repository.completeGame(second, 1);

        PersistedGame third = repository.startGame(List.of("Ada", "Grace"));
        repository.recordRound(third, 1, 0, new int[] {30, 0}, new int[] {30, 0});
        repository.completeGame(third, 0);

        List<PlayerWinReport> wins = repository.playerWinCounts();

        assertEquals("Ada", wins.get(0).getPlayerName());
        assertEquals(2, wins.get(0).getWinCount());
        assertEquals("Grace", wins.get(1).getPlayerName());
        assertEquals(1, wins.get(1).getWinCount());
    }

    @Test
    void reportsHighestScores() {
        GameHistoryRepository repository = repository("scores");

        PersistedGame game = repository.startGame(List.of("Ada", "Grace", "Linus"));
        repository.recordRound(game, 1, 2, new int[] {0, 0, 70}, new int[] {0, 0, 70});
        repository.recordRound(game, 2, 1, new int[] {0, 90, 0}, new int[] {0, 90, 70});
        repository.completeGame(game, 1);

        List<HighScoreReport> scores = repository.highestScores(2);

        assertFalse(scores.isEmpty());
        assertEquals("Grace", scores.get(0).getPlayerName());
        assertEquals(90, scores.get(0).getScore());
        assertEquals("Linus", scores.get(1).getPlayerName());
        assertEquals(70, scores.get(1).getScore());
    }

    private GameHistoryRepository repository(String name) {
        return PersistenceFactory.create("jdbc:h2:mem:uno_" + name + ";DB_CLOSE_DELAY=-1");
    }
}
