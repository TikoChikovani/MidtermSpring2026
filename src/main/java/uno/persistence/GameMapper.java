package uno.persistence;

import java.time.LocalDateTime;
import java.util.List;

import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Results;
import org.apache.ibatis.annotations.Result;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

public interface GameMapper {

    @Insert("INSERT INTO games(started_at) VALUES(#{startedAt})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    void insert(GameRecord game);

    @Update("""
            UPDATE games
            SET completed_at = #{completedAt},
                final_winner_player_id = #{finalWinnerPlayerId}
            WHERE id = #{gameId}
            """)
    void complete(@Param("gameId") Long gameId,
                  @Param("completedAt") LocalDateTime completedAt,
                  @Param("finalWinnerPlayerId") Long finalWinnerPlayerId);

    @Select("""
            SELECT g.id AS game_id,
                   g.started_at,
                   g.completed_at,
                   p.name AS winner_name,
                   MAX(s.total_score) AS winning_score,
                   COUNT(DISTINCT r.id) AS rounds_played
            FROM games g
            LEFT JOIN players p ON p.id = g.final_winner_player_id
            LEFT JOIN rounds r ON r.game_id = g.id
            LEFT JOIN scores s ON s.game_id = g.id
                AND s.player_id = g.final_winner_player_id
            GROUP BY g.id, g.started_at, g.completed_at, p.name
            ORDER BY g.completed_at DESC, g.started_at DESC
            LIMIT #{limit}
            """)
    @Results({
            @Result(column = "game_id", property = "gameId"),
            @Result(column = "started_at", property = "startedAt"),
            @Result(column = "completed_at", property = "completedAt"),
            @Result(column = "winner_name", property = "winnerName"),
            @Result(column = "winning_score", property = "winningScore"),
            @Result(column = "rounds_played", property = "roundsPlayed")
    })
    List<RecentGameReport> recentGames(@Param("limit") int limit);
}
