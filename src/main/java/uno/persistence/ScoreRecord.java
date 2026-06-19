package uno.persistence;

public class ScoreRecord {
    private Long id;
    private Long gameId;
    private Long roundId;
    private Long playerId;
    private int roundPoints;
    private int totalScore;

    public ScoreRecord() {}

    public ScoreRecord(Long gameId, Long roundId, Long playerId, int roundPoints, int totalScore) {
        this.gameId = gameId;
        this.roundId = roundId;
        this.playerId = playerId;
        this.roundPoints = roundPoints;
        this.totalScore = totalScore;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getGameId() { return gameId; }
    public void setGameId(Long gameId) { this.gameId = gameId; }

    public Long getRoundId() { return roundId; }
    public void setRoundId(Long roundId) { this.roundId = roundId; }

    public Long getPlayerId() { return playerId; }
    public void setPlayerId(Long playerId) { this.playerId = playerId; }

    public int getRoundPoints() { return roundPoints; }
    public void setRoundPoints(int roundPoints) { this.roundPoints = roundPoints; }

    public int getTotalScore() { return totalScore; }
    public void setTotalScore(int totalScore) { this.totalScore = totalScore; }
}
