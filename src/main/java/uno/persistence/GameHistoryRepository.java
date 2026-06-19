package uno.persistence;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;

public class GameHistoryRepository {

    private final SqlSessionFactory sqlSessionFactory;

    public GameHistoryRepository(SqlSessionFactory sqlSessionFactory) {
        this.sqlSessionFactory = sqlSessionFactory;
    }

    public PersistedGame startGame(List<String> playerNames) {
        try (SqlSession session = sqlSessionFactory.openSession(false)) {
            PlayerMapper playerMapper = session.getMapper(PlayerMapper.class);
            GameMapper gameMapper = session.getMapper(GameMapper.class);

            Map<Integer, Long> playerIdsByIndex = new LinkedHashMap<>();
            for (int i = 0; i < playerNames.size(); i++) {
                playerIdsByIndex.put(i, findOrCreatePlayer(playerMapper, playerNames.get(i)));
            }

            GameRecord game = new GameRecord(LocalDateTime.now());
            gameMapper.insert(game);
            session.commit();
            return new PersistedGame(game.getId(), playerIdsByIndex);
        }
    }

    public void recordRound(PersistedGame game,
                            int roundNumber,
                            int winnerIndex,
                            int[] roundPoints,
                            int[] totalScores) {
        try (SqlSession session = sqlSessionFactory.openSession(false)) {
            RoundMapper roundMapper = session.getMapper(RoundMapper.class);
            ScoreMapper scoreMapper = session.getMapper(ScoreMapper.class);

            Long winnerPlayerId = winnerIndex >= 0 ? game.playerId(winnerIndex) : null;
            RoundRecord round = new RoundRecord(
                    game.gameId(),
                    roundNumber,
                    winnerPlayerId,
                    LocalDateTime.now()
            );
            roundMapper.insert(round);

            for (int i = 0; i < totalScores.length; i++) {
                scoreMapper.insert(new ScoreRecord(
                        game.gameId(),
                        round.getId(),
                        game.playerId(i),
                        roundPoints[i],
                        totalScores[i]
                ));
            }

            session.commit();
        }
    }

    public void completeGame(PersistedGame game, int finalWinnerIndex) {
        try (SqlSession session = sqlSessionFactory.openSession(false)) {
            GameMapper gameMapper = session.getMapper(GameMapper.class);
            Long winnerPlayerId = finalWinnerIndex >= 0 ? game.playerId(finalWinnerIndex) : null;
            gameMapper.complete(game.gameId(), LocalDateTime.now(), winnerPlayerId);
            session.commit();
        }
    }

    public List<RecentGameReport> recentGames(int limit) {
        try (SqlSession session = sqlSessionFactory.openSession()) {
            return new ArrayList<>(session.getMapper(GameMapper.class).recentGames(limit));
        }
    }

    public List<PlayerWinReport> playerWinCounts() {
        try (SqlSession session = sqlSessionFactory.openSession()) {
            return new ArrayList<>(session.getMapper(ScoreMapper.class).playerWinCounts());
        }
    }

    public List<HighScoreReport> highestScores(int limit) {
        try (SqlSession session = sqlSessionFactory.openSession()) {
            return new ArrayList<>(session.getMapper(ScoreMapper.class).highestScores(limit));
        }
    }

    private Long findOrCreatePlayer(PlayerMapper mapper, String name) {
        PlayerRecord existing = mapper.findByName(name);
        if (existing != null) return existing.getId();

        PlayerRecord player = new PlayerRecord(name);
        mapper.insert(player);
        return player.getId();
    }
}
