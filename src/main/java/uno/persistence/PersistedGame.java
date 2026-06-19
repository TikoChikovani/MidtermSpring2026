package uno.persistence;

import java.util.Map;

public class PersistedGame {
    private final Long gameId;
    private final Map<Integer, Long> playerIdsByIndex;

    public PersistedGame(Long gameId, Map<Integer, Long> playerIdsByIndex) {
        this.gameId = gameId;
        this.playerIdsByIndex = Map.copyOf(playerIdsByIndex);
    }

    public Long gameId() {
        return gameId;
    }

    public Long playerId(int playerIndex) {
        return playerIdsByIndex.get(playerIndex);
    }
}
