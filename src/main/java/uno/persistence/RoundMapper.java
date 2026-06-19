package uno.persistence;

import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Options;

public interface RoundMapper {

    @Insert("""
            INSERT INTO rounds(game_id, round_number, winner_player_id, completed_at)
            VALUES(#{gameId}, #{roundNumber}, #{winnerPlayerId}, #{completedAt})
            """)
    @Options(useGeneratedKeys = true, keyProperty = "id")
    void insert(RoundRecord round);
}
