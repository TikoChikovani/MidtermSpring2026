package uno.persistence;

import java.time.LocalDateTime;

public class RecentGameReport {
    private Long gameId;
    private LocalDateTime startedAt;
    private LocalDateTime completedAt;
    private String winnerName;
    private Integer winningScore;
    private Integer roundsPlayed;

    public Long getGameId() { return gameId; }
    public void setGameId(Long gameId) { this.gameId = gameId; }

    public LocalDateTime getStartedAt() { return startedAt; }
    public void setStartedAt(LocalDateTime startedAt) { this.startedAt = startedAt; }

    public LocalDateTime getCompletedAt() { return completedAt; }
    public void setCompletedAt(LocalDateTime completedAt) { this.completedAt = completedAt; }

    public String getWinnerName() { return winnerName; }
    public void setWinnerName(String winnerName) { this.winnerName = winnerName; }

    public Integer getWinningScore() { return winningScore; }
    public void setWinningScore(Integer winningScore) { this.winningScore = winningScore; }

    public Integer getRoundsPlayed() { return roundsPlayed; }
    public void setRoundsPlayed(Integer roundsPlayed) { this.roundsPlayed = roundsPlayed; }
}
