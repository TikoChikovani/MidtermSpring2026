package uno.persistence;

import java.util.List;

import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Result;
import org.apache.ibatis.annotations.Results;
import org.apache.ibatis.annotations.Select;

public interface ScoreMapper {

    @Insert("""
            INSERT INTO scores(game_id, round_id, player_id, round_points, total_score)
            VALUES(#{gameId}, #{roundId}, #{playerId}, #{roundPoints}, #{totalScore})
            """)
    @Options(useGeneratedKeys = true, keyProperty = "id")
    void insert(ScoreRecord score);

    @Select("""
            SELECT p.name AS player_name,
                   COUNT(*) AS win_count
            FROM games g
            JOIN players p ON p.id = g.final_winner_player_id
            WHERE g.final_winner_player_id IS NOT NULL
            GROUP BY p.name
            ORDER BY win_count DESC, p.name ASC
            """)
    @Results({
            @Result(column = "player_name", property = "playerName"),
            @Result(column = "win_count", property = "winCount")
    })
    List<PlayerWinReport> playerWinCounts();

    @Select("""
            SELECT p.name AS player_name,
                   s.game_id,
                   MAX(s.total_score) AS score,
                   g.completed_at
            FROM scores s
            JOIN players p ON p.id = s.player_id
            JOIN games g ON g.id = s.game_id
            GROUP BY p.name, s.game_id, g.completed_at
            ORDER BY score DESC, g.completed_at DESC
            LIMIT #{limit}
            """)
    @Results({
            @Result(column = "player_name", property = "playerName"),
            @Result(column = "game_id", property = "gameId"),
            @Result(column = "score", property = "score"),
            @Result(column = "completed_at", property = "completedAt")
    })
    List<HighScoreReport> highestScores(@Param("limit") int limit);
}
