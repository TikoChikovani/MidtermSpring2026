package uno.persistence;

import java.time.LocalDateTime;

public class GameRecord {
    private Long id;
    private LocalDateTime startedAt;
    private LocalDateTime completedAt;
    private Long finalWinnerPlayerId;

    public GameRecord() {}

    public GameRecord(LocalDateTime startedAt) {
        this.startedAt = startedAt;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public LocalDateTime getStartedAt() { return startedAt; }
    public void setStartedAt(LocalDateTime startedAt) { this.startedAt = startedAt; }

    public LocalDateTime getCompletedAt() { return completedAt; }
    public void setCompletedAt(LocalDateTime completedAt) { this.completedAt = completedAt; }

    public Long getFinalWinnerPlayerId() { return finalWinnerPlayerId; }
    public void setFinalWinnerPlayerId(Long finalWinnerPlayerId) {
        this.finalWinnerPlayerId = finalWinnerPlayerId;
    }
}
