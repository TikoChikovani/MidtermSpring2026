package uno.persistence;

import java.time.LocalDateTime;

public class RoundRecord {
    private Long id;
    private Long gameId;
    private int roundNumber;
    private Long winnerPlayerId;
    private LocalDateTime completedAt;

    public RoundRecord() {}

    public RoundRecord(Long gameId, int roundNumber, Long winnerPlayerId, LocalDateTime completedAt) {
        this.gameId = gameId;
        this.roundNumber = roundNumber;
        this.winnerPlayerId = winnerPlayerId;
        this.completedAt = completedAt;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getGameId() { return gameId; }
    public void setGameId(Long gameId) { this.gameId = gameId; }

    public int getRoundNumber() { return roundNumber; }
    public void setRoundNumber(int roundNumber) { this.roundNumber = roundNumber; }

    public Long getWinnerPlayerId() { return winnerPlayerId; }
    public void setWinnerPlayerId(Long winnerPlayerId) { this.winnerPlayerId = winnerPlayerId; }

    public LocalDateTime getCompletedAt() { return completedAt; }
    public void setCompletedAt(LocalDateTime completedAt) { this.completedAt = completedAt; }
}
